package jpark.bro.days.core.firebase.sync

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SyncDtosTest {
    /** product-spec.md 9장: 지민 생일 (공유, 매년) */
    private val birthday =
        ItemDto(
            id = "item-1",
            type = "DDAY",
            ownerUserId = "user-partner",
            coupleId = "couple-1",
            title = "지민 생일",
            category = "BIRTHDAY",
            date = "2026-12-20",
            countMode = "DDAY",
            countFirstDayAsOne = false,
            repeatRule = "YEARLY",
            timeSlot = null,
            time = null,
            place = null,
            memo = null,
            isDone = false,
            showAsDday = false,
            colorKey = "primary",
            iconKey = "cake",
            backgroundImageId = null,
            updatedBy = "user-partner",
            createdAt = 1_793_836_800_000L,
            updatedAt = 1_793_836_800_000L,
            deletedAt = null,
        )

    @Test
    fun `항목을 문서로 바꿨다가 되돌리면 같고 서버 시각은 받은 값을 씀`() {
        val restored = itemDtoOf("item-1", birthday.toFirestoreMap(), serverUpdatedAtMicros = 42)

        assertEquals(birthday.copy(serverUpdatedAtMicros = 42), restored)
    }

    @Test
    fun `문서는 항상 공유 항목이고 서버 시각 필드는 넣지 않음`() {
        val map = birthday.toFirestoreMap()

        assertEquals("SHARED", map["visibility"])
        assertEquals(false, map.containsKey("serverUpdatedAt"))
    }

    @Test
    fun `배경 사진 id가 경로를 바꾸는 형식이면 사진 없음으로 읽음`() {
        val map = birthday.toFirestoreMap()

        assertEquals(
            "11111111-1111-1111-1111-111111111111",
            itemDtoOf("item-1", map + ("backgroundImageId" to "11111111-1111-1111-1111-111111111111"), 0)
                ?.backgroundImageId,
        )
        assertNull(itemDtoOf("item-1", map + ("backgroundImageId" to "../../shared_prefs/x"), 0)?.backgroundImageId)
        assertNull(itemDtoOf("item-1", map + ("backgroundImageId" to ""), 0)?.backgroundImageId)
    }

    @Test
    fun `상대 프로필의 사진 id와 커플 id도 형식이 틀리면 없음`() {
        val data =
            mapOf(
                "nickname" to "지민",
                "updatedAt" to 1L,
                "profileImageId" to "../x",
                "coupleId" to "couple-1",
            )

        val profile = profileDtoOf("user-partner", data, 0)

        assertNull(profile?.profileImageId)
        assertEquals("couple-1", profile?.coupleId)
    }

    @Test
    fun `필수 필드가 없거나 타입이 다르거나 공유가 아닌 문서는 건너뜀`() {
        val map = birthday.toFirestoreMap()

        assertNull(itemDtoOf("item-1", map - "title", 0))
        assertNull(itemDtoOf("item-1", map + ("updatedAt" to "어제"), 0))
        assertNull(itemDtoOf("item-1", map + ("visibility" to "PRIVATE"), 0))
    }

    @Test
    fun `커플 문서는 멤버가 두 명일 때만 읽음`() {
        val data =
            mapOf(
                "members" to listOf("user-partner", "user-me"),
                "startDate" to "2025-06-14",
                "countFirstDayAsOne" to true,
                "status" to "ACTIVE",
                "pairCode" to "K7Q2MX",
                "createdAt" to 1L,
                "updatedAt" to 2L,
            )

        assertEquals(listOf("user-partner", "user-me"), coupleDtoOf("couple-1", data, 0)?.members)
        assertNull(coupleDtoOf("couple-1", data + ("members" to listOf("user-me")), 0))
    }

    @Test
    fun `숨긴 기념일은 숨김 해제 시각을 그대로 주고받음`() {
        val hide = HiddenAnniversaryDto(ruleKey = "DAY_600", createdAt = 1, deletedAt = 2)

        assertEquals(hide.copy(serverUpdatedAtMicros = 7), hiddenAnniversaryDtoOf("DAY_600", hide.toFirestoreMap(), 7))
    }

    @Test
    fun `서버 시각은 마이크로초까지 남김`() {
        assertEquals(1_793_836_800_000_123L, microsOf(seconds = 1_793_836_800L, nanoseconds = 123_456))
    }

    @Test
    fun `프로필은 이름이 있어야 하고 생일은 없어도 됨`() {
        val data = mapOf("nickname" to "지민", "updatedAt" to 1L)

        assertEquals(
            ProfileDto("user-partner", "지민", null, 1, serverUpdatedAtMicros = 9),
            profileDtoOf("user-partner", data, 9),
        )
        assertNull(profileDtoOf("user-partner", mapOf("coupleId" to "couple-1"), 0))
    }
}
