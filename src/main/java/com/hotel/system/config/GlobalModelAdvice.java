package com.hotel.system.config;

import com.hotel.system.entity.User;
import com.hotel.system.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.ui.Model;

@ControllerAdvice
public class GlobalModelAdvice {

    private final UserService userService;

    public GlobalModelAdvice(UserService userService) {
        this.userService = userService;
    }

    @ModelAttribute
    public void addUserAttributes(@AuthenticationPrincipal OAuth2User principal, Model model) {
        if (principal == null) {
            return;
        }

        User user = userService.findByOAuth2User(principal).orElse(null);

        if (!model.containsAttribute("user")) {
            model.addAttribute("user", user);
        }
        if (!model.containsAttribute("avatarUrl")) {
            model.addAttribute("avatarUrl", userService.resolveAvatarUrl(user, principal));
        }
    }
}