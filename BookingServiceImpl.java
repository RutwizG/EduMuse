package com.project.hotelBookingManagement.service;


import com.project.hotelBookingManagement.dto.BookingDto;
import com.project.hotelBookingManagement.dto.BookingRequest;
import com.project.hotelBookingManagement.dto.GuestDto;
import com.project.hotelBookingManagement.dto.HotelReportDto;
import com.project.hotelBookingManagement.entity.*;
import com.project.hotelBookingManagement.entity.enums.BookingStatus;
import com.project.hotelBookingManagement.exception.ResourceNotFoundException;
import com.project.hotelBookingManagement.exception.UnauthorizedException;
import com.project.hotelBookingManagement.repository.*;
import com.project.hotelBookingManagement.strategy.PricingService;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.Refund;
import com.stripe.model.checkout.Session;
import com.stripe.param.RefundCreateParams;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import static com.project.hotelBookingManagement.util.AppUtils.getCurrentUser;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService{
    private final GuestRepository guestRepository;

    private final BookingRepository bookingRepository;
    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final InventoryRepository inventoryRepository;
    private final ModelMapper modelMapper;
    private final CheckoutService checkoutService;
    private final PricingService pricingService;

    @Value("${frontend.url}")
    private String frontendUrl;
    @Override
    @Transactional
    public BookingDto initializeBooking(BookingRequest bookingRequest) {

        log.info("Initializing booking for hotel: {}, room:{}, date:{} - {}",
                bookingRequest.getHotelId(),bookingRequest.getRoomId(),bookingRequest.getCheckInDate(),bookingRequest.getCheckOutDate());
        Hotel hotel = hotelRepository.findById(bookingRequest.getHotelId())
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id: "+bookingRequest.getHotelId()));

        Room room = roomRepository.findById(bookingRequest.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: "+bookingRequest.getRoomId()));

        List<Inventory> inventoryList = inventoryRepository
                .findAndLockAvailableInventory(room.getId(),bookingRequest.getCheckInDate(),bookingRequest.getCheckOutDate(),bookingRequest.getRoomsCount());
        long daysCount = ChronoUnit.DAYS.between(bookingRequest.getCheckInDate(),bookingRequest.getCheckOutDate())+1;
        if (inventoryList.size() != daysCount){
            throw new IllegalStateException("Room is not available anymore");
        }
//        Reserve the room/update the booked count of inventory
        inventoryRepository.initBooking(room.getId() ,bookingRequest.getCheckInDate(),bookingRequest.getCheckOutDate(),bookingRequest.getRoomsCount());

        //Create the booking
        User user = getCurrentUser();

        //Calculate dynamic amount
    BigDecimal priceForOneRoom = pricingService.calculateTotalPrice(inventoryList);
    BigDecimal totalPrice = priceForOneRoom.multiply(BigDecimal.valueOf(bookingRequest.getRoomsCount()));


        Booking booking = Booking.builder()
                .bookingStatus(BookingStatus.RESERVED)
                .hotel(hotel)
                .room(room)
                .checkInDate(bookingRequest.getCheckInDate())
                .checkOutDate(bookingRequest.getCheckOutDate())
                .user(user)
                .roomsCount(bookingRequest.getRoomsCount())
                .amount(totalPrice)
                .build();

        booking = bookingRepository.save(booking);
        return modelMapper.map(booking, BookingDto.class);

    }



    @Override
    public BookingDto addGuests(Long bookingId, List<GuestDto> guestDtoList) {
        log.info("Adding Guests for booking with id: {}", bookingId);
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: "+bookingId));
        User user = getCurrentUser();
        if(!user.equals(booking.getUser())){
            throw new UnauthorizedException("Booking does not belong to this user with id:"+ user.getId());
        }
        if(hasBookingExpired(booking)){
            throw new IllegalStateException("Booking has already expired");
        }
        if (booking.getBookingStatus() != BookingStatus.RESERVED){
            throw new IllegalStateException("Booking is not under reserved state");
        }
        for(GuestDto guestDto:guestDtoList){
            Guest guest = modelMapper.map(guestDto, Guest.class);
            guest.setUser(user);
            guest = guestRepository.save(guest);
            booking.getGuests().add(guest);
        }
        booking.setBookingStatus(BookingStatus.GUESTS_ADDED);
        booking = bookingRepository.save(booking);
        return modelMapper.map(booking, BookingDto.class);
    }

    @Override
    public String initiatePayments(Long bookingId) throws StripeException {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("booking not found with ID: "+bookingId));
        User user = getCurrentUser();
        if(!user.equals(booking.getUser())){
            throw new UnauthorizedException("Booking does not belong to the user with Id: "+user.getId());
        }
        if(hasBookingExpired(booking)){
            throw new IllegalStateException("Booking has already expired");
        }

        String sessionUrl = checkoutService.getCheckoutSession(booking,frontendUrl+"/payments/success",frontendUrl+"/payments/failure");
        booking.setBookingStatus(BookingStatus.PAYMENT_PENDING);
        bookingRepository.save(booking);
        return sessionUrl;

    }

    @Override
    @Transactional
    public void capturePayment(Event event) {
        if("checkout.session.completed".equals(event.getType())){

            Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
            if( session!= null){
                String sessionId = session.getId();
                Booking booking = bookingRepository.findByPaymentSessionId(sessionId).orElseThrow(() -> new ResourceNotFoundException("booking not found with Stripe "));
                booking.setBookingStatus(BookingStatus.CONFIRMED);
                bookingRepository.save(booking);
                inventoryRepository.findAndLockReservedInventory(booking.getRoom().getId(), booking.getCheckInDate(),booking.getCheckOutDate(),booking.getRoomsCount());
                inventoryRepository.confirmBooking(booking.getRoom().getId(), booking.getCheckInDate(),booking.getCheckOutDate(),booking.getRoomsCount());
                log.info("Booking confirmed with session ID: {}", sessionId);
            } else {
                log.warn("Unhandled event type: {}", event.getType());
            }
        }
    }

    @Override
    public void cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(
                () -> new ResourceNotFoundException("Booking not found with ID: "+bookingId)
        );
        User user = getCurrentUser();
        if(!user.equals(booking.getUser())){
            throw  new UnauthorizedException("Booking doesnot belong to this user wiht id:"+ user.getId());
        }
        if(booking.getBookingStatus() != BookingStatus.CONFIRMED){
            throw new IllegalStateException("Only Confirmed booking can be cancelled");
        }
        booking.setBookingStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
        inventoryRepository.findAndLockReservedInventory(booking.getRoom().getId(), booking.getCheckInDate(),booking.getCheckOutDate(),booking.getRoomsCount());
        inventoryRepository.cancelBooking(booking.getRoom().getId(), booking.getCheckInDate(),booking.getCheckOutDate(),booking.getRoomsCount());

        //Handle the Refund
        try{
            Session session = Session.retrieve(booking.getPaymentSessionId());
            RefundCreateParams refundCreateParams = RefundCreateParams.builder()
                    .setPaymentIntent(session.getPaymentIntent())

                    .build();
            Refund.create(refundCreateParams);
        } catch (StripeException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getBookingStatus(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("booking not found with ID: "+bookingId));
        User user = getCurrentUser();
        if(!user.equals(booking.getUser())){
            throw new UnauthorizedException("Booking does not belong to the user with Id: "+user.getId());
        }
        return booking.getBookingStatus().name();
    }

    @Override
    public List<BookingDto> getAllBookingsByHotelId(Long hotelId) throws AccessDeniedException {
        Hotel hotel = hotelRepository
                .findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with ID: "+ hotelId));
        User user = getCurrentUser();
        log.info("Getting all bookings for the hotel with id: {}", hotelId);
        if(!user.equals(hotel.getOwner())){
            throw new AccessDeniedException("You are not the owner of hotel with id: "+ hotelId);
        }
        List<Booking> bookings = bookingRepository.findByHotel(hotel);
        return bookings.stream().map((element) -> modelMapper.map(element, BookingDto.class)).collect(Collectors.toList());
    }

    @Override
    public HotelReportDto getHotelReport(Long hotelId, LocalDateTime startDate, LocalDateTime endDate) throws AccessDeniedException {
        Hotel hotel = hotelRepository
                .findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with ID: "+ hotelId));
        User user = getCurrentUser();
        if(!user.equals(hotel.getOwner())){
            throw new AccessDeniedException("You are not the owner of hotel with id: "+ hotelId);
        }

        List<Booking> bookings = bookingRepository.findByHotelAndCreatedAtBetween(hotel,startDate,endDate);

        Long totalConfirmedBookings = bookings.stream().filter(booking -> booking.getBookingStatus() == BookingStatus.CONFIRMED)
                .count();
        BigDecimal totalRevenueOfConfirmedBookings = bookings.stream().filter(booking -> booking.getBookingStatus() == BookingStatus.CONFIRMED)
                .map(Booking::getAmount)
                .reduce(BigDecimal.ZERO,BigDecimal::add);
        BigDecimal avgRevenue = totalRevenueOfConfirmedBookings
                .divide(BigDecimal.valueOf(totalConfirmedBookings), RoundingMode.DOWN);

        return new HotelReportDto(totalConfirmedBookings,totalRevenueOfConfirmedBookings,avgRevenue);
    }

    @Override
    public List<BookingDto> getMyBookings() {
        User user = getCurrentUser();
        return bookingRepository.getByUser(user)
                .stream().map((element) -> modelMapper.map(element, BookingDto.class)).collect(Collectors.toList());
    }

    public boolean hasBookingExpired(Booking booking){
        return booking.getCreatedAt().plusMinutes(10).isBefore(LocalDateTime.now());
    }
}
