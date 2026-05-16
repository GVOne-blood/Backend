package com.theblood.shopservice;

import com.theblood.springfood.client.api.PaymentClient;
import com.theblood.springfood.client.service.LoggingService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
class ShopServiceApplicationTests {

    @MockBean
    LoggingService loggingService;

    @MockBean
    PaymentClient paymentClient;

    @Test
    void contextLoads() {
    }

}
