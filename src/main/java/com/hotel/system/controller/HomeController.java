package com.hotel.system.controller;

import com.hotel.system.entity.User;
import com.hotel.system.service.UserService;
import java.util.Map;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final UserService userService;

    public HomeController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/")
    public String home(@AuthenticationPrincipal OAuth2User principal, Model model) {
        if (principal == null) {
            return "redirect:/login";
        }

        User user = userService.findByOAuth2User(principal).orElse(null);

        model.addAttribute("user", user);
        model.addAttribute("avatarUrl", userService.resolveAvatarUrl(user, principal));
        model.addAttribute("attributes", principal.getAttributes());
        if (!model.containsAttribute("profileSaved")) {
            model.addAttribute("profileSaved", false);
        }
        if (!model.containsAttribute("profileError")) {
            model.addAttribute("profileError", null);
        }
        return "index";
    }

    @GetMapping("/api/me")
    public Map<String, Object> currentUser(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) {
            return Map.of("authenticated", false);
        }

        User user = userService.findByOAuth2User(principal).orElse(null);

        return Map.of(
            "authenticated", true,
            "name", principal.getAttribute("name"),
            "email", principal.getAttribute("email"),
            "role", user != null ? user.getRole().name() : "GUEST"
        );
    }
}