package jpark.bro.days.core.data.repository

import jpark.bro.days.core.model.Reauthentication

/** 회원 탈퇴 (sync-and-pairing.md 7장) */
interface AccountRepository {
    /**
     * [reauthentication]으로 본인 확인 후 탈퇴한다. 순서: 본인 확인 → 커플 연결 해제(상대 기기에 공유 항목 사본이 남는다)
     * → 내가 올린 사진 · 발급한 코드 · `users/{uid}` 삭제 → 로그인 계정 삭제 → 이 기기의 내 데이터 삭제 → 카카오 연결 끊기.
     * 중간에 실패하면 로그인 계정은 남아 다시 시도할 수 있다. 다른 계정으로 확인하면
     * [jpark.bro.days.core.data.account.AccountMismatchException]
     */
    suspend fun withdraw(reauthentication: Reauthentication): Result<Unit>
}
