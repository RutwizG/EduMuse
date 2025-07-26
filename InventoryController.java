package com.project.hotelBookingManagement.controller;


import com.project.hotelBookingManagement.dto.InventoryDto;
import com.project.hotelBookingManagement.dto.UpdateInventoryRequestDto;
import com.project.hotelBookingManagement.entity.Inventory;
import com.project.hotelBookingManagement.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequestMapping("/admin/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<List<InventoryDto>> getAllInventoryByRoom(@PathVariable Long roomId) throws AccessDeniedException {
        return ResponseEntity.ok(inventoryService.getAllInventoryByRoom(roomId));
    }

    @PatchMapping("/rooms/{roomId}")
    public ResponseEntity<List<InventoryDto>> updateInventory(@PathVariable Long roomId,
                                                              @RequestBody UpdateInventoryRequestDto updateInventoryRequestDto) throws AccessDeniedException {
        inventoryService.updateInventory(roomId,updateInventoryRequestDto);
        return ResponseEntity.noContent().build();
    }

}
