package com.theblood.shopservice.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class InternalAuthenticationFilter extends OncePerRequestFilter {

    @Value("${security.internal.auth.header.username:X-User-Username}")
    private String usernameHeader;

    @Value("${security.internal.auth.header.roles:X-User-Roles}")
    private String rolesHeader;

    @Value("${security.internal.auth.header.authorities:X-User-Authorities}")
    private String authoritiesHeader;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
        throws ServletException, IOException {
        log.info("================ _Services_ - InternalAuthenticationFilter ================");
        String path = request.getRequestURI();

        log.debug("🔍 Filter processing: {}", path);

        // Extract headers
        String username = request.getHeader(usernameHeader);
        String rolesString = request.getHeader(rolesHeader);
        String authoritiesString = request.getHeader(authoritiesHeader);

        // NẾU có headers → set authentication
        if (StringUtils.hasText(username)) {
            log.info("✅ Setting authentication for user: {}", username);
            setAuthentication(username, rolesString, authoritiesString);
        } else {
            log.debug("⚠️ No authentication headers - request will be anonymous");
            // KHÔNG set gì cả - để Spring Security tự handle
            // - Public endpoints: permitAll → pass
            // - Secured endpoints: authenticated → reject với 401
        }

        // LUÔN continue chain - để SecurityConfig quyết định cho qua hay không
        filterChain.doFilter(request, response);
    }

    /**
     * Set authentication vào SecurityContext
     */
    private void setAuthentication(String username, String rolesString, String authoritiesString) {
        List<String> rolesList = parseCommaSeparated(rolesString);
        List<String> authoritiesList = parseCommaSeparated(authoritiesString);
        List<GrantedAuthority> grantedAuthorities = buildAuthorities(rolesList, authoritiesList);

        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(username, null, grantedAuthorities);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        log.debug("✅ SecurityContext set: user={}, authorities={}", username, grantedAuthorities);
    }

    private List<String> parseCommaSeparated(String str) {
        if (!StringUtils.hasText(str)) {
            return Collections.emptyList();
        }
        return Arrays.stream(str.split(","))
            .map(String::trim)
            .filter(StringUtils::hasText)
            .collect(Collectors.toList());
    }

    private List<GrantedAuthority> buildAuthorities(List<String> roles, List<String> authorities) {
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();

        roles.forEach(role -> {
            String authorityName = role.startsWith("ROLE_") ? role : "ROLE_" + role;
            grantedAuthorities.add(new SimpleGrantedAuthority(authorityName));
        });

        authorities.forEach(authority ->
            grantedAuthorities.add(new SimpleGrantedAuthority(authority))
        );

        return grantedAuthorities;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Filter luôn chạy để extract headers (nếu có)
        return false;
    }
}
