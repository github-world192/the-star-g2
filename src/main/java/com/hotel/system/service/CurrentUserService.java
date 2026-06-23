package com.hotel.system.service;

import com.hotel.system.entity.User;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    private final UserService userService;

    public CurrentUserService(UserService userService) {
        this.userService = userService;
    }

    public User requireCurrentUser(OAuth2User principal) {
        if (principal == null) {
            throw new AccessDeniedException("請先登入");
        }
        return userService.findByOAuth2User(principal)
            .orElseThrow(() -> new AccessDeniedException("使用者不存在"));
    }
}