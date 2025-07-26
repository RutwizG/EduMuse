package com.project.hotelBookingManagement.dto;


import com.project.hotelBookingManagement.entity.enums.Gender;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UserDto {

    private Long id;
    private String password;
    private String name;
    private Gender gender;
    private LocalDate dateOfBirth;

}
