package com.project.hotelBookingManagement.dto;

import com.project.hotelBookingManagement.entity.enums.Gender;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ProfileUpdateDto {

    private String name;
    private LocalDate dateOfBirth;
    private Gender gender;
}
