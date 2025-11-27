package com.theblood.orderservice.background.job;


import com.theblood.orderservice.repository.OrderItemRepository;
import com.theblood.orderservice.repository.OrderRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DeleteExpiredOrderCronjob {

    OrderRepository orderRepository;
    OrderItemRepository orderItemRepository;

    @Scheduled(cron = "0 0 */6 * * *")
    public void deleteExpiredPaymentPendingOrder(){

    }

}
