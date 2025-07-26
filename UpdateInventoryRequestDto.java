package com.project.hotelBookingManagement.dto;


import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UpdateInventoryRequestDto {

    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private BigDecimal surgeFactor;
    private Boolean closed;

}
