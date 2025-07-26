package com.project.hotelBookingManagement.service;

import com.project.hotelBookingManagement.entity.Booking;
import com.stripe.exception.StripeException;

public interface CheckoutService {

    String getCheckoutSession(Booking booking, String successUrl, String failureUrl) throws StripeException;
}
