// 사진 업로드 한도 · Storage 규칙 (infra-and-billing-protection.md 2-2 · 2-3)
import { readFileSync } from 'node:fs';
import { afterAll, beforeAll, beforeEach, describe, test } from 'vitest';
import { assertFails, assertSucceeds, initializeTestEnvironment } from '@firebase/rules-unit-testing';
import { doc, getDoc, setDoc, updateDoc, deleteDoc } from 'firebase/firestore';
import { ref, uploadBytes, getBytes, deleteObject } from 'firebase/storage';

const A = 'user-a';
const B = 'user-b';
const C = 'user-c';
const IMG1 = '11111111-1111-1111-1111-111111111111';
const IMG2 = '22222222-2222-2222-2222-222222222222';
const KB = 1024;

let env;

beforeAll(async () => {
  env = await initializeTestEnvironment({
    projectId: 'demo-days',
    firestore: { rules: readFileSync('firestore.rules', 'utf8') },
    storage: { rules: readFileSync('storage.rules', 'utf8') },
  });
});

afterAll(async () => {
  await env.cleanup();
});

beforeEach(async () => {
  await env.clearFirestore();
  await env.clearStorage();
});

const db = (uid) => (uid ? env.authenticatedContext(uid).firestore() : env.unauthenticatedContext().firestore());
const storage = (uid) => (uid ? env.authenticatedContext(uid).storage() : env.unauthenticatedContext().storage());
const webp = (size = 10 * KB) => new Uint8Array(size);
const put = (uid, path, size, contentType = 'image/webp') =>
  uploadBytes(ref(storage(uid), path), webp(size), { contentType });

async function seed(path, data) {
  await env.withSecurityRulesDisabled(async (ctx) => {
    await setDoc(doc(ctx.firestore(), path), data);
  });
}

async function seedFile(path) {
  await env.withSecurityRulesDisabled(async (ctx) => {
    await uploadBytes(ref(ctx.storage(), path), webp(), { contentType: 'image/webp' });
  });
}

async function seedCouple({ status = 'ACTIVE', count, lastImageId = IMG1 } = {}) {
  await seed('couples/c1', { members: [A, B], status, pairCode: 'K7Q2MX', startDate: '2025-06-14' });
  if (count !== undefined) await seed('couples/c1/usage/images', { count, lastImageId });
}

describe('사진 업로드 카운터', () => {
  test('멤버는 1부터 1씩만 늘리며 방금 올릴 사진 id를 적는다', async () => {
    await seedCouple();
    await assertFails(setDoc(doc(db(A), 'couples/c1/usage/images'), { count: 2, lastImageId: IMG1 }));
    await assertSucceeds(setDoc(doc(db(A), 'couples/c1/usage/images'), { count: 1, lastImageId: IMG1 }));
    await assertSucceeds(updateDoc(doc(db(B), 'couples/c1/usage/images'), { count: 2, lastImageId: IMG2 }));
    await assertFails(updateDoc(doc(db(B), 'couples/c1/usage/images'), { count: 4, lastImageId: IMG1 }));
    await assertSucceeds(getDoc(doc(db(A), 'couples/c1/usage/images')));
  });

  test('30번을 넘기거나 줄이거나 지울 수 없다', async () => {
    await seedCouple({ count: 30 });
    await assertFails(updateDoc(doc(db(A), 'couples/c1/usage/images'), { count: 31, lastImageId: IMG2 }));
    await assertFails(updateDoc(doc(db(A), 'couples/c1/usage/images'), { count: 29, lastImageId: IMG2 }));
    await assertFails(deleteDoc(doc(db(A), 'couples/c1/usage/images')));
  });

  test('비멤버 · 해제된 커플 · 형식이 틀린 id · 다른 필드는 거부된다', async () => {
    await seedCouple();
    await assertFails(setDoc(doc(db(C), 'couples/c1/usage/images'), { count: 1, lastImageId: IMG1 }));
    await assertFails(getDoc(doc(db(C), 'couples/c1/usage/images')));
    await assertFails(setDoc(doc(db(A), 'couples/c1/usage/images'), { count: 1, lastImageId: '../x' }));
    await assertFails(setDoc(doc(db(A), 'couples/c1/usage/images'), { count: 1, lastImageId: IMG1, extra: 1 }));
    await assertFails(setDoc(doc(db(A), 'couples/c1/usage/other'), { count: 1, lastImageId: IMG1 }));

    await seedCouple({ status: 'DISCONNECTED' });
    await assertFails(setDoc(doc(db(A), 'couples/c1/usage/images'), { count: 1, lastImageId: IMG1 }));
  });
});

describe('Storage 사진', () => {
  test('멤버는 방금 등록한 id의 webp 300KB 이하만 올리고, 상대도 읽을 수 있다', async () => {
    await seedCouple({ count: 1 });
    await assertSucceeds(put(A, `couples/c1/images/${IMG1}.webp`, 300 * KB));
    await assertSucceeds(getBytes(ref(storage(B), `couples/c1/images/${IMG1}.webp`)));
  });

  test('등록하지 않은 id · 300KB 초과 · webp가 아닌 형식은 거부된다', async () => {
    await seedCouple({ count: 1 });
    await assertFails(put(A, `couples/c1/images/${IMG2}.webp`));
    await assertFails(put(A, `couples/c1/images/${IMG1}.webp`, 300 * KB + 1));
    await assertFails(put(A, `couples/c1/images/${IMG1}.webp`, 10 * KB, 'image/jpeg'));
  });

  test('이미 있는 파일은 덮어쓸 수 없다', async () => {
    await seedCouple({ count: 1 });
    await seedFile(`couples/c1/images/${IMG1}.webp`);
    await assertFails(put(A, `couples/c1/images/${IMG1}.webp`));
  });

  test('비멤버 · 비로그인 사용자는 읽기 · 쓰기 · 삭제가 거부된다', async () => {
    await seedCouple({ count: 1 });
    await seedFile(`couples/c1/images/${IMG1}.webp`);
    await assertFails(getBytes(ref(storage(C), `couples/c1/images/${IMG1}.webp`)));
    await assertFails(getBytes(ref(storage(null), `couples/c1/images/${IMG1}.webp`)));
    await assertFails(deleteObject(ref(storage(C), `couples/c1/images/${IMG1}.webp`)));
    await assertFails(put(C, `couples/c1/images/${IMG1}.webp`));
  });

  test('해제된 커플에는 올릴 수 없지만 멤버는 읽고 지울 수 있다', async () => {
    await seedCouple({ status: 'DISCONNECTED', count: 1 });
    await seedFile(`couples/c1/images/${IMG1}.webp`);
    await assertFails(put(A, `couples/c1/images/${IMG2}.webp`));
    await assertSucceeds(getBytes(ref(storage(B), `couples/c1/images/${IMG1}.webp`)));
    await assertSucceeds(deleteObject(ref(storage(A), `couples/c1/images/${IMG1}.webp`)));
  });

  test('커플 밖 경로는 모두 거부된다', async () => {
    await seedCouple({ count: 1 });
    await assertFails(put(A, `users/${A}/profile.webp`));
  });
});
