package vip.yeee.app.bootstrap;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@MapperScan({"vip.yeee.app"})
@ComponentScan({"vip.yeee.app"})
public class YeeeSpringbootVueBootstrapApplication {

    public static void main(String[] args) {
        SpringApplication.run(YeeeSpringbootVueBootstrapApplication.class, args);
    }

}
