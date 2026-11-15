package com.escapehub.escapehub.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@Profile("dev")
public class DevelopmentCurrentUserFilter extends OncePerRequestFilter {

    public static final String HEADER_NAME = "X-Dev-User-Id";

    public static final String CURRENT_USER_ID_ATTRIBUTE = "currentUserId";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String userIdHeader = request.getHeader(HEADER_NAME);

        // A request without a development user can still reach
        // endpoints that do not require a current user.
        if (userIdHeader != null) {

            try {
                UUID userId = UUID.fromString(userIdHeader);

                request.setAttribute(
                        CURRENT_USER_ID_ATTRIBUTE,
                        userId
                );

            } catch (IllegalArgumentException exception) {

                response.sendError(
                        HttpServletResponse.SC_BAD_REQUEST,
                        HEADER_NAME + " must contain a valid UUID."
                );

                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
