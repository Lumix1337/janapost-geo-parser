package com.janapost.address;

import com.janapost.address.dto.AddressInput;
import com.janapost.address.dto.AddressOutput;
import com.janapost.address.parser.AddressParserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BenchmarkTest {

    private static final Logger log = LoggerFactory.getLogger(BenchmarkTest.class);

    @Autowired
    private AddressParserService parser;

    @Test
    @DisplayName("Performance benchmark: 1000 records processed in under 5 seconds (Jury requirement <= 300s)")
    void test1000RecordsSpeed() {
        String[] samples = {
            "город Алматы/Алматы қаласы, Мкр Аксай 3а, 59, кв 70",
            "66квартал 12дом 69квартира",
            "Ул .Курильская 9 кв 1",
            "Туран 55/6 магазин береке 4подьезд 9этаж",
            "Турксибский район, Рыбинский переулок, дом 4",
            "Нурсити 2 микрорайон 11дом 5 подьезд 239 кв",
            "город Астана, проспект Мангилик Ел 28, кв 15",
            "город Шымкент, мкр Нурсат 15, кв 45",
            "Кулманова 133",
            "жуалы 19"
        };

        List<AddressInput> inputs = new ArrayList<>(1000);
        for (int i = 0; i < 1000; i++) {
            inputs.add(new AddressInput("rec_" + i, samples[i % samples.length]));
        }

        // Warm up JIT
        parser.parseBatch(inputs.subList(0, 50));

        long start = System.currentTimeMillis();
        List<AddressOutput> results = parser.parseBatch(inputs);
        long elapsed = System.currentTimeMillis() - start;

        log.info(">>> 1000 RECORDS PARSED IN: {} ms ({} seconds)", elapsed, (elapsed / 1000.0));
        System.out.println(">>> 1000 RECORDS PARSED IN: " + elapsed + " ms (" + (elapsed / 1000.0) + " seconds)");

        assertEquals(1000, results.size());
        assertTrue(elapsed < 5000, "1000 records must be processed in under 5 seconds, actual: " + elapsed + " ms");
    }
}
