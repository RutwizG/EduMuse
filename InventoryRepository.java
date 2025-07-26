package com.project.hotelBookingManagement.repository;

import com.project.hotelBookingManagement.entity.Hotel;
import com.project.hotelBookingManagement.entity.Inventory;
import com.project.hotelBookingManagement.entity.Room;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    void deleteByRoom(Room room);


    @Query("""
            SELECT DISTINCT i.hotel
            FROM Inventory i
            WHERE i.city = :city
                    AND i.date BETWEEN :startDate AND :endDate
                    AND i.closed = false
                    AND (i.totalCount - i.bookCount - i.reservedCount) >= :roomsCount
            GROUP BY i.hotel, i.room
            HAVING COUNT(i.date) = :dateCount \s
       \s""")
    Page<Hotel> findHotelsWithAvailableInventory(
            @Param("city") String city,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("roomsCount") Integer roomsCount,
            @Param("dateCount") Long dateCount,
            Pageable pageable
    );


    @Query("""
            SELECT i
            FROM Inventory i
            WHERE i.room.id = :roomId
            AND i.date BETWEEN :startDate AND :endDate
            AND (i.totalCount - i.bookCount - i.reservedCount) >= :roomsCount\s
           \s""")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Inventory> findAndLockAvailableInventory(
            @Param("roomId") Long roomId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("roomsCount") Integer roomsCount
    );


    @Query("""
            SELECT i
            FROM Inventory i
            WHERE i.room.id = :roomId
            AND i.date BETWEEN :startDate AND :endDate
            AND (i.totalCount - i.bookCount) >= :numberOfRooms
            AND i.closed = false\s
           \s""")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Inventory> findAndLockReservedInventory(
            @Param("roomId") Long roomId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("roomsCount") int numberOfRooms
    );

    @Modifying
    @Query("""
            UPDATE Inventory i\s
                    SET  i.reservedCount = i.reservedCount + :numberOfRooms
                    WHERE i.room.id = :roomId
                        and i.date BETWEEN :startDate AND :endDate
                        and (i.totalCount - i.bookCount - i.reservedCount) >= :numberOfRooms
                        and i.closed = false
           \s""")
    void initBooking(
            @Param("roomId") Long roomId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("roomsCount") int numberOfRooms
    );

    List<Inventory> findByHotelAndDateBetween(Hotel hotel, LocalDateTime dateAfter, LocalDateTime dateBefore);


    @Modifying
    @Query("""
            UPDATE Inventory i\s
                    SET i.reservedCount = i.reservedCount - :numberOfRooms,
                        i.bookCount = i.bookCount + :numberOfRooms
                    WHERE i.room.id = :roomId
                        and i.date BETWEEN :startDate AND :endDate
                        and (i.totalCount - i.bookCount) >= :numberOfRooms
                        and i.reservedCount >= :numberOfRooms
                        and i.closed = false
       \s""")
    void confirmBooking(
            @Param("roomId") Long roomId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("numberOfRooms") int numberOfRooms
    );


    @Modifying
    @Query("""
            UPDATE Inventory i\s
                    SET  i.bookCount = i.bookCount - :numberOfRooms
                    WHERE i.room.id = :roomId
                        and i.date BETWEEN :startDate AND :endDate
                        and (i.totalCount - i.bookCount) >= :numberOfRooms
                        and i.closed = false
       \s""")
    void cancelBooking(
            @Param("roomId") Long roomId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("numberOfRooms") int numberOfRooms
    );


    List<Inventory> findByRoom(Room room);

    List<Inventory> findByRoomOrderByDate(Room room);

    List<Inventory> findByRoomOrderByDateDesc(Room room);

    List<Inventory> findByRoomOrderByDateAsc(Room room);

    @Query("""
            select i from Inventory i
                    WHERE i.room.id = :roomId
                        and i.date BETWEEN :startDate AND :endDate
       \s""")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    void getInventoryAndLockBeforeUpdate(
            @Param("roomId") Long roomId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
    @Modifying
    @Query("""
            UPDATE Inventory i\s
                    SET  i.surgeFactor = :surgeFactor,
                           i.closed = :closed
                    WHERE i.room.id = :roomId
                        and i.date BETWEEN :startDate AND :endDate
       \s""")
    void updateInventory(
            @Param("roomId") Long roomId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("closed") boolean closed,
            @Param("surgeFactor")BigDecimal surgeFactor
            );

}
