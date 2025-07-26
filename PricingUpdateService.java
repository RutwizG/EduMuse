package com.project.hotelBookingManagement.service;


import com.project.hotelBookingManagement.entity.Hotel;
import com.project.hotelBookingManagement.entity.HotelMinPrice;
import com.project.hotelBookingManagement.entity.Inventory;
import com.project.hotelBookingManagement.repository.HotelMinPriceRepository;
import com.project.hotelBookingManagement.repository.HotelRepository;
import com.project.hotelBookingManagement.repository.InventoryRepository;
import com.project.hotelBookingManagement.strategy.PricingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j

public class PricingUpdateService {
    private final HotelRepository hotelRepository;
    private final InventoryRepository inventoryRepository;
    private final PricingService pricingService;
    private final HotelMinPriceRepository hotelMinPriceRepository;

    //Scheduler to update the inventory and Hotel Min price tables every hour

    @Scheduled(cron = "0 0 * * * *")
    public void updatePrices(){

        int page = 0;
        int batchSize = 100;
        while(true){
            Page<Hotel> hotelPage = hotelRepository.findAll(PageRequest.of(page, batchSize));
            if (hotelPage.isEmpty()){
                break;
            }
            hotelPage.getContent().forEach(this::updateHotelPrices);
            page++;
        }
    }

    private void updateHotelPrices(Hotel hotel){
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = LocalDateTime.now().plusYears(1);
        List<Inventory> inventoryList = inventoryRepository.findByHotelAndDateBetween(hotel,startDate,endDate);
        updateInventoryPrices(inventoryList);
        updateHotelMinPrice(hotel, inventoryList, startDate, endDate);
        
    }

    private void updateHotelMinPrice(Hotel hotel, List<Inventory> inventoryList, LocalDateTime startDate, LocalDateTime endDate) {
        // Compute min price per day
        Map<LocalDateTime, BigDecimal> dailyMinPrices = inventoryList.stream()
                .collect(Collectors.groupingBy(
                        Inventory::getDate,
                        Collectors.mapping(Inventory::getPrice, Collectors.minBy(Comparator.naturalOrder()))
                ))
                .entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,e -> e.getValue().orElse(BigDecimal.ZERO)));
        List<HotelMinPrice> hotelPrices = new ArrayList<>();
        dailyMinPrices.forEach((date,price) -> {
            HotelMinPrice hotelPrice = hotelMinPriceRepository.findByHotelAndDate(hotel,date)
                    .orElse(new HotelMinPrice(hotel,date));
            hotelPrice.setPrice(price);
            hotelPrices.add(hotelPrice);
        });
        //Save all HotelPrice entities in bulk
        hotelMinPriceRepository.saveAll(hotelPrices);

    }

    private void updateInventoryPrices(List<Inventory> inventoryList) {

        inventoryList.forEach(inventory -> {
            BigDecimal dynamicPrice = pricingService.calculateDynamicPricing(inventory);
            inventory.setPrice(dynamicPrice);
        });
        inventoryRepository.saveAll(inventoryList);
    }
}
