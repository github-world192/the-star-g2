package com.hotel.system.service;

import com.hotel.system.entity.Room;
import com.hotel.system.exception.ResourceNotFoundException;
import com.hotel.system.repository.RoomRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class RoomService {

    private final RoomRepository roomRepository;

    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    public List<Room> listRooms() {
        return roomRepository.findAll();
    }

    public Room getRoom(Long roomId) {
        return roomRepository.findById(roomId)
            .orElseThrow(() -> new ResourceNotFoundException("房型不存在"));
    }
}