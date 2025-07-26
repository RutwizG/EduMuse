package com.project.hotelBookingManagement.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.project.hotelBookingManagement.entity.Hotel;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RoomDto {

    private Long id;
    @JsonIgnore
    private Hotel hotel;
    private String type;
    private BigDecimal basePrice;
    private String[] photos;
    private String[] amenities;
    private Integer totalCount;
    private Integer capacity;
}
