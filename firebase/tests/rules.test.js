// firebase/CLAUDE.md "규칙을 바꿀 때 반드시 테스트할 시나리오" + 커플 연결 흐름 (sync-and-pairing.md 3 · 4장)
import { readFileSync } from 'node:fs';
import { afterAll, beforeAll, beforeEach, describe, test } from 'vitest';
import { assertFails, assertSucceeds, initializeTestEnvironment } from '@firebase/rules-unit-testing';
import { doc, getDoc, setDoc, updateDoc, deleteDoc, runTransaction, serverTimestamp } from 'firebase/firestore';

const A = 'user-a'; // 코드 발급자
const B = 'user-b'; // 코드 입력자
const C = 'user-c'; // 제3자
const HOUR = 60 * 60 * 1000;

let env;

beforeAll(async () => {
  env = await initializeTestEnvironment({
    projectId: 'demo-days',
    firestore: { rules: readFileSync('firestore.rules', 'utf8') },
  });
});

afterAll(async () => {
  await env.cleanup();
});

beforeEach(async () => {
  await env.clearFirestore();
});

const db = (uid) => (uid ? env.authenticatedContext(uid).firestore() : env.unauthenticatedContext().firestore());

/** 규칙을 건너뛰고 미리 데이터를 넣는다 */
async function seed(path, data) {
  await env.withSecurityRulesDisabled(async (ctx) => {
    await setDoc(doc(ctx.firestore(), path), data);
  });
}

async function seedCouple({ status = 'ACTIVE' } = {}) {
  await seed('couples/c1', { members: [A, B], status, pairCode: 'K7Q2MX', startDate: '2025-06-14' });
}

const sharedItem = (uid) => ({
  title: '지민 생일',
  visibility: 'SHARED',
  updatedBy: uid,
  updatedAt: 1,
  serverUpdatedAt: serverTimestamp(),
});

describe('공유 항목', () => {
  test('멤버는 공유 항목을 읽고 쓸 수 있다', async () => {
    await seedCouple();
    await assertSucceeds(setDoc(doc(db(A), 'couples/c1/items/i1'), sharedItem(A)));
    await assertSucceeds(getDoc(doc(db(B), 'couples/c1/items/i1')));
    await assertSucceeds(
      updateDoc(doc(db(B), 'couples/c1/items/i1'), {
        title: '지민 생일 🎂',
        updatedBy: B,
        serverUpdatedAt: serverTimestamp(),
      }),
    );
  });

  test('비멤버 · 비로그인 사용자는 거부된다', async () => {
    await seedCouple();
    await seed('couples/c1/items/i1', sharedItem(A));
    await assertFails(getDoc(doc(db(C), 'couples/c1/items/i1')));
    await assertFails(getDoc(doc(db(null), 'couples/c1/items/i1')));
    await assertFails(setDoc(doc(db(C), 'couples/c1/items/i2'), sharedItem(C)));
    await assertFails(getDoc(doc(db(C), 'couples/c1')));
  });

  test('나만 보기 항목이나 다른 사람 이름으로는 쓸 수 없다', async () => {
    await seedCouple();
    await assertFails(setDoc(doc(db(A), 'couples/c1/items/i1'), { ...sharedItem(A), visibility: 'PRIVATE' }));
    await assertFails(setDoc(doc(db(A), 'couples/c1/items/i1'), sharedItem(B)));
  });

  test('연결 해제된 커플에는 항목을 쓸 수 없다', async () => {
    await seedCouple({ status: 'DISCONNECTED' });
    await assertFails(setDoc(doc(db(A), 'couples/c1/items/i1'), sharedItem(A)));
  });

  test('serverUpdatedAt은 서버 시각만 허용한다', async () => {
    await seedCouple();
    await assertFails(setDoc(doc(db(A), 'couples/c1/items/i1'), { ...sharedItem(A), serverUpdatedAt: 1 }));
    const { serverUpdatedAt, ...withoutServerTime } = sharedItem(A);
    await assertFails(setDoc(doc(db(A), 'couples/c1/items/i1'), withoutServerTime));
  });

  test('공유 항목 삭제(delete)는 거부된다', async () => {
    await seedCouple();
    await seed('couples/c1/items/i1', sharedItem(A));
    await assertFails(deleteDoc(doc(db(A), 'couples/c1/items/i1')));
  });
});

describe('숨긴 자동 기념일', () => {
  const hide = (deletedAt = null) => ({ createdAt: 1, deletedAt, serverUpdatedAt: serverTimestamp() });

  test('멤버는 숨기고 숨김 해제(deletedAt)할 수 있다', async () => {
    await seedCouple();
    await assertSucceeds(setDoc(doc(db(A), 'couples/c1/hiddenAnniversaries/DAY_600'), hide()));
    await assertSucceeds(getDoc(doc(db(B), 'couples/c1/hiddenAnniversaries/DAY_600')));
    await assertSucceeds(setDoc(doc(db(B), 'couples/c1/hiddenAnniversaries/DAY_600'), hide(2)));
  });

  test('비멤버는 읽거나 쓸 수 없다', async () => {
    await seedCouple();
    await seed('couples/c1/hiddenAnniversaries/DAY_600', { createdAt: 1, deletedAt: null, serverUpdatedAt: 1 });
    await assertFails(getDoc(doc(db(C), 'couples/c1/hiddenAnniversaries/DAY_600')));
    await assertFails(setDoc(doc(db(C), 'couples/c1/hiddenAnniversaries/YEAR_2'), hide()));
  });

  test('실제 삭제 · 서버 시각이 아닌 serverUpdatedAt · 해제된 커플 쓰기는 거부된다', async () => {
    await seedCouple();
    await seed('couples/c1/hiddenAnniversaries/DAY_600', { createdAt: 1, deletedAt: null, serverUpdatedAt: 1 });
    await assertFails(deleteDoc(doc(db(A), 'couples/c1/hiddenAnniversaries/DAY_600')));
    await assertFails(setDoc(doc(db(A), 'couples/c1/hiddenAnniversaries/YEAR_2'), { ...hide(), serverUpdatedAt: 5 }));
    await seedCouple({ status: 'DISCONNECTED' });
    await assertFails(setDoc(doc(db(A), 'couples/c1/hiddenAnniversaries/YEAR_2'), hide()));
  });
});

describe('커플 연결', () => {
  async function seedCode({ expiresAt = Date.now() + HOUR, usedBy } = {}) {
    await seed('pairCodes/K7Q2MX', { creatorUid: A, expiresAt, createdAt: Date.now(), ...(usedBy ? { usedBy } : {}) });
  }

  /** B가 코드를 사용하고 같은 트랜잭션에서 커플을 만든다 */
  function join(uid, members = [A, uid]) {
    const firestore = db(uid);
    return runTransaction(firestore, async (tx) => {
      tx.update(doc(firestore, 'pairCodes/K7Q2MX'), { usedBy: uid });
      tx.set(doc(firestore, 'couples/c1'), { members, status: 'ACTIVE', pairCode: 'K7Q2MX', startDate: '2025-06-14' });
    });
  }

  test('유효한 코드로는 커플을 만들 수 있다', async () => {
    await seedCode();
    await assertSucceeds(join(B));
  });

  test('만료된 코드로는 커플을 만들 수 없다', async () => {
    await seedCode({ expiresAt: Date.now() - HOUR });
    await assertFails(join(B));
  });

  test('이미 사용된 코드로는 커플을 만들 수 없다', async () => {
    await seedCode({ usedBy: C });
    await assertFails(join(B));
  });

  test('본인이 만든 코드로는 커플을 만들 수 없다', async () => {
    await seedCode();
    await assertFails(join(A, [B, A]));
  });

  test('코드를 쓰지 않고는 커플을 만들 수 없다', async () => {
    await seedCode();
    await assertFails(setDoc(doc(db(B), 'couples/c1'), { members: [A, B], status: 'ACTIVE', pairCode: 'K7Q2MX' }));
  });

  test('코드는 하나씩만 조회할 수 있고 발급자는 자기 코드를 지울 수 있다', async () => {
    await seedCode();
    await assertSucceeds(getDoc(doc(db(B), 'pairCodes/K7Q2MX')));
    await assertFails(deleteDoc(doc(db(B), 'pairCodes/K7Q2MX')));
    await assertSucceeds(deleteDoc(doc(db(A), 'pairCodes/K7Q2MX')));
  });

  test('멤버는 커플 상태를 바꿀 수 있지만 멤버 구성은 못 바꾼다', async () => {
    await seedCouple();
    await assertSucceeds(updateDoc(doc(db(A), 'couples/c1'), { status: 'DISCONNECTED', disconnectedAt: 1 }));
    await assertFails(updateDoc(doc(db(A), 'couples/c1'), { members: [A, C] }));
    await assertFails(deleteDoc(doc(db(A), 'couples/c1')));
  });
});

describe('사용자 문서', () => {
  const profile = (nickname, coupleId) => ({ nickname, coupleId, updatedAt: 1, serverUpdatedAt: serverTimestamp() });

  test('같은 커플의 상대 프로필만 읽을 수 있다', async () => {
    await seedCouple();
    await seed(`users/${A}`, { nickname: '현우', coupleId: 'c1' });
    await seed(`users/${B}`, { nickname: '지민', coupleId: 'c1' });
    await seed(`users/${C}`, { nickname: '남', coupleId: null });
    await assertSucceeds(getDoc(doc(db(A), `users/${B}`)));
    await assertFails(getDoc(doc(db(C), `users/${A}`)));
  });

  test('남의 커플 id를 내 문서에 적어 그 커플 프로필을 읽을 수 없다', async () => {
    await seedCouple();
    await seed(`users/${A}`, { nickname: '현우', coupleId: 'c1' });
    await assertFails(setDoc(doc(db(C), `users/${C}`), profile('남', 'c1')));
    await assertSucceeds(setDoc(doc(db(C), `users/${C}`), profile('남', null)));
    await assertFails(getDoc(doc(db(C), `users/${A}`)));
  });

  test('내가 멤버인 커플 id는 적을 수 있고 다른 사람 문서는 못 쓴다', async () => {
    await seedCouple();
    await assertSucceeds(setDoc(doc(db(B), `users/${B}`), profile('지민', 'c1')));
    await assertFails(setDoc(doc(db(B), `users/${A}`), profile('현우', null)));
  });

  test('프로필의 serverUpdatedAt은 서버 시각만 허용한다', async () => {
    await assertFails(setDoc(doc(db(C), `users/${C}`), { ...profile('남', null), serverUpdatedAt: 1 }));
    await assertFails(setDoc(doc(db(C), `users/${C}`), { nickname: '남', updatedAt: 1 }));
  });

  test('이름 · 생일만 merge해도 연결된 coupleId는 그대로 유지된다', async () => {
    await seedCouple();
    await seed(`users/${B}`, { nickname: '지민', coupleId: 'c1', serverUpdatedAt: 1 });
    await assertSucceeds(
      setDoc(
        doc(db(B), `users/${B}`),
        { nickname: '지민 🌷', birthDate: '1999-09-28', updatedAt: 2, serverUpdatedAt: serverTimestamp() },
        { merge: true },
      ),
    );
  });
});
