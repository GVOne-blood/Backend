package com.example.demo.Spring.Introduction;

import org.springframework.stereotype.Component;

@Component
public class Code {

    private static AnotherCode anotherCode;

    public Code(AnotherCode anotherCode) {
        this.anotherCode = anotherCode;
    }

    private static BeanDemo beanDemo;

    public static void main(String[] args) {
        anotherCode.main(args);
        beanDemo.show();
        // beanDemo = anotherCode.beanDemo();
        System.out.println("Hello, Spring!");
    }
}
