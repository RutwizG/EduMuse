package com.project.hotelBookingManagement.controller;


import com.project.hotelBookingManagement.dto.HotelDto;
import com.project.hotelBookingManagement.dto.HotelInfoDto;
import com.project.hotelBookingManagement.dto.HotelPriceDto;
import com.project.hotelBookingManagement.dto.HotelSearchRequest;
import com.project.hotelBookingManagement.service.HotelService;
import com.project.hotelBookingManagement.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/hotels")
@RequiredArgsConstructor
@Slf4j
public class HotelBrowseController {


    private final InventoryService inventoryService;
    private final HotelService hotelService;

    @GetMapping("/search")
    public ResponseEntity<Page<HotelPriceDto>> searchHotels(@RequestBody HotelSearchRequest hotelSearchRequest){

        Page<HotelPriceDto> page = inventoryService.searchHotels(hotelSearchRequest);

        return ResponseEntity.ok(page);

    }

    @GetMapping("/{hotelId}")
    public ResponseEntity<HotelInfoDto>  getHotelInfo(@PathVariable Long hotelId){
        HotelInfoDto hotelInfo = hotelService.getHotelInfoById(hotelId);
        return ResponseEntity.ok(hotelInfo);
    }


}
