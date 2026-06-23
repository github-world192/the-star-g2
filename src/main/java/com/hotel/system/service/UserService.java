package com.hotel.system.service;

import com.hotel.system.dto.ProfileUpdateRequest;
import com.hotel.system.entity.User;
import com.hotel.system.entity.User.AuthProvider;
import com.hotel.system.repository.UserRepository;
import java.util.Map;
import java.util.Optional;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> findByOAuth2User(OAuth2User principal) {
        if (principal == null) {
            return Optional.empty();
        }

        Map<String, Object> attributes = principal.getAttributes();

        if (attributes.containsKey("sub")) {
            return userRepository.findByProviderAndProviderId(AuthProvider.GOOGLE, (String) attributes.get("sub"));
        }

        String email = principal.getAttribute("email");
        if (email != null) {
            return userRepository.findByEmail(email);
        }

        return Optional.empty();
    }

    public String resolveAvatarUrl(User user, OAuth2User principal) {
        if (user != null && StringUtils.hasText(user.getAvatarUrl())) {
            return user.getAvatarUrl();
        }
        if (principal != null) {
            String picture = principal.getAttribute("picture");
            if (StringUtils.hasText(picture)) {
                return picture;
            }
        }
        return null;
    }

    @Transactional
    public User updateProfile(User user, ProfileUpdateRequest request) {
        user.setName(request.getName().trim());
        user.setPhone(StringUtils.hasText(request.getPhone()) ? request.getPhone().trim() : null);
        user.setAvatarUrl(StringUtils.hasText(request.getAvatarUrl()) ? request.getAvatarUrl().trim() : null);
        return userRepository.save(user);
    }
}