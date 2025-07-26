package com.project.hotelBookingManagement.dto;


import com.project.hotelBookingManagement.entity.Guest;
import com.project.hotelBookingManagement.entity.Hotel;
import com.project.hotelBookingManagement.entity.Room;
import com.project.hotelBookingManagement.entity.User;
import com.project.hotelBookingManagement.entity.enums.BookingStatus;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class BookingDto {

    private Long id;
    private Hotel hotel;
    private Room room;
//    private User user;
    private Integer roomsCount;
    private LocalDateTime checkInDate;
    private LocalDateTime checkOutDate;
    private BookingStatus bookingStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Set<GuestDto> guests;
}
