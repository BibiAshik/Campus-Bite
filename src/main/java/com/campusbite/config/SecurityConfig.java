package com.campusbite.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable) // Disabled for simplicity in beginner project
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/admin/**", "/api/admin/**", "/admin-dashboard.html", "/admin-menu.html").hasRole("ADMIN")
                .anyRequest().permitAll() // Allow students to browse and order without login
            )
            .formLogin(form -> form
                .loginPage("/index.html")
                .loginProcessingUrl("/api/login")
                .defaultSuccessUrl("/admin-dashboard.html", true)
                .failureUrl("/index.html?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/api/logout"))
                .logoutSuccessUrl("/")
                .permitAll()
            );

        return http.build();
    }
}
