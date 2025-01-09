import { test, expect } from '@playwright/test';

test.describe('tags-rest integration test', () => {
    test.describe('creating tags - validation', () => {
        test('validation fails when body is null', async ({ request }) => {
            const response = await request.post(
                'http://admin:admin@localhost:8080/AMW_rest/resources/settings/tags',
                {
                    data: null}
            );
            expect(response.status()).toBe(400);
            expect((await response.json()).message).toBe('Tag must not be null.');
        });

        test('validation fails when name property is null', async ({ request }) => {
            const response = await request.post(
                'http://admin:admin@localhost:8080/AMW_rest/resources/settings/tags', {
                    data: { name: null },
                }
            );
            expect(response.status()).toBe(400);
            expect((await response.json()).message).toBe(
                'Tag name must not be null or empty.'
            );
        });

        test('validation fails when name property is blank', async ({ request }) => {
            const response = await request.post(
                'http://admin:admin@localhost:8080/AMW_rest/resources/settings/tags', {
                    data: { name: "" },
                }
            );
            expect(response.status()).toBe(400);
            expect((await response.json()).message).toBe(
                'Tag name must not be null or empty.'
            );
        });
    });

    test.describe('deleting tags - validation', () => {
        test('fails if tag is not found', async ({ request }) => {
            const response = await request.delete(
                'http://admin:admin@localhost:8080/AMW_rest/resources/settings/tags/-1'
            );
            expect(response.status()).toBe(404);
        });
    });

    test.describe('working with tags', () => {
        const tagName = `test-tag${new Date().getMilliseconds()}`;

        test('create a new tag, list them all and delete it afterwards', async ({
                                                                                    request,
                                                                                }) => {
            const response = await request.post(
                'http://admin:admin@localhost:8080/AMW_rest/resources/settings/tags', {
                    data: { name: tagName,
                        failOnStatusCode: false, },
                }
            ).then(async (response) => {
                expect(response.status()).toBe(201);
                const location = response.headers()['location'];
                await request.delete(location).then(response => {
                    expect(response.status()).toBe(200);
                })
                await request.get('http://admin:admin@localhost:8080/AMW_rest/resources/settings/tags').then(
                    async response => {
                        let resp = await response.json();
                        expect(resp).not.toContain(tagName);
                    }
                )
            });
        });
    });
});
