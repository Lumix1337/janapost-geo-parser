package com.example.myapplication

import com.example.myapplication.data.formatPartial
import com.example.myapplication.data.formatSent
import org.junit.Assert.assertEquals
import org.junit.Test

class PhoneFormatTest {
    @Test
    fun formatsFullKazakhNumber() {
        assertEquals("+7(778)008-50-81", formatSent("+7", "7780085081"))
    }

    @Test
    fun keepsOtherCountriesReadable() {
        assertEquals("+996 555123456", formatSent("+996", "555123456"))
    }

    @Test
    fun formatsPartialKazakhNumber() {
        assertEquals("(778) 008", formatPartial("+7", "778008"))
    }
}
