package jpark.bro.days.core.sync

import jpark.bro.days.core.firebase.sync.CoupleDto
import jpark.bro.days.core.firebase.sync.HiddenAnniversaryDto
import jpark.bro.days.core.firebase.sync.ItemDto
import jpark.bro.days.core.firebase.sync.SyncRemoteDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

/** 서버 대신. 리스너는 [coupleChanges] 등에 직접 흘려 넣고, 1회성 조회는 [remoteItems] 등을 돌려준다 */
internal class FakeSyncRemoteDataSource : SyncRemoteDataSource {
    val coupleChanges = MutableSharedFlow<List<CoupleDto>>()
    val itemChanges = MutableSharedFlow<List<ItemDto>>()
    val hideChanges = MutableSharedFlow<List<HiddenAnniversaryDto>>()

    var remoteCouples: List<CoupleDto> = emptyList()
    var remoteItems: List<ItemDto> = emptyList()
    var remoteHides: List<HiddenAnniversaryDto> = emptyList()

    val pushedItems = mutableListOf<Pair<String, List<ItemDto>>>()
    val pushedHides = mutableListOf<Pair<String, List<HiddenAnniversaryDto>>>()

    /** 조회 · 리스너에 넘어온 커서 */
    val requestedSince = mutableListOf<Long?>()

    var failPush = false

    /** 올리는 도중에 일어나는 일 (예: 같은 항목을 다시 저장) */
    var duringPush: suspend () -> Unit = {}

    override fun observeCouples(uid: String): Flow<List<CoupleDto>> = coupleChanges

    override suspend fun getCouples(uid: String): List<CoupleDto> = remoteCouples

    override suspend fun pushItems(
        coupleId: String,
        items: List<ItemDto>,
    ) {
        duringPush()
        check(!failPush) { "push 실패 (테스트)" }
        pushedItems += coupleId to items
    }

    override fun observeItems(
        coupleId: String,
        sinceMicros: Long?,
    ): Flow<List<ItemDto>> {
        requestedSince += sinceMicros
        return itemChanges
    }

    override suspend fun getItems(
        coupleId: String,
        sinceMicros: Long?,
    ): List<ItemDto> {
        requestedSince += sinceMicros
        return remoteItems
    }

    override suspend fun pushHiddenAnniversaries(
        coupleId: String,
        hides: List<HiddenAnniversaryDto>,
    ) {
        check(!failPush) { "push 실패 (테스트)" }
        pushedHides += coupleId to hides
    }

    override fun observeHiddenAnniversaries(
        coupleId: String,
        sinceMicros: Long?,
    ): Flow<List<HiddenAnniversaryDto>> = hideChanges

    override suspend fun getHiddenAnniversaries(
        coupleId: String,
        sinceMicros: Long?,
    ): List<HiddenAnniversaryDto> = remoteHides
}
