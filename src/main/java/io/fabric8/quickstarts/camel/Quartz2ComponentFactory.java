package io.fabric8.quickstarts.camel;

import org.apache.camel.component.quartz2.QuartzComponent;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class Quartz2ComponentFactory {

    @Bean(name = "quartz2")
    public QuartzComponent createJmsComponent() {

        QuartzComponent component = new QuartzComponent();
        component.setPropertiesFile("quartz.properties");

        return component;
    }
}

