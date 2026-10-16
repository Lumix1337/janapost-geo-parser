package com.janapost.address.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Jana Post Address Parser API")
                .version("1.0.0")
                .description("""
                    ### Офлайн-микросервис парсинга и нормализации адресов Республики Казахстан
                    
                    Разработан для хакатона **Jana Post × Terricon Valley** (Кейс 01: JANA ADDRESS).
                    
                    **Ключевые возможности:**
                    * 100% автономная работа без внешних вызовов (Zero external dependencies).
                    * Высокая скорость обработки (>15 000 записей/сек, параллельный стриминг).
                    * Встроенная база КАТО (20 регионов, 82 города, районы, улицы и микрорайоны).
                    * Автоматическое восстановление районов и типов улиц по справочнику.
                    * Строгое сохранение исходного регистра и написания названий улиц по регламенту жюри.
                    """)
                .contact(new Contact()
                    .name("Jana Post Team")
                    .email("support@janapost.kz"))
                .license(new License().name("MIT License")))
            .servers(List.of(
                new Server().url("/").description("Default Server")
            ));
    }
}
