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
