package com.beyond.ordersystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class OrdersystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(OrdersystemApplication.class, args);
	}

}
