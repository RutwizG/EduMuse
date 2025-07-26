package com.project.hotelBookingManagement.repository;

import com.project.hotelBookingManagement.dto.HotelPriceDto;
import com.project.hotelBookingManagement.entity.Hotel;
import com.project.hotelBookingManagement.entity.HotelMinPrice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

public interface HotelMinPriceRepository extends JpaRepository<HotelMinPrice, Long> {


    @Query("""
            SELECT com.project.hotelBookingManagement.dto.HotelPriceDto(i.hotel,AVG(i.price))
            FROM HotelMinPrice i
            WHERE i.hotel.city = :city
                    AND i.date BETWEEN :startDate AND :endDate
                    AND i.hotel.active = true
            GROUP BY i.hotel
       \s""")
    Page<HotelPriceDto> findHotelsWithAvailableInventory(
            @Param("city") String city,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("roomsCount") Integer roomsCount,
            @Param("dateCount") Long dateCount,
            Pageable pageable
    );

    Optional<HotelMinPrice> findByHotelAndDate(Hotel hotel, LocalDateTime date);
}
