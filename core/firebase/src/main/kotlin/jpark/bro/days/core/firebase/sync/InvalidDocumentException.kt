package jpark.bro.days.core.firebase.sync

// 상대 기기가 쓴 Firestore 문서 읽기. 신뢰하지 않으므로 필수 필드가 없거나 타입이 다르면 문서 전체를 건너뛴다

/** 상대 기기가 쓴 문서의 필수 필드가 없거나 타입이 다름 */
internal class InvalidDocumentException : Exception()

internal inline fun <T> parseOrNull(parse: () -> T): T? =
    try {
        parse()
    } catch (_: InvalidDocumentException) {
        null
    }

internal fun Map<String, Any?>.requiredString(key: String): String = string(key) ?: throw InvalidDocumentException()

internal fun Map<String, Any?>.requiredLong(key: String): Long = long(key) ?: throw InvalidDocumentException()

internal fun Map<String, Any?>.string(key: String): String? = this[key] as? String

internal fun Map<String, Any?>.long(key: String): Long? = (this[key] as? Number)?.toLong()

internal fun Map<String, Any?>.boolean(key: String): Boolean = this[key] as? Boolean ?: false

/**
 * 파일 경로에 들어가는 id (사진 id → 기기 파일 이름 · Storage 경로). 상대가 쓴 값이라 영문 · 숫자 · `-`만 받는다.
 * 형식이 틀리면 없는 것으로 본다 (`../` 같은 경로 조작을 막는다)
 */
internal fun Map<String, Any?>.pathId(key: String): String? = string(key)?.takeIf { PATH_ID.matches(it) }

private val PATH_ID = Regex("^[A-Za-z0-9-]{1,64}$")
