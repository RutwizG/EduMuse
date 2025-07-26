package com.project.hotelBookingManagement.dto;


import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class HotelReportDto {

    private long bookingCount;
    private BigDecimal totalRevenue;
    private BigDecimal avgRevenue;
}
