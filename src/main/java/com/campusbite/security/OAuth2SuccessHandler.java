package com.campusbite.security;

import com.campusbite.entity.Student;
import com.campusbite.exception.InvalidDomainException;
import com.campusbite.service.StudentService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final StudentService studentService;

    @Value("${app.college-email-domain}")
    private String collegeDomain;

    public OAuth2SuccessHandler(JwtUtil jwtUtil, StudentService studentService) {
        this.jwtUtil = jwtUtil;
        this.studentService = studentService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        
        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");
        String pictureUrl = oauth2User.getAttribute("picture");

        if (email == null || !email.endsWith(collegeDomain)) {
            response.sendRedirect("/student/login?error=invalid-domain");
            return;
        }

        Student student = studentService.findOrCreateStudent(email, name, pictureUrl);

        String token = jwtUtil.generateToken(student.getEmail(), "ROLE_STUDENT");

        // Redirect to a specific route that will capture this token and store it in localStorage
        response.sendRedirect("/student/menu?token=" + URLEncoder.encode(token, StandardCharsets.UTF_8));
    }
}
