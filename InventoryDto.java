package com.project.hotelBookingManagement.dto;


import com.project.hotelBookingManagement.entity.Hotel;
import com.project.hotelBookingManagement.entity.Room;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class InventoryDto {

    private Long id;
    private Hotel hotel;
    private Room room;
    private LocalDateTime date;
    private Integer bookCount;
    private Integer reservedCount;
    private Integer totalCount;
    private BigDecimal surgeFactor;
    private BigDecimal price;  // Base Price * surgeFactor
    private String city;
    private Boolean closed;

}
