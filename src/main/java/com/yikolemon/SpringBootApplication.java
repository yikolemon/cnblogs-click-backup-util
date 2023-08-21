package com.yikolemon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author yikolemon
 * @date 2023/7/19 23:33
 * @description
 */

@ComponentScan
@EnableAutoConfiguration
public class SpringBootApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootApplication.class,args);
    }

}
