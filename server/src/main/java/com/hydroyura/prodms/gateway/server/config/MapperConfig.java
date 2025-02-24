package com.hydroyura.prodms.gateway.server.config;

import com.hydroyura.prodms.gateway.server.mapper.GetUnitResToGetUnitDetailResMapper;
import com.hydroyura.prodms.gateway.server.mapper.GetUnitResToGetUnitDetailResMapperImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MapperConfig {

    @Bean
    GetUnitResToGetUnitDetailResMapper getUnitResToGetUnitDetailResMapper() {
        return new GetUnitResToGetUnitDetailResMapperImpl();
    }

}
