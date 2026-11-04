package jpark.bro.days.feature.couple.impl

import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId

/** product-spec 9장: 오늘 = 2026.11.05 */
internal val Seoul: ZoneId = ZoneId.of("Asia/Seoul")
internal val Today: LocalDate = LocalDate.of(2026, 11, 5)
internal val TestClock: Clock = Clock.fixed(Today.atStartOfDay(Seoul).toInstant(), Seoul)
