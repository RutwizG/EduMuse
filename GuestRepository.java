package com.project.hotelBookingManagement.repository;

import com.project.hotelBookingManagement.entity.Guest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuestRepository extends JpaRepository<Guest, Long> {
}