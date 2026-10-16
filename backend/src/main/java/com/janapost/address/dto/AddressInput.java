package com.janapost.address.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Входная запись для разбора адреса")
public record AddressInput(
    @Schema(description = "Уникальный идентификатор записи", example = "rec_00417")
    String id,

    @Schema(description = "Сырая неструктурированная строка адреса", example = "город Алматы/Алматы қаласы, Мкр Аксай 3а, 59, кв 70")
    String raw_address
) {}
