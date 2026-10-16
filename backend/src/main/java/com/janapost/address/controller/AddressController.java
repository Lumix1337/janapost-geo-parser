package com.janapost.address.controller;

import com.janapost.address.dto.AddressInput;
import com.janapost.address.dto.AddressOutput;
import com.janapost.address.parser.AddressParserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
@Tag(name = "Address Parser API", description = "Методы разбора, структурирования и проверки адресов РК")
public class AddressController {

    private final AddressParserService parserService;

    public AddressController(AddressParserService parserService) {
        this.parserService = parserService;
    }

    @Operation(
        summary = "Разбор одиночного адреса",
        description = "Принимает сырую строку адреса и возвращает строго типизированный объект с выделенными компонентами (область, город, район, улица, дом, квартира и т.д.)."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Успешный разбор адреса",
            content = @Content(schema = @Schema(implementation = AddressOutput.class))
        )
    })
    @PostMapping("/parse")
    public ResponseEntity<AddressOutput> parseSingle(@RequestBody AddressInput input) {
        return ResponseEntity.ok(parserService.parse(input));
    }

    @Operation(
        summary = "Пакетная обработка адресов (Batch Processing)",
        description = "Высокопроизводительный параллельный разбор списка адресов (до 10 000+ записей). Поддерживает мультипоточность и автономную работу."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Список структурированных результатов",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = AddressOutput.class)))
        )
    })
    @PostMapping("/batch")
    public ResponseEntity<List<AddressOutput>> parseBatch(@RequestBody List<AddressInput> inputs) {
        return ResponseEntity.ok(parserService.parseBatch(inputs));
    }

    @Operation(
        summary = "Проверка состояния сервиса (Health Check)",
        description = "Возвращает текущий статус сервиса, название и версию микросервиса."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Сервис доступен и готов к обработке")
    })
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "jana-address",
            "version", "1.0.0"
        ));
    }
}
