package com.example.demo.Spring.Introduction;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class AnotherCode {
    public static void main(String[] args) {
        System.out.println("Hello, Another Spring!");
    }

    @Bean
    public BeanDemo beanDemo() {
        return new BeanDemo();
    }
}

class BeanDemo {
    public void show() {
        System.out.println("Hello, BeanDemo!");
    }

}
