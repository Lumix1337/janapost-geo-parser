package com.example.myapplication.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SmartAddressParserTest {

    @Test
    fun parsesOfficialSampleAddress() {
        val input = "город Алматы/Алматы қаласы, Мкр Аксай 3а, 59, кв 70"
        val res = SmartAddressParser.parse(input)

        assertNotNull(res)
        assertEquals("город Алматы/Алматы қаласы, Мкр Аксай 3а, 59, кв 70", res.rawAddress)
        assertEquals("Алматы", res.city)
        assertEquals("Алматы", res.province)
        assertEquals("Ауэзовский", res.district)
        assertEquals("микрорайон", res.streetType)
        assertEquals("Аксай 3а", res.streetName)
        assertEquals("59", res.house)
        assertEquals("70", res.apartment)
        assertNull(res.entrance)
        assertNull(res.floor)
        assertNull(res.landmark)
        assertTrue("Confidence should be >= 0.85, actual: ${res.confidence}", res.confidence >= 0.85)
    }

    @Test
    fun parsesGluedTokens() {
        val input = "66квартал 12дом 69квартира"
        val res = SmartAddressParser.parse(input)

        assertNotNull(res)
        assertEquals("квартал", res.streetType)
        assertEquals("66", res.streetName)
        assertEquals("12", res.house)
        assertEquals("69", res.apartment)
    }

    @Test
    fun parsesSpacedDotAndStreet() {
        val input = "Ул .Курильская 9 кв 1"
        val res = SmartAddressParser.parse(input)

        assertNotNull(res)
        assertEquals("улица", res.streetType)
        assertEquals("Курильская", res.streetName)
        assertEquals("9", res.house)
        assertEquals("1", res.apartment)
    }

    @Test
    fun parsesTuranFractionalHouseWithLandmarkAndFloor() {
        val input = "Туран 55/6 магазин береке 4подьезд 9этаж"
        val res = SmartAddressParser.parse(input)

        assertNotNull(res)
        assertEquals("Туран", res.streetName)
        assertEquals("55/6", res.house)
        assertEquals("4", res.entrance)
        assertEquals("9", res.floor)
        assertEquals("магазин береке", res.landmark)
    }

    @Test
    fun parsesAstanaAndAvenue() {
        val input = "г. Астана, пр. Мангилик Ел 55/2, кв 45"
        val res = SmartAddressParser.parse(input)

        assertNotNull(res)
        assertEquals("Астана", res.city)
        assertEquals("проспект", res.streetType)
        assertEquals("Мангилик Ел", res.streetName)
        assertEquals("55/2", res.house)
        assertEquals("45", res.apartment)
    }

    @Test
    fun handlesBlankAndEmptyInputGracefully() {
        val empty = SmartAddressParser.parse("")
        assertEquals("", empty.rawAddress)
        assertEquals(0.0, empty.confidence, 0.001)

        val blank = SmartAddressParser.parse("   ")
        assertEquals(0.0, blank.confidence, 0.001)
    }
}
