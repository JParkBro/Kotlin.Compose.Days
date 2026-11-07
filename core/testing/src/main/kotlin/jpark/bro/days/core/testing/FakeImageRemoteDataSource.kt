package jpark.bro.days.core.testing

import jpark.bro.days.core.firebase.sync.IMAGE_LIMIT_PER_COUPLE
import jpark.bro.days.core.firebase.sync.ImageLimitException
import jpark.bro.days.core.firebase.sync.ImageNotFoundException
import jpark.bro.days.core.firebase.sync.ImageRemoteDataSource
import jpark.bro.days.core.firebase.sync.ImageUsage
import java.io.File

/** 메모리에 커플별 파일 · 업로드 카운터를 두는 Fake. 규칙처럼 방금 등록한 사진만 올릴 수 있다 */
class FakeImageRemoteDataSource : ImageRemoteDataSource {
    /** "coupleId/imageId" → 내용 */
    val files = mutableMapOf<String, ByteArray>()
    val counts = mutableMapOf<String, Int>()
    val bonuses = mutableMapOf<String, Int>()
    private val lastClaimed = mutableMapOf<String, String>()

    /** null이 아니면 upload · download · delete가 이 예외로 실패한다 */
    var failure: Exception? = null

    override suspend fun claim(
        coupleId: String,
        imageId: String,
    ) {
        if (lastClaimed[coupleId] == imageId) return
        val count = counts[coupleId] ?: 0
        if (count >= IMAGE_LIMIT_PER_COUPLE + (bonuses[coupleId] ?: 0)) throw ImageLimitException()
        counts[coupleId] = count + 1
        lastClaimed[coupleId] = imageId
    }

    override suspend fun upload(
        coupleId: String,
        imageId: String,
        file: File,
    ) {
        failure?.let { throw it }
        check(lastClaimed[coupleId] == imageId) { "등록하지 않은 사진 (규칙 거부)" }
        check(files["$coupleId/$imageId"] == null) { "덮어쓰기 (규칙 거부)" }
        files["$coupleId/$imageId"] = file.readBytes()
    }

    override suspend fun usage(coupleId: String): ImageUsage =
        ImageUsage(count = counts[coupleId] ?: 0, bonus = bonuses[coupleId] ?: 0)

    /** 서버(함수)가 이미 올렸다면 바로, 아니면 시간 초과(null) */
    override suspend fun awaitBonusAbove(
        coupleId: String,
        bonus: Int,
        timeoutMillis: Long,
    ): ImageUsage? = usage(coupleId).takeIf { it.bonus > bonus }

    override suspend fun download(
        coupleId: String,
        imageId: String,
        destination: File,
    ) {
        failure?.let { throw it }
        val data = files["$coupleId/$imageId"] ?: throw ImageNotFoundException()
        destination.parentFile?.mkdirs()
        destination.writeBytes(data)
    }

    override suspend fun delete(
        coupleId: String,
        imageId: String,
    ) {
        failure?.let { throw it }
        files.remove("$coupleId/$imageId")
    }
}
