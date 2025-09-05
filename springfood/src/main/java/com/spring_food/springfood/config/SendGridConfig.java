package com.spring_food.springfood.config;

import com.sendgrid.SendGrid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

public class SendGridConfig {

    @Value("${spring.sendgrid.api-key}")
    private String sendgridApiKey;
    @Bean
    public SendGrid sendGrid(){
        return new SendGrid(sendgridApiKey);
    }

}
