package com.hotel.system.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hotel.system.dto.BookingCreateRequest;
import com.hotel.system.entity.Booking.BookingStatus;
import com.hotel.system.entity.Room;
import com.hotel.system.entity.User;
import com.hotel.system.entity.User.AuthProvider;
import com.hotel.system.entity.User.UserRole;
import com.hotel.system.exception.ResourceNotFoundException;
import com.hotel.system.repository.RoomRepository;
import com.hotel.system.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BookingServiceTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoomRepository roomRepository;

    private User userA;
    private User userB;
    private Room room;

    @BeforeEach
    void setUp() {
        userA = userRepository.save(User.builder()
            .email("user-a@test.com")
            .name("User A")
            .provider(AuthProvider.GOOGLE)
            .providerId("sub-a")
            .role(UserRole.GUEST)
            .createdAt(LocalDateTime.now())
            .build());

        userB = userRepository.save(User.builder()
            .email("user-b@test.com")
            .name("User B")
            .provider(AuthProvider.GOOGLE)
            .providerId("sub-b")
            .role(UserRole.GUEST)
            .createdAt(LocalDateTime.now())
            .build());

        room = roomRepository.save(Room.builder()
            .name("測試房型")
            .description("測試用")
            .pricePerNight(new BigDecimal("1000"))
            .capacity(2)
            .build());
    }

    @Test
    void createAndGetMyBooking() {
        BookingCreateRequest request = new BookingCreateRequest();
        request.setRoomId(room.getId());
        request.setCheckIn(LocalDate.now().plusDays(2));
        request.setCheckOut(LocalDate.now().plusDays(4));

        var booking = bookingService.createBooking(userA, request);

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        assertThat(booking.getTotalPrice()).isEqualByComparingTo("2000");
        assertThat(bookingService.getMyBooking(userA, booking.getId()).getRoomName()).isEqualTo("測試房型");
    }

    @Test
    void cannotAccessOtherUsersBooking() {
        BookingCreateRequest request = new BookingCreateRequest();
        request.setRoomId(room.getId());
        request.setCheckIn(LocalDate.now().plusDays(2));
        request.setCheckOut(LocalDate.now().plusDays(3));

        var booking = bookingService.createBooking(userA, request);

        assertThatThrownBy(() -> bookingService.getMyBooking(userB, booking.getId()))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void cancelMyBooking() {
        BookingCreateRequest request = new BookingCreateRequest();
        request.setRoomId(room.getId());
        request.setCheckIn(LocalDate.now().plusDays(3));
        request.setCheckOut(LocalDate.now().plusDays(4));

        var booking = bookingService.createBooking(userA, request);
        bookingService.cancelMyBooking(userA, booking.getId());

        assertThat(bookingService.getMyBooking(userA, booking.getId()).getStatus())
            .isEqualTo(BookingStatus.CANCELLED);
    }
}