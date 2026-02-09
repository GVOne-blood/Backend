package com.theblood.springfood.chat.config;

import com.theblood.common.util.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.Collections;

@Configuration
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class WebSocketAuthConfig implements WebSocketMessageBrokerConfigurer {

    private static final Logger log = LoggerFactory.getLogger(WebSocketAuthConfig.class); // Khai báo thủ công
    private final JwtUtil jwtUtil;

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor =
                    MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                    String token = accessor.getFirstNativeHeader("Authorization");

                    if (token != null && token.startsWith("Bearer ")) {
                        token = token.substring(7);
                        try {
                            jwtUtil.validateToken(token);
                            Claims claims = jwtUtil.extractAllClaims(token);
                            String username = claims.getSubject();

                            Authentication auth = new UsernamePasswordAuthenticationToken(
                                username, null, Collections.emptyList()
                            );

                            SecurityContextHolder.getContext().setAuthentication(auth);
                            accessor.setUser(auth);

                            log.debug("WebSocket authenticated for user: {}", username); // Giờ sẽ hết lỗi

                        } catch (Exception e) {
                            log.error("WebSocket Authentication failed: {}", e.getMessage());
                            return null;
                        }
                    } else {
                        log.warn("WebSocket connection attempt without valid Authorization header");
                    }
                }
                return message;
            }
        });
    }
}
