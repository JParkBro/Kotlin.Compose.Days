package jpark.bro.days.core.database.entity

/** Room 전용 동기화 상태 (`sync_state` 컬럼, sync-and-pairing.md 2장). 컬럼에는 [name]을 저장한다 */
enum class SyncState {
    /** 서버에 올려야 함 (공유 항목을 저장 · 삭제한 직후) */
    PENDING,

    /** 서버와 같음. 나만 보기(PRIVATE) 항목은 올릴 것이 없으므로 항상 이 값 */
    SYNCED,

    /** 올리기 실패, 재시도 대상 */
    FAILED,
}
