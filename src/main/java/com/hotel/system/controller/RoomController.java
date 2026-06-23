package com.hotel.system.controller;

import com.hotel.system.dto.BookingCreateRequest;
import com.hotel.system.entity.User;
import com.hotel.system.service.BookingService;
import com.hotel.system.service.CurrentUserService;
import com.hotel.system.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class RoomController {

    private final RoomService roomService;
    private final BookingService bookingService;
    private final CurrentUserService currentUserService;

    public RoomController(
        RoomService roomService,
        BookingService bookingService,
        CurrentUserService currentUserService
    ) {
        this.roomService = roomService;
        this.bookingService = bookingService;
        this.currentUserService = currentUserService;
    }

    @ModelAttribute("bookingRequest")
    public BookingCreateRequest bookingRequest() {
        return new BookingCreateRequest();
    }

    @GetMapping("/rooms")
    public String listRooms(Model model) {
        model.addAttribute("rooms", roomService.listRooms());
        return "rooms";
    }

    @PostMapping("/rooms/book")
    public String createBooking(
        @AuthenticationPrincipal OAuth2User principal,
        @Valid @ModelAttribute("bookingRequest") BookingCreateRequest request,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("rooms", roomService.listRooms());
            return "rooms";
        }

        User user = currentUserService.requireCurrentUser(principal);

        try {
            var booking = bookingService.createBooking(user, request);
            redirectAttributes.addFlashAttribute("successMessage", "訂房成功！訂單編號：" + booking.getId());
            return "redirect:/my/bookings/" + booking.getId();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("rooms", roomService.listRooms());
            return "rooms";
        }
    }
}