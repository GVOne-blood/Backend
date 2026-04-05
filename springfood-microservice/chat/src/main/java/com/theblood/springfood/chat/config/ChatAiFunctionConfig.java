package com.theblood.springfood.chat.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.function.Function;

@Configuration
public class ChatAiFunctionConfig {

    private final Logger log = LoggerFactory.getLogger(ChatAiFunctionConfig.class);

    /**
     * Ví dụ Cấu hình Function Calling cho Spring AI.
     * Khi user trò chuyện với AI và hỏi về thông tin mà model không biết (ví dụ trạng thái một đơn hàng),
     * GenAI model sẽ sử dụng function này để lấy thông tin.
     */
    @Bean
    @Description("Lấy tình trạng của một giao dịch hoặc đơn hàng dựa trên mã ID")
    public Function<OrderStatusRequest, OrderStatusResponse> checkOrderStatusFunction() {
        return request -> {
            log.info("AI gọi function lấy tình trạng đơn hàng cho ID: {}", request.orderId());
            // TODO: Tại đây bạn sẽ gọi gRPC sang Order service, hoặc repository query sang DB
            return new OrderStatusResponse(
                    request.orderId(),
                    "Đang chuẩn bị",
                    "Đơn hàng đang được nhà hàng chuẩn bị nhanh chóng"
            );
        };
    }

    // Các record đóng vai trò là DTO cho việc gọi Function Calling
    public record OrderStatusRequest(String orderId) {}
    public record OrderStatusResponse(String orderId, String status, String description) {}
}
