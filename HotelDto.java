package com.project.hotelBookingManagement.dto;

import com.project.hotelBookingManagement.entity.HotelContactInfo;
import com.project.hotelBookingManagement.entity.Room;
import lombok.Data;
import java.util.List;

@Data
public class HotelDto {

    private Long id;
    private String name;
    private String city;
    private String[] photos;
    private String[] amenities;
    private HotelContactInfo contactInfo;
    private boolean active;
    private List<Room> room;
}
