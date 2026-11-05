package jpark.bro.days.core.firebase.sync

/**
 * Firestore 문서 ↔ 앱 사이의 전송 객체 (data-model-erd.md 4장). 필드명은 camelCase, Room 엔티티 변환은 core:sync가 한다.
 * 상대 기기가 쓴 문서는 신뢰하지 않는다: 필수 필드가 없거나 타입이 다르면 그 문서는 건너뛴다(null).
 * [serverUpdatedAtMicros]는 서버가 기록한 시각(마이크로초). 올릴 때는 쓰지 않고 서버 시각으로 채운다.
 */
data class ItemDto(
    val id: String,
    val type: String,
    val ownerUserId: String,
    val coupleId: String,
    val title: String,
    val category: String,
    val date: String?,
    val countMode: String?,
    val countFirstDayAsOne: Boolean,
    val repeatRule: String,
    val timeSlot: String?,
    val time: String?,
    val place: String?,
    val memo: String?,
    val isDone: Boolean,
    val showAsDday: Boolean,
    val colorKey: String,
    val iconKey: String,
    val backgroundImageId: String?,
    val updatedBy: String,
    val createdAt: Long,
    val updatedAt: Long,
    val deletedAt: Long?,
    val serverUpdatedAtMicros: Long = 0,
)

/** Firestore `couples/{id}`. members[0] 코드 발급자, members[1] 코드 입력자 */
data class CoupleDto(
    val id: String,
    val members: List<String>,
    val startDate: String,
    val countFirstDayAsOne: Boolean,
    val status: String,
    val pairCode: String,
    val disconnectedAt: Long?,
    val createdAt: Long,
    val updatedAt: Long,
    val serverUpdatedAtMicros: Long = 0,
)

/** Firestore `couples/{coupleId}/hiddenAnniversaries/{ruleKey}`. 숨김 해제는 [deletedAt] */
data class HiddenAnniversaryDto(
    val ruleKey: String,
    val createdAt: Long,
    val deletedAt: Long?,
    val serverUpdatedAtMicros: Long = 0,
)

internal fun ItemDto.toFirestoreMap(): Map<String, Any?> =
    mapOf(
        "type" to type,
        "ownerUserId" to ownerUserId,
        "coupleId" to coupleId,
        // 규칙이 SHARED만 허용한다 (나만 보기 항목은 서버로 보내지 않음)
        "visibility" to SHARED,
        "title" to title,
        "category" to category,
        "date" to date,
        "countMode" to countMode,
        "countFirstDayAsOne" to countFirstDayAsOne,
        "repeatRule" to repeatRule,
        "timeSlot" to timeSlot,
        "time" to time,
        "place" to place,
        "memo" to memo,
        "isDone" to isDone,
        "showAsDday" to showAsDday,
        "colorKey" to colorKey,
        "iconKey" to iconKey,
        "backgroundImageId" to backgroundImageId,
        "updatedBy" to updatedBy,
        "createdAt" to createdAt,
        "updatedAt" to updatedAt,
        "deletedAt" to deletedAt,
    )

internal fun itemDtoOf(
    id: String,
    data: Map<String, Any?>,
    serverUpdatedAtMicros: Long,
): ItemDto? =
    parseOrNull {
        if (data["visibility"] != SHARED) throw InvalidDocumentException()
        ItemDto(
            id = id,
            type = data.requiredString("type"),
            ownerUserId = data.requiredString("ownerUserId"),
            coupleId = data.requiredString("coupleId"),
            title = data.requiredString("title"),
            category = data.requiredString("category"),
            date = data.string("date"),
            countMode = data.string("countMode"),
            countFirstDayAsOne = data.boolean("countFirstDayAsOne"),
            repeatRule = data.requiredString("repeatRule"),
            timeSlot = data.string("timeSlot"),
            time = data.string("time"),
            place = data.string("place"),
            memo = data.string("memo"),
            isDone = data.boolean("isDone"),
            showAsDday = data.boolean("showAsDday"),
            colorKey = data.requiredString("colorKey"),
            iconKey = data.requiredString("iconKey"),
            backgroundImageId = data.pathId("backgroundImageId"),
            updatedBy = data.requiredString("updatedBy"),
            createdAt = data.requiredLong("createdAt"),
            updatedAt = data.requiredLong("updatedAt"),
            deletedAt = data.long("deletedAt"),
            serverUpdatedAtMicros = serverUpdatedAtMicros,
        )
    }

internal fun coupleDtoOf(
    id: String,
    data: Map<String, Any?>,
    serverUpdatedAtMicros: Long,
): CoupleDto? =
    parseOrNull {
        val members = (data["members"] as? List<*>)?.filterIsInstance<String>()?.takeIf { it.size == 2 }
        CoupleDto(
            id = id,
            members = members ?: throw InvalidDocumentException(),
            startDate = data.requiredString("startDate"),
            countFirstDayAsOne = data.boolean("countFirstDayAsOne"),
            status = data.requiredString("status"),
            pairCode = data.requiredString("pairCode"),
            disconnectedAt = data.long("disconnectedAt"),
            createdAt = data.requiredLong("createdAt"),
            updatedAt = data.requiredLong("updatedAt"),
            serverUpdatedAtMicros = serverUpdatedAtMicros,
        )
    }

internal fun HiddenAnniversaryDto.toFirestoreMap(): Map<String, Any?> =
    mapOf("createdAt" to createdAt, "deletedAt" to deletedAt)

internal fun hiddenAnniversaryDtoOf(
    ruleKey: String,
    data: Map<String, Any?>,
    serverUpdatedAtMicros: Long,
): HiddenAnniversaryDto? =
    parseOrNull {
        HiddenAnniversaryDto(
            ruleKey = ruleKey,
            createdAt = data.requiredLong("createdAt"),
            deletedAt = data.long("deletedAt"),
            serverUpdatedAtMicros = serverUpdatedAtMicros,
        )
    }

private const val SHARED = "SHARED"
