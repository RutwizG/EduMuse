package com.project.hotelBookingManagement.service;

import com.project.hotelBookingManagement.config.MapperConfig;
import com.project.hotelBookingManagement.dto.HotelDto;
import com.project.hotelBookingManagement.dto.RoomDto;
import com.project.hotelBookingManagement.entity.Hotel;
import com.project.hotelBookingManagement.entity.Room;
import com.project.hotelBookingManagement.entity.User;
import com.project.hotelBookingManagement.exception.ResourceNotFoundException;
import com.project.hotelBookingManagement.exception.UnauthorizedException;
import com.project.hotelBookingManagement.repository.HotelRepository;
import com.project.hotelBookingManagement.repository.RoomRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.project.hotelBookingManagement.util.AppUtils.getCurrentUser;
import static org.springframework.data.jpa.domain.AbstractPersistable_.id;


@Service
@RequiredArgsConstructor
@Slf4j
public class RoomServiceImpl implements RoomService{

    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;
    private final ModelMapper modelMapper;
    private final InventoryService inventoryService;

    @Override
    public RoomDto createNewRoom(Long hotelId, RoomDto roomDto) {
        log.info("Creating a new room in hotel with id:{}", hotelId);
        Room room = modelMapper.map(roomDto, Room.class);
        Hotel hotel = hotelRepository
                .findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id: "+ hotelId));

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if(!user.equals(hotel.getOwner())){
            throw new UnauthorizedException("This user does not own this hotel with id: "+hotelId);
        }
        room.setHotel(hotel);
        room = roomRepository.save(room);

        log.info("Room with id :{} is created in hotel with id:{}",room.getId(),hotelId);
//        Create Inventory
        if(hotel.isActive()){
            inventoryService.initializeRoomForYear(room);
        }
        return modelMapper.map(room,RoomDto.class);
    }

    @Override
    public List<RoomDto> getAllRoomsInHotel(Long hotelId) {
        log.info("Getting all rooms in hotel with id:{}", hotelId);
        Hotel hotel = hotelRepository
                .findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id: "+ hotelId));
        return hotel.getRooms()
                .stream()
                .map((element) -> modelMapper.map(element, RoomDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public RoomDto getRoomById(Long roomId) {
        log.info("Getting the room by id:{}", roomId);
        Room room = roomRepository
                .findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: "+ roomId));

        return modelMapper.map(room,RoomDto.class);
    }

    @Override
    @Transactional
    public void deleteRoomById(Long roomId) {
        log.info("Deleting room with ID:{}",roomId);
        Room room = roomRepository.findById(roomId)
                        .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: "+ roomId));

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if(!user.equals(room.getHotel().getOwner())){
            throw new UnauthorizedException("This user does not own this room with id: "+room);
        }
        inventoryService.deleteAllInventories(room);
        roomRepository.deleteById(roomId);


    }

    @Override
    @Transactional
    public RoomDto updateRoomById(Long hotelId, Long roomId, RoomDto roomDto) {
        log.info("Updating the Room with the ID: {}", roomId);
        Hotel hotel = hotelRepository
                .findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with ID: "+roomId));

        User user = getCurrentUser();

        if(!user.equals(hotel.getOwner())){
            throw new UnauthorizedException("This user does not own this hotel with id: "+id);
        }

        Room room = roomRepository
                .findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with ID: "+roomId));



        modelMapper.map(roomDto,room);
        room.setId(roomId);
        room = roomRepository.save(room);
        //TODO: Update the inventory as well for price changes
        return modelMapper.map(room, RoomDto.class);
    }
}
