package com.project.hotelBookingManagement.dto;


import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class HotelSearchRequest {

    private String city;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer roomsCount;

    private Integer page = 0;
    private Integer size = 10;
}
