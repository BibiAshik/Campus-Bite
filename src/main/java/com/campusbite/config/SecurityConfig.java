package com.campusbite.config;

import com.campusbite.security.JwtFilter;
import com.campusbite.security.OAuth2SuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    public SecurityConfig(JwtFilter jwtFilter, OAuth2SuccessHandler oAuth2SuccessHandler) {
        this.jwtFilter = jwtFilter;
        this.oAuth2SuccessHandler = oAuth2SuccessHandler;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // APIs are stateless JWT-protected — CSRF tokens are not needed. 
            // Spring Security CSRF would block Razorpay webhook and mobile API calls.
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/admin/login").permitAll() // public — this IS the login endpoint
                .requestMatchers("/api/webhook/**").permitAll() // Razorpay webhook — server-to-server
                .requestMatchers("/admin/login", "/admin/login.html").permitAll() // admin login page itself is public
                .requestMatchers("/student/login", "/student/login.html").permitAll() // student login page is public
                .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll() // Google OAuth2 redirect flow
                .requestMatchers("/images/**", "/uploads/**").permitAll() // food images — no auth needed
                .requestMatchers("/css/**", "/js/**", "/admin/css/**", "/admin/js/**", "/student/css/**", "/student/js/**").permitAll() // static resources
                .requestMatchers("/").permitAll() // Root path redirects to admin login via AuthController
                
                // Allow fetching food items and tracking orders without authentication for students browsing before login
                .requestMatchers("/api/food").permitAll()
                .requestMatchers("/api/orders/track/**").permitAll()

                // Allow ALL HTML pages to load. The frontend JS will enforce auth by checking localStorage and calling APIs
                .requestMatchers("/admin/**", "/student/**").permitAll()

                // Admin specific API endpoints
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                
                // Student specific API endpoints
                .requestMatchers("/api/student/**", "/api/favorites/**").hasRole("STUDENT")
                
                // Other APIs that require authentication (handled by method-level @PreAuthorize)
                .requestMatchers("/api/orders/**", "/api/payments/**").authenticated()
                
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/student/login")
                .successHandler(oAuth2SuccessHandler)
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
