package jpark.bro.days.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class AnniversaryRuleTest {
    private val date = LocalDate.of(2026, 11, 5)

    @Test
    fun `기념일 키를 종류로 되읽음`() {
        assertEquals(AnniversaryRule.Days(600), AutoAnniversary.ruleOf(AutoAnniversary.Days(600, date).ruleKey))
        assertEquals(AnniversaryRule.Years(2), AutoAnniversary.ruleOf(AutoAnniversary.Years(2, date).ruleKey))
        assertEquals(
            AnniversaryRule.Birthday("user_with_underscore"),
            AutoAnniversary.ruleOf(AutoAnniversary.Birthday("user_with_underscore", 2026, date).ruleKey),
        )
    }

    @Test
    fun `형식이 다른 키는 null`() {
        assertNull(AutoAnniversary.ruleOf("DAY_x"))
        assertNull(AutoAnniversary.ruleOf("BDAY_2026"))
        assertNull(AutoAnniversary.ruleOf("OTHER_1"))
    }
}
