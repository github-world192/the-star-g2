package com.hotel.system.service;

import com.hotel.system.entity.User;
import com.hotel.system.entity.User.AuthProvider;
import com.hotel.system.entity.User.UserRole;
import com.hotel.system.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        AuthProvider provider = resolveProvider(registrationId);
        Map<String, Object> attributes = oauth2User.getAttributes();

        String providerId = extractProviderId(provider, attributes);
        String email = extractEmail(provider, attributes, providerId);
        String name = extractName(provider, attributes);
        String avatarUrl = (String) attributes.get("picture");

        userRepository.findByProviderAndProviderId(provider, providerId)
            .map(existing -> {
                if (existing.getAvatarUrl() == null && avatarUrl != null) {
                    existing.setAvatarUrl(avatarUrl);
                    return userRepository.save(existing);
                }
                return existing;
            })
            .orElseGet(() -> userRepository.save(
                User.builder()
                    .email(email)
                    .name(name)
                    .avatarUrl(avatarUrl)
                    .provider(provider)
                    .providerId(providerId)
                    .role(UserRole.GUEST)
                    .createdAt(LocalDateTime.now())
                    .build()
            ));

        return oauth2User;
    }

    private AuthProvider resolveProvider(String registrationId) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> AuthProvider.GOOGLE;
            default -> throw new OAuth2AuthenticationException("Unsupported provider: " + registrationId);
        };
    }

    private String extractProviderId(AuthProvider provider, Map<String, Object> attributes) {
        return (String) attributes.get("sub");
    }

    private String extractEmail(AuthProvider provider, Map<String, Object> attributes, String providerId) {
        Object email = attributes.get("email");
        if (email != null) {
            return email.toString();
        }
        return provider.name().toLowerCase() + "_" + providerId + "@oauth.local";
    }

    private String extractName(AuthProvider provider, Map<String, Object> attributes) {
        String name = (String) attributes.get("name");
        return name != null ? name : "Google User";
    }
}