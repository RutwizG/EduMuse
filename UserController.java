package com.project.hotelBookingManagement.controller;


import com.project.hotelBookingManagement.dto.BookingDto;
import com.project.hotelBookingManagement.dto.ProfileUpdateDto;
import com.project.hotelBookingManagement.dto.UserDto;
import com.project.hotelBookingManagement.service.BookingService;
import com.project.hotelBookingManagement.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final BookingService bookingService;

    @PutMapping("/profile")
    public ResponseEntity<Void> updateProfile(@RequestBody ProfileUpdateDto profileUpdateDto){
        userService.updateProfile(profileUpdateDto);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/myBookings")
    public ResponseEntity<List<BookingDto>> getMyBookings(){
        return ResponseEntity.ok(bookingService.getMyBookings());
    }

    @GetMapping("/profile")
    public ResponseEntity<UserDto> getMyProfile(){
        return ResponseEntity.ok(userService.getMyProfile());
    }
}
