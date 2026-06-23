package com.hotel.system.service;

import com.hotel.system.dto.BookingCreateRequest;
import com.hotel.system.entity.Booking;
import com.hotel.system.entity.Booking.BookingStatus;
import com.hotel.system.entity.Room;
import com.hotel.system.entity.User;
import com.hotel.system.exception.ResourceNotFoundException;
import com.hotel.system.repository.BookingRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RoomService roomService;

    public BookingService(BookingRepository bookingRepository, RoomService roomService) {
        this.bookingRepository = bookingRepository;
        this.roomService = roomService;
    }

    @Transactional(readOnly = true)
    public List<Booking> getMyBookings(User currentUser) {
        return bookingRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId());
    }

    @Transactional(readOnly = true)
    public Booking getMyBooking(User currentUser, Long bookingId) {
        return bookingRepository.findByIdAndUserId(bookingId, currentUser.getId())
            .orElseThrow(() -> new ResourceNotFoundException("訂房紀錄不存在"));
    }

    @Transactional
    public Booking createBooking(User currentUser, BookingCreateRequest request) {
        validateDates(request.getCheckIn(), request.getCheckOut());

        Room room = roomService.getRoom(request.getRoomId());
        long nights = ChronoUnit.DAYS.between(request.getCheckIn(), request.getCheckOut());
        BigDecimal totalPrice = room.getPricePerNight().multiply(BigDecimal.valueOf(nights));

        Booking booking = Booking.builder()
            .user(currentUser)
            .room(room)
            .roomName(room.getName())
            .checkIn(request.getCheckIn())
            .checkOut(request.getCheckOut())
            .totalPrice(totalPrice)
            .status(BookingStatus.CONFIRMED)
            .createdAt(LocalDateTime.now())
            .build();

        return bookingRepository.save(booking);
    }

    @Transactional
    public void cancelMyBooking(User currentUser, Long bookingId) {
        Booking booking = getMyBooking(currentUser, bookingId);

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalStateException("此訂房已取消");
        }
        if (!booking.getCheckIn().isAfter(LocalDate.now().plusDays(1))) {
            throw new IllegalStateException("入住前 24 小時內不可取消");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
    }

    private void validateDates(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null) {
            throw new IllegalArgumentException("請填寫入住與退房日期");
        }
        if (!checkIn.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("入住日期必須晚於今天");
        }
        if (!checkOut.isAfter(checkIn)) {
            throw new IllegalArgumentException("退房日期必須晚於入住日期");
        }
    }
}