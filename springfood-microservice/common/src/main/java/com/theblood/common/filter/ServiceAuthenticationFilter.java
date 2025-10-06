package com.theblood.common.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ServiceAuthenticationFilter extends OncePerRequestFilter {

    private static final String USERNAME_HEADER = "X-User-Username";
    private static final String ROLES_HEADER = "X-User-Roles";

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        // If context is already populated by a previous filter, skip
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        String username = request.getHeader(USERNAME_HEADER);
        String roles = request.getHeader(ROLES_HEADER);

        if (StringUtils.hasText(username)) {
            log.debug("Found user '{}' in headers. Populating SecurityContext.", username);

            List<SimpleGrantedAuthority> authorities = Arrays.stream(roles.replace("[", "").replace("]", "").split(","))
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    username, null, authorities
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
        } else {
            log.debug("No user found in headers. Skipping SecurityContext population.");
        }

        filterChain.doFilter(request, response);
    }
}
