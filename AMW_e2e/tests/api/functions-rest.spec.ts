import { test, expect } from '@playwright/test';

test.describe('functions-rest integration test', () => {
    test.describe('getting all functions', () => {
        test('returns a list of functions', async ({ request }) => {
            const response = await request.get(
                'http://admin:admin@localhost:8080/AMW_rest/resources/settings/functions'
            );
            expect(response.status()).toBe(200);
            const functions = response.body;
        });
    });
});
