package io.fabric8.quickstarts.camel;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component
@Configuration
@EnableConfigurationProperties
public class YamlConfig {

    @Value("${quickstart.generateOrderPeriod}")
    private String generateOrderPeriod;


    public String getGenerateOrderPeriod() {
        return generateOrderPeriod;
    }
}