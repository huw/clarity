package nu.huw.clarity

import nu.huw.clarity.model.IDHelper
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Ensures ID helper functions work correctly
 */
class IDTest {
    @Test
    fun id_validate_correctID() {
        assertTrue(IDHelper.validate("eVJuS9Id_wJ"))
    }

    @Test
    fun id_validate_incorrectID() {
        assertFalse(IDHelper.validate("eVJuS9Id%wJ"))
    }

    @Test
    fun id_validateStrict_correctID() {
        assertTrue(IDHelper.validate("eVJuS9Id_wJ", true))
    }

    @Test
    fun id_validateStrict_incorrectID() {
        assertFalse(IDHelper.validate("20170112132112=eVJuS9Id_wJ+f7GuFV74VOG", true))
    }
}