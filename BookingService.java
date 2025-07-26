package com.project.hotelBookingManagement.service;

import com.project.hotelBookingManagement.dto.BookingDto;
import com.project.hotelBookingManagement.dto.BookingRequest;
import com.project.hotelBookingManagement.dto.GuestDto;
import com.project.hotelBookingManagement.dto.HotelReportDto;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.List;

public interface BookingService {

    BookingDto initializeBooking(BookingRequest bookingRequest);
    BookingDto addGuests(Long bookingId, List<GuestDto> guestDtoList);

    String initiatePayments(Long bookingId) throws StripeException;

    void capturePayment(Event event);

    void cancelBooking(Long bookingId);

    String getBookingStatus(Long bookingId);

    List<BookingDto> getAllBookingsByHotelId(Long hotelId) throws AccessDeniedException;

    HotelReportDto getHotelReport(Long hotelId, LocalDateTime startDate, LocalDateTime endDate) throws AccessDeniedException;

    List<BookingDto> getMyBookings();
}
