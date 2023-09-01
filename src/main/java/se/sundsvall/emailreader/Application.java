package se.sundsvall.emailreader;

import static org.springframework.boot.SpringApplication.run;

import org.springframework.cloud.openfeign.EnableFeignClients;

import se.sundsvall.dept44.ServiceApplication;

@ServiceApplication
@EnableFeignClients
public class Application {

    public static void main(final String... args) {
        run(Application.class, args);
    }

}
