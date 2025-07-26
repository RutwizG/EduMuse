package com.project.hotelBookingManagement.dto;

import com.project.hotelBookingManagement.entity.User;
import com.project.hotelBookingManagement.entity.enums.Gender;
import lombok.Data;

@Data
public class GuestDto {

    private Long id;
    private User user;
    private String name;
    private Gender gender;
    private Integer age;

}
