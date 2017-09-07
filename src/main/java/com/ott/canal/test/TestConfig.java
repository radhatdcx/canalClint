package com.ott.canal.test;

import com.ott.canal.domain.InitServlet;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.ServletContext;

/**
 * Created by dongcunxiao on 2017/8/30 9:04
 */
@Configuration
public class TestConfig {

    @Bean
    public  ServletRegistrationBean initServlet() {
        System.out.println("=======初始化initServlet==========");
        ServletRegistrationBean registration = new ServletRegistrationBean(new InitServlet(new SqlCallackImpl()));
        registration.setLoadOnStartup(1);
        return registration;
    }

}
