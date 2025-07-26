package com.project.hotelBookingManagement.repository;

import com.project.hotelBookingManagement.dto.BookingDto;
import com.project.hotelBookingManagement.entity.Booking;
import com.project.hotelBookingManagement.entity.Hotel;
import com.project.hotelBookingManagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking,Long> {
    Optional<Booking> findByPaymentSessionId(String sessionId);

    List<Booking> findByHotel(Hotel hotel);

    List<Booking> findByHotelAndCreatedAtBetween(Hotel hotel, LocalDateTime createdAtAfter, LocalDateTime createdAtBefore);

    List<BookingDto> getByUser(User user);
}
