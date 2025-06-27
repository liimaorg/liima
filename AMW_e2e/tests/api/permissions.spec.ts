import {test, expect} from '@playwright/test';

test.describe('permissions-rest integration test', () => {
    test.describe('list restriction - validation', () => {
        test('list restriction fails with viewer user', async ({request}) => {
            const response = await request.get(
                'http://viewer:viewer@localhost:8080/AMW_rest/resources/permissions/restrictions', {
                    headers: {
                        "Content-Type": "application/json; charset=utf-8",
                    }
                }
            );
            expect(response.status()).toBe(403);
        });
        test('list own restrictions succeeds with viewer user', async ({
                                                                           request,
                                                                       }) => {
            const response = await request.get(
                'http://admin:admin@localhost:8080/AMW_rest/resources/permissions/restrictions/ownRestrictions'
            );
            expect(response.status()).toBe(200);
        });
        test('list restrictions succeeds with admin user', async ({request}) => {
            const response = await request.get(
                'http://admin:admin@localhost:8080/AMW_rest/resources/permissions/restrictions'
            );
            expect(response.status()).toBe(200);
        });
    });
});
