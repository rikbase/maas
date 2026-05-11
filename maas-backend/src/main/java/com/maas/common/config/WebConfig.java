package com.maas.common.config;

import com.maas.gateway.config.GatewayAuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final GatewayAuthInterceptor gatewayAuthInterceptor;

    public WebConfig(GatewayAuthInterceptor gatewayAuthInterceptor) {
        this.gatewayAuthInterceptor = gatewayAuthInterceptor;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins("http://localhost:5173")
            .allowedMethods("GET", "POST", "PUT", "DELETE");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(gatewayAuthInterceptor)
            .addPathPatterns("/v1/**");
    }
}
