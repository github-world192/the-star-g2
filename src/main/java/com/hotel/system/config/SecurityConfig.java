package com.hotel.system.config;

import com.hotel.system.service.CustomOAuth2UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final String oauth2RedirectUri;

    public SecurityConfig(
        CustomOAuth2UserService customOAuth2UserService,
        @Value("${app.oauth2.redirect-uri:/callback}") String oauth2RedirectUri
    ) {
        this.customOAuth2UserService = customOAuth2UserService;
        this.oauth2RedirectUri = oauth2RedirectUri;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", oauth2RedirectUri, "/error", "/css/**", "/js/**", "/h2-console/**").permitAll()
                .requestMatchers("/", "/rooms", "/shop", "/my/**").authenticated()
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .redirectionEndpoint(redirection -> redirection.baseUri(oauth2RedirectUri))
                .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                .defaultSuccessUrl("/", true)
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
            );

        http.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));
        http.csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"));

        return http.build();
    }
}