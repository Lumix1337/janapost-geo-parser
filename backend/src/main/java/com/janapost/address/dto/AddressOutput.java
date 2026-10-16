package com.janapost.address.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.ALWAYS)
@Schema(description = "Структурированный результат разбора адреса по регламенту жюри")
public record AddressOutput(
    @Schema(description = "Идентификатор записи", example = "rec_00417")
    String id,

    @Schema(description = "Исходный неформатированный адрес", example = "город Алматы/Алматы қаласы, Мкр Аксай 3а, 59, кв 70")
    String raw_address,

    @Schema(description = "Область / город республиканского значения", example = "Алматы")
    String province,

    @Schema(description = "Город или населенный пункт", example = "Алматы")
    String city,

    @Schema(description = "Район города (автовосстановление по КАТО)", example = "Ауэзовский")
    String district,

    @Schema(description = "Жилой комплекс или массив", example = "null", nullable = true)
    String living_area,

    @Schema(description = "Тип улицы", example = "микрорайон")
    String street_type,

    @Schema(description = "Название улицы (в исходном регистре)", example = "Аксай 3а")
    String street_name,

    @Schema(description = "Номер дома со всеми литерами и дробями", example = "59")
    String house,

    @Schema(description = "Номер квартиры", example = "70")
    String apartment,

    @Schema(description = "Номер подъезда", example = "null", nullable = true)
    String entrance,

    @Schema(description = "Этаж", example = "null", nullable = true)
    String floor,

    @Schema(description = "Ориентир", example = "null", nullable = true)
    String landmark,

    @Schema(description = "Уверенность распознавания от 0.0 до 1.0", example = "0.91")
    double confidence
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String rawAddress;
        private String province;
        private String city;
        private String district;
        private String livingArea;
        private String streetType;
        private String streetName;
        private String house;
        private String apartment;
        private String entrance;
        private String floor;
        private String landmark;
        private double confidence;

        public Builder id(String id) { this.id = id; return this; }
        public Builder rawAddress(String rawAddress) { this.rawAddress = rawAddress; return this; }
        public Builder province(String province) { this.province = province; return this; }
        public Builder city(String city) { this.city = city; return this; }
        public Builder district(String district) { this.district = district; return this; }
        public Builder livingArea(String livingArea) { this.livingArea = livingArea; return this; }
        public Builder streetType(String streetType) { this.streetType = streetType; return this; }
        public Builder streetName(String streetName) { this.streetName = streetName; return this; }
        public Builder house(String house) { this.house = house; return this; }
        public Builder apartment(String apartment) { this.apartment = apartment; return this; }
        public Builder entrance(String entrance) { this.entrance = entrance; return this; }
        public Builder floor(String floor) { this.floor = floor; return this; }
        public Builder landmark(String landmark) { this.landmark = landmark; return this; }
        public Builder confidence(double confidence) { this.confidence = confidence; return this; }

        public AddressOutput build() {
            return new AddressOutput(
                id,
                rawAddress,
                province,
                city,
                district,
                livingArea,
                streetType,
                streetName,
                house,
                apartment,
                entrance,
                floor,
                landmark,
                Math.round(confidence * 100.0) / 100.0
            );
        }
    }
}
