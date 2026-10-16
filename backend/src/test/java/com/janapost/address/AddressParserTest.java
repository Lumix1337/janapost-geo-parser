package com.janapost.address;

import com.janapost.address.dto.AddressInput;
import com.janapost.address.dto.AddressOutput;
import com.janapost.address.parser.AddressParserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AddressParserTest {

    @Autowired
    private AddressParserService parser;

    @Test
    @DisplayName("Sample from hackathon specifications: Almaty, Aksay 3a")
    void testSampleOutputMatch() {
        AddressInput input = new AddressInput(
            "rec_00417",
            "город Алматы/Алматы қаласы, Мкр Аксай 3а, 59, кв 70"
        );

        AddressOutput out = parser.parse(input);

        assertNotNull(out);
        assertEquals("rec_00417", out.id());
        assertEquals("город Алматы/Алматы қаласы, Мкр Аксай 3а, 59, кв 70", out.raw_address());
        assertEquals("Алматы", out.province());
        assertEquals("Алматы", out.city());
        assertEquals("Ауэзовский", out.district());
        assertNull(out.living_area());
        assertEquals("микрорайон", out.street_type());
        assertEquals("Аксай 3а", out.street_name());
        assertEquals("59", out.house());
        assertEquals("70", out.apartment());
        assertNull(out.entrance());
        assertNull(out.floor());
        assertNull(out.landmark());
        assertTrue(out.confidence() >= 0.85);
    }

    @Test
    @DisplayName("Example with attached numbers: 66квартал 12дом 69квартира")
    void testAttachedNumbers() {
        AddressInput input = new AddressInput(
            "test_attached",
            "66квартал 12дом 69квартира"
        );

        AddressOutput out = parser.parse(input);

        assertNotNull(out);
        assertEquals("квартал", out.street_type());
        assertEquals("66", out.street_name());
        assertEquals("12", out.house());
        assertEquals("69", out.apartment());
    }

    @Test
    @DisplayName("Example with spaced dot: Ул .Курильская 9 кв 1")
    void testSpacedDot() {
        AddressInput input = new AddressInput(
            "test_dot",
            "Ул .Курильская 9 кв 1"
        );

        AddressOutput out = parser.parse(input);

        assertNotNull(out);
        assertEquals("улица", out.street_type());
        assertEquals("Курильская", out.street_name());
        assertEquals("9", out.house());
        assertEquals("1", out.apartment());
    }

    @Test
    @DisplayName("Example with fraction house, landmark, entrance and floor: Туран 55/6 магазин береке 4подьезд 9этаж")
    void testTuranWithLandmarkAndDetails() {
        AddressInput input = new AddressInput(
            "test_turan",
            "Туран 55/6 магазин береке 4подьезд 9этаж"
        );

        AddressOutput out = parser.parse(input);

        assertNotNull(out);
        assertEquals("Туран", out.street_name());
        assertEquals("55/6", out.house());
        assertEquals("4", out.entrance());
        assertEquals("9", out.floor());
        assertEquals("магазин береке", out.landmark());
    }

    @Test
    @DisplayName("Example with explicit district and lane: Турксибский район, Рыбинский переулок, дом 4")
    void testDistrictAndLane() {
        AddressInput input = new AddressInput(
            "test_rybinsky",
            "Турксибский район, Рыбинский переулок, дом 4"
        );

        AddressOutput out = parser.parse(input);

        assertNotNull(out);
        assertEquals("Турксибский", out.district());
        assertEquals("переулок", out.street_type());
        assertEquals("Рыбинский", out.street_name());
        assertEquals("4", out.house());
    }

    @Test
    @DisplayName("Verify batch parsing of 20 diverse Kazakhstan test addresses")
    void test20SampleAddresses() {
        java.util.List<AddressInput> inputs = java.util.List.of(
            new AddressInput("rec_001", "город Алматы/Алматы қаласы, Мкр Аксай 3а, 59, кв 70"),
            new AddressInput("rec_002", "66квартал 12дом 69квартира"),
            new AddressInput("rec_003", "Ул .Курильская 9 кв 1"),
            new AddressInput("rec_004", "Туран 55/6 магазин береке 4подьезд 9этаж"),
            new AddressInput("rec_005", "Турксибский район, Рыбинский переулок, дом 4"),
            new AddressInput("rec_006", "Нурсити 2 микрорайон 11дом 5 подьезд 239 кв"),
            new AddressInput("rec_007", "Кулманова 133"),
            new AddressInput("rec_008", "жуалы 19"),
            new AddressInput("rec_009", "г. Астана, пр. Мангилик Ел 55/2, ЖК Expo Residence, кв 45"),
            new AddressInput("rec_010", "г. Шымкент, Абайский район, ул. Байтурсынова, д. 24, кв. 12"),
            new AddressInput("rec_011", "Караганда, проспект Бухар Жырау 42, ТД Абзал"),
            new AddressInput("rec_012", "г. Актау, 14 мкр, 28 дом, 3 подъезд, 15 кв"),
            new AddressInput("rec_013", "Костанай, ул. Тауелсиздик, 114, рядом с ЦОН"),
            new AddressInput("rec_014", "Павлодар, ул. Торайгырова 77, 2 этаж, комната 14"),
            new AddressInput("rec_015", "г. Семей, ул. Абая 88, магазин Береке, 1 подъезд"),
            new AddressInput("rec_016", "г. Талдыкорган, мкр. Каратал, дом 15, кв 42"),
            new AddressInput("rec_017", "г. Уральск, проспект Назарбаева 182, 4 вход"),
            new AddressInput("rec_018", "Петропавловск, ул. Конституции Казахстана, дом 27, кв 5"),
            new AddressInput("rec_019", "г. Кызылорда, мкр Акмешит, 12 дом, 3 кв"),
            new AddressInput("rec_020", "Атырау, мкр. Авангард 2, дом 10, кв 88")
        );

        java.util.List<AddressOutput> results = parser.parseBatch(inputs);
        assertEquals(20, results.size());
        for (AddressOutput res : results) {
            assertNotNull(res);
            assertNotNull(res.id());
            assertNotNull(res.raw_address());
            assertTrue(res.confidence() >= 0.5, "Confidence should be at least 0.5 for " + res.raw_address());
        }
    }
}
