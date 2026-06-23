package com.hotel.system.controller;

import com.hotel.system.entity.User;
import com.hotel.system.service.BookingService;
import com.hotel.system.service.CurrentUserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class MyBookingController {

    private final BookingService bookingService;
    private final CurrentUserService currentUserService;

    public MyBookingController(BookingService bookingService, CurrentUserService currentUserService) {
        this.bookingService = bookingService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/my/bookings")
    public String myBookings(@AuthenticationPrincipal OAuth2User principal, Model model) {
        User user = currentUserService.requireCurrentUser(principal);
        model.addAttribute("bookings", bookingService.getMyBookings(user));
        return "my-bookings";
    }

    @GetMapping("/my/bookings/{id}")
    public String bookingDetail(
        @AuthenticationPrincipal OAuth2User principal,
        @PathVariable Long id,
        Model model
    ) {
        User user = currentUserService.requireCurrentUser(principal);
        model.addAttribute("booking", bookingService.getMyBooking(user, id));
        return "booking-detail";
    }

    @PostMapping("/my/bookings/{id}/cancel")
    public String cancelBooking(
        @AuthenticationPrincipal OAuth2User principal,
        @PathVariable Long id,
        RedirectAttributes redirectAttributes
    ) {
        User user = currentUserService.requireCurrentUser(principal);

        try {
            bookingService.cancelMyBooking(user, id);
            redirectAttributes.addFlashAttribute("successMessage", "訂房已取消");
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/my/bookings/" + id;
    }
}