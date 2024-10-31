package com.example.ilia.aidemo.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // Configure to use snake_case
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        // Register the JavaTimeModule to handle LocalDate and other Java 8 date/time types
        mapper.registerModule(new JavaTimeModule());

        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false); // Ignore empty beans
        // Ensure dates are serialized as strings
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
        return builder -> {
            // Configure Swagger's ObjectMapper to use snake_case as well
            builder.propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
            builder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            builder.modules(new JavaTimeModule());
        };
    }
}
