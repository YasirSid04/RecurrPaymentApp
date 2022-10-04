package com.example.config;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
@Configuration
public class WebMVCConfigure implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")  //append any URL Pattern
                .allowedOrigins("http://localhost:3000/")//request coming from port no 3000 which is in the client part(Front End)
                .allowedMethods("PUT", "DELETE","GET", "POST"). //operations
                allowedHeaders("*");    //all headers is allowed to access this backend
    }

}


