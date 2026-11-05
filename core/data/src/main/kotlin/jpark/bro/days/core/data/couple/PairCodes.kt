package jpark.bro.days.core.data.couple

import jpark.bro.days.core.model.PairCodeCheck
import kotlin.random.Random

/** 6자리 영문 대문자 + 숫자. 헷갈리는 0 · O · 1 · I는 뺀다 (product-spec 7장) */
internal const val PAIR_CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
internal const val PAIR_CODE_LENGTH = 6

/** 24시간 유효 */
internal const val PAIR_CODE_VALID_MILLIS = 24 * 60 * 60 * 1000L

/** 30일 안에 같은 상대와 다시 연결하면 기존 커플(공유 데이터)을 되살린다 */
internal const val RECONNECT_WINDOW_MILLIS = 30 * 24 * 60 * 60 * 1000L

internal fun newPairCode(random: Random): String =
    (1..PAIR_CODE_LENGTH).map { PAIR_CODE_ALPHABET[random.nextInt(PAIR_CODE_ALPHABET.length)] }.joinToString("")

/** 입력값을 코드 형식으로 (대문자). 형식이 아니면 null */
internal fun normalizePairCode(input: String): String? =
    input.trim().uppercase().takeIf { code -> code.length == PAIR_CODE_LENGTH && code.all { it in PAIR_CODE_ALPHABET } }

/** 코드 발급에는 상대 확인 화면에 보일 내 이름이 필요하다 */
class ProfileRequiredException : IllegalStateException("프로필 이름이 필요합니다.")

/** 코드로 연결하지 못한 이유 */
class PairingException(
    val reason: PairCodeCheck,
) : IllegalStateException("커플 연결 불가: $reason")
