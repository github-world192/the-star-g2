package com.hotel.system.controller;

import com.hotel.system.dto.ProfileUpdateRequest;
import com.hotel.system.entity.User;
import com.hotel.system.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ProfileController {

    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/profile")
    public String updateProfile(
        @AuthenticationPrincipal OAuth2User principal,
        @Valid ProfileUpdateRequest request,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes
    ) {
        User user = userService.findByOAuth2User(principal).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute(
                "profileError",
                bindingResult.getFieldError() != null
                    ? bindingResult.getFieldError().getDefaultMessage()
                    : "資料格式不正確"
            );
            return "redirect:/";
        }

        userService.updateProfile(user, request);
        redirectAttributes.addFlashAttribute("profileSaved", true);
        return "redirect:/";
    }
}