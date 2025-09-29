package com.spring_food.springfood.config;

import com.spring_food.springfood.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String[] WHITE_LIST = {
            "/api/auth/**",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/webjars/**",
            "/actuator/**",
    };
    //    private final ExceptionHandlingFilter exceptionHandlingFilter;
    private final PreFilter preFilter;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
//    private final CustomAccessDeniedHandler customAccessDeniedHandler;
//    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//                .exceptionHandling(exceptions -> exceptions
//                        .authenticationEntryPoint(customAuthenticationEntryPoint)
//                        .accessDeniedHandler(customAccessDeniedHandler))

                //
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(WHITE_LIST).permitAll()
                        // Product endpoints - Phân quyền theo Permission
                        .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll() // Tất cả có thể xem (hoặc hasAuthority("VIEW_PRODUCT"))
                        .requestMatchers(HttpMethod.POST, "/api/products/**").hasAuthority("CREATE_PRODUCT") // Cần permission CREATE_PRODUCT
                        .requestMatchers(HttpMethod.PUT, "/api/products/**").hasAuthority("UPDATE_PRODUCT") // Cần permission UPDATE_PRODUCT
                        .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasAuthority("DELETE_PRODUCT") // Cần permission DELETE_PRODUCT

                        // Category endpoints - Phân quyền cho danh mục
                        .requestMatchers(HttpMethod.GET, "/api/categories/**").permitAll() // Tất cả có thể xem danh mục
                        .requestMatchers(HttpMethod.POST, "/api/categories/**").hasRole("ADMIN") // Chỉ ADMIN tạo danh mục
                        .requestMatchers(HttpMethod.PUT, "/api/categories/**").hasRole("ADMIN") // Chỉ ADMIN sửa danh mục
                        .requestMatchers(HttpMethod.DELETE, "/api/categories/**").hasRole("ADMIN") // Chỉ ADMIN xóa danh mục

                        .requestMatchers(HttpMethod.GET, "/api/users/search").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/users/").hasAuthority("MANAGE_USERS") // Cần permission MANAGE_USERS
                        .requestMatchers(HttpMethod.GET, "/api/users/profile").authenticated() // User xem profile của mình
                        .requestMatchers(HttpMethod.GET, "/api/users/{id}").hasAuthority("MANAGE_USERS") // Cần permission MANAGE_USERS
                        .requestMatchers(HttpMethod.PUT, "/api/users/profile").authenticated() // User cập nhật profile của mình
                        .requestMatchers(HttpMethod.DELETE, "/api/users/{id}").hasAuthority("MANAGE_USERS") // Cần permission MANAGE_USERS
                        .requestMatchers(HttpMethod.POST, "/api/orders/**").hasAuthority("CREATE_ORDER") // Cần permission CREATE_ORDER
                        .requestMatchers(HttpMethod.GET, "/api/orders/my-orders").hasAuthority("VIEW_ORDERS") // Cần permission VIEW_ORDERS
                        .requestMatchers(HttpMethod.GET, "/api/orders/**").hasAuthority("MANAGE_ORDERS") // Cần permission MANAGE_ORDERS
                        .requestMatchers(HttpMethod.PUT, "/api/orders/{id}/status").hasAuthority("MANAGE_ORDERS") // Cần permission MANAGE_ORDERS
                        .requestMatchers(HttpMethod.PUT, "/api/orders/{id}/cancel").hasAnyAuthority("CREATE_ORDER", "MANAGE_ORDERS") // Customer hoặc Admin
                        .requestMatchers(HttpMethod.DELETE, "/api/orders/**").hasAuthority("MANAGE_ORDERS") // Cần permission MANAGE_ORDERS

                        // Shop management - Quản lý cửa hàng
                        .requestMatchers(HttpMethod.GET, "/api/shops/**").permitAll() // Tất cả xem thông tin shop
                        .requestMatchers(HttpMethod.POST, "/api/shops/**").hasRole("ADMIN") // Chỉ Admin tạo shop
                        .requestMatchers(HttpMethod.PUT, "/api/shops/**").hasRole("ADMIN") // Chỉ Admin sửa shop
                        .requestMatchers(HttpMethod.DELETE, "/api/shops/**").hasRole("ADMIN") // Chỉ Admin xóa shop

                        // Feedback endpoints
                        .requestMatchers(HttpMethod.POST, "/api/feedbacks/**").hasRole("CUSTOMER") // Customer tạo feedback
                        .requestMatchers(HttpMethod.GET, "/api/feedbacks/**").permitAll() // Tất cả xem feedback
                        .requestMatchers(HttpMethod.DELETE, "/api/feedbacks/**").hasRole("ADMIN") // Admin xóa feedback

                        // Notification endpoints
                        .requestMatchers(HttpMethod.GET, "/api/notifications/my-notifications").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/notifications/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/notifications/{id}/read").authenticated()

                        // Wallet endpoints
                        .requestMatchers("/api/wallets/**").hasRole("ADMIN")

                        // Bank Account endpoints
                        .requestMatchers("/api/bank-accounts/my-accounts").hasRole("CUSTOMER")
                        .requestMatchers("/api/bank-accounts/**").hasRole("ADMIN")

                        // Address endpoints
                        .requestMatchers(HttpMethod.GET, "/api/addresses/my-addresses").hasRole("CUSTOMER")
                        .requestMatchers(HttpMethod.POST, "/api/addresses/**").hasRole("CUSTOMER")
                        .requestMatchers(HttpMethod.PUT, "/api/addresses/**").hasRole("CUSTOMER")
                        .requestMatchers(HttpMethod.DELETE, "/api/addresses/**").hasRole("CUSTOMER")

                        // Post endpoints (Blog/Articles)
                        .requestMatchers(HttpMethod.GET, "/api/posts/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/posts/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/posts/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/posts/**").hasRole("ADMIN")

                        // Sale/Promotion endpoints
                        .requestMatchers(HttpMethod.GET, "/api/sales/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/sales/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/sales/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/sales/**").hasRole("ADMIN")

                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(preFilter, UsernamePasswordAuthenticationFilter.class);
        // thêm bố lên đầu chuỗi thức ăn
        //.addFilterBefore(exceptionHandlingFilter, PreFilter.class);
        // bỏ bố đi bố phế vc
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:8081", "http://localhost:3000"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userService.userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
