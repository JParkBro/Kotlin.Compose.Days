package jpark.bro.days.core.model

/** 복원한 백업: 언제 만든 백업인지(epoch millis)와 되살린 나만 보기 항목 수 */
data class RestoredBackup(
    val createdAt: Long,
    val itemCount: Int,
)
