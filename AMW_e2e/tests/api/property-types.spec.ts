import {test, expect} from '@playwright/test';

test.describe('property-types integration test', () => {
    test.describe('creating property types - validation', () => {
        test('validation fails when body is null', async ({request}) => {
            const response = await request.post(
                'http://admin:admin@localhost:8080/AMW_rest/resources/settings/propertyTypes', {
                    data: null, headers: {
                        "Content-Type": "application/json; charset=utf-8",
                    }
                }
            );
            expect(response.status()).toBe(400);
            expect((await response.json()).message).toBe(
                'Property type must not be null.'
            );
        });

        test('validation fails when name property is null', async ({request}) => {
            const response = await request.post(
                'http://admin:admin@localhost:8080/AMW_rest/resources/settings/propertyTypes', {
                    data: {name: null, validationRegex: "string", propertyTags: []}
                }
            );
            expect(response.status()).toBe(400);
            expect((await response.json()).message).toContain(
                'PropertyType name must not be null.'
            );
        });

        test('validation fails when name property is blank', async ({request}) => {
            const response = await request.post(
                'http://admin:admin@localhost:8080/AMW_rest/resources/settings/propertyTypes', {
                    data: {name: "", validationRegex: "string", propertyTags: []}
                }
            );
            expect(response.status()).toBe(400);
            expect((await response.json()).message).toBe(
                'PropertyType name must not be empty.'
            );
        });

        test('validation fails when tag name property is blank', async ({
                                                                            request,
                                                                        }) => {
            const response = await request.post(
                'http://admin:admin@localhost:8080/AMW_rest/resources/settings/propertyTypes', {
                    data: {
                        name: "name",
                        validationRegex: "string",
                        propertyTags: [{name: null, type: "LOCAL"}],
                    }
                }
            );
            expect(response.status()).toBe(400);
            expect((await response.json()).message).toBe(
                'PropertyTag name must not be null or empty.'
            );
        });
    });

    test.describe('deleting propertytype - validation', () => {
        test('fails if tag is not found', async ({request}) => {
            const response = await request.delete(
                'http://admin:admin@localhost:8080/AMW_rest/resources/settings/propertyTypes/-1'
            );
            expect(response.status()).toBe(404);
        });
    });

    test.describe('working with propertytypes', () => {
        const name = `test-propertyType${new Date().getMilliseconds()}`;

        test('create a new propertytype, list them, update it and delete it afterwards', async ({
                                                                                                    request,
                                                                                                }) => {
            const response = await request.post(
                'http://admin:admin@localhost:8080/AMW_rest/resources/settings/propertyTypes', {
                    data: {
                        name: name,
                        validationRegex: "string",
                        propertyTags: [
                            {name: name, type: "LOCAL"}
                        ],
                    }
                }
            )
                .then(async (response) => {
                    expect(response.status()).toBe(201);

                    const location = response.headers()['location'];
                    await request.get('http://admin:admin@localhost:8080/AMW_rest/resources/settings/propertyTypes').then((response) => {
                        expect(response.status()).toBe(200);
                    });
                    await request.put(location, {
                        data: {
                            name: name,
                            validationRegex: "string3",
                            encryption: true,
                            propertyTags: [{name: name, type: "LOCAL"}],
                        }
                    }).then(async (response) => {
                        expect(response.status()).toBe(200);
                        await request.delete(location).then(async (response) => {
                            expect(response.status()).toBe(204);
                            await request.get('http://admin:admin@localhost:8080/AMW_rest/resources/settings/propertyTypes')
                                .then(async response => {
                                    let resp = await response.json();
                                    expect(resp).not.toContain(name)
                                })
                        })

                    })
                });


        });
    });
});
