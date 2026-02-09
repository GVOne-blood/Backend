package com.theblood.authentication.config;

import com.theblood.common.dto.request.CustomUserPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Component
@Slf4j
public class InternalAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String username = request.getHeader("X-User-Username");
        String userId = request.getHeader("X-User-ID");
        String rolesStr = request.getHeader("X-User-Roles");
        String authoritiesStr = request.getHeader("X-User-Authorities");

        // ✅ Only set authentication if username exists
        if (StringUtils.hasText(username)) {
            log.info("✅ Setting authentication for user from Gateway: {}", username);

            CustomUserPrincipal principle = new CustomUserPrincipal();
            principle.setUsername(username);
            principle.setUserId(UUID.fromString(userId));
            List<GrantedAuthority> grantedAuthorities = new ArrayList<>();

            // ✅ Parse roles with null check
            if (StringUtils.hasText(rolesStr)) {
                Arrays.stream(rolesStr.split(","))
                        .map(String::trim)
                        .filter(StringUtils::hasText)
                        .map(SimpleGrantedAuthority::new)
                        .forEach(grantedAuthorities::add);
            }

            // ✅ Parse authorities with null check
            if (StringUtils.hasText(authoritiesStr)) {
                Arrays.stream(authoritiesStr.split(","))
                        .map(String::trim)
                        .filter(StringUtils::hasText)
                        .map(SimpleGrantedAuthority::new)
                        .forEach(grantedAuthorities::add);
            }

            log.debug("User '{}' has {} authorities: {}",
                    username, grantedAuthorities.size(), grantedAuthorities);

            // Create authentication
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(principle, null, grantedAuthorities);

            // Set to SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.debug("✅ SecurityContext set successfully for user: {}", username);

        } else {
            log.debug("⚠️ No X-User-Username header - request will be anonymous");
        }

        // Continue filter chain
        filterChain.doFilter(request, response);
    }
}
