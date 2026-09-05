package com.tripflow;

import com.tripflow.auth.security.JwtProperties;
import com.tripflow.booking.config.BookingProperties;
import com.tripflow.payment.config.PaymentProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties({JwtProperties.class, PaymentProperties.class, BookingProperties.class})
@EnableScheduling
public class TripflowApplication {

	public static void main(String[] args) {
		SpringApplication.run(TripflowApplication.class, args);
	}

}
