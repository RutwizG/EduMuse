package com.project.hotelBookingManagement.strategy;

import com.project.hotelBookingManagement.entity.Inventory;

import java.math.BigDecimal;

public interface PricingStrategy {

    BigDecimal calculatePrice(Inventory inventory);
}
