package com.project.hotelBookingManagement.service;

import com.project.hotelBookingManagement.dto.*;
import com.project.hotelBookingManagement.entity.Room;
import org.springframework.data.domain.Page;

import java.nio.file.AccessDeniedException;
import java.util.List;

public interface InventoryService {

    void initializeRoomForYear(Room room);

    void deleteAllInventories(Room room);

    Page<HotelPriceDto> searchHotels(HotelSearchRequest hotelSearchRequest);

    List<InventoryDto> getAllInventoryByRoom(Long roomId) throws AccessDeniedException;

    void updateInventory(Long roomId, UpdateInventoryRequestDto updateInventoryRequestDto) throws AccessDeniedException;
}
