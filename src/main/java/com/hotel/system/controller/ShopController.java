package com.hotel.system.controller;

import com.hotel.system.dto.OrderCreateRequest;
import com.hotel.system.entity.User;
import com.hotel.system.service.CurrentUserService;
import com.hotel.system.service.OrderService;
import com.hotel.system.service.ProductService;
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
public class ShopController {

    private final ProductService productService;
    private final OrderService orderService;
    private final CurrentUserService currentUserService;

    public ShopController(
        ProductService productService,
        OrderService orderService,
        CurrentUserService currentUserService
    ) {
        this.productService = productService;
        this.orderService = orderService;
        this.currentUserService = currentUserService;
    }

    @ModelAttribute("orderRequest")
    public OrderCreateRequest orderRequest() {
        return new OrderCreateRequest();
    }

    @GetMapping("/shop")
    public String listProducts(Model model) {
        model.addAttribute("products", productService.listProducts());
        return "shop";
    }

    @PostMapping("/shop/order")
    public String createOrder(
        @AuthenticationPrincipal OAuth2User principal,
        @Valid @ModelAttribute("orderRequest") OrderCreateRequest request,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("products", productService.listProducts());
            return "shop";
        }

        User user = currentUserService.requireCurrentUser(principal);

        try {
            var order = orderService.createOrder(user, request);
            redirectAttributes.addFlashAttribute("successMessage", "購買成功！訂單編號：" + order.getId());
            return "redirect:/my/orders/" + order.getId();
        } catch (IllegalStateException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("products", productService.listProducts());
            return "shop";
        }
    }
}