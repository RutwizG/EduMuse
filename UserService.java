package com.project.hotelBookingManagement.service;

import com.project.hotelBookingManagement.dto.ProfileUpdateDto;
import com.project.hotelBookingManagement.dto.UserDto;
import com.project.hotelBookingManagement.entity.User;

public interface UserService {

    User getUserById(Long id);

    void updateProfile(ProfileUpdateDto profileUpdateDto);

    UserDto getMyProfile();
}
