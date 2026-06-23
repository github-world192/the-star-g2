package com.hotel.system.controller;

import com.hotel.system.entity.User;
import com.hotel.system.service.CurrentUserService;
import com.hotel.system.service.OrderService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class MyOrderController {

    private final OrderService orderService;
    private final CurrentUserService currentUserService;

    public MyOrderController(OrderService orderService, CurrentUserService currentUserService) {
        this.orderService = orderService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/my/orders")
    public String myOrders(@AuthenticationPrincipal OAuth2User principal, Model model) {
        User user = currentUserService.requireCurrentUser(principal);
        model.addAttribute("orders", orderService.getMyOrders(user));
        return "my-orders";
    }

    @GetMapping("/my/orders/{id}")
    public String orderDetail(
        @AuthenticationPrincipal OAuth2User principal,
        @PathVariable Long id,
        Model model
    ) {
        User user = currentUserService.requireCurrentUser(principal);
        model.addAttribute("order", orderService.getMyOrder(user, id));
        return "order-detail";
    }

    @PostMapping("/my/orders/{id}/cancel")
    public String cancelOrder(
        @AuthenticationPrincipal OAuth2User principal,
        @PathVariable Long id,
        RedirectAttributes redirectAttributes
    ) {
        User user = currentUserService.requireCurrentUser(principal);

        try {
            orderService.cancelMyOrder(user, id);
            redirectAttributes.addFlashAttribute("successMessage", "訂單已取消");
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/my/orders/" + id;
    }
}