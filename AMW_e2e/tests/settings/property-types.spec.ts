import { test, expect } from '@playwright/test';

test.beforeEach(async ({page}) => {
    await page.goto('./#/settings/property-types');
});

/**
 * Test scenario:
 * 1. add a propertyType called test-property-type with invalid regex should not be possible
 * 2. add a propertyType called test-property-type
 * 3. adding the same propertyType again should not be possible
 * 4. updating the propertyType
 * 5. delete propertyType
 */

test.describe('PropertyTypes -CRUD', () => {
    test('should create, read and delete a propertyType', async ({ page }) => {

        await page.getByTestId('button-add').click();
        await page.locator('#name').fill('test-property-type');
        await page.locator('#regex').fill('*');
        await page.getByTestId('button-save').click();
        expect(page.locator('ngb-toast :has-text("Invalid property type validation pattern.")')).toBeDefined();

        await page.getByTestId('button-add').click();
        await page.locator('#name').fill('test-property-type');
        await page.locator('#regex').fill('sd');
        await page.getByTestId('button-save').click();
        expect(page.locator('ngb-toast :has-text("Property type saved.")')).toBeDefined();

        await page.getByTestId('button-add').click();
        await page.locator('#name').fill('test-property-type');
        await page.locator('#regex').fill('sd');
        await page.getByTestId('button-save').click();
        expect(page.locator('ngb-toast :has-text("Property type already exists.")')).toBeDefined();

        await page.getByRole('row').filter({ hasText: 'test-property-type' }).getByRole('button').first().click();
        await page.locator('#encrypted').click();
        await page.getByTestId('button-save').click();
        await expect(page.getByText(/Property type saved\./).first()).toBeVisible();
        await page.getByRole('row').filter({ hasText: 'test-property-type' }).getByRole('button').last().click();
        await page.getByTestId('button-delete').click();
        expect(page.locator('ngb-toast :has-text("Property type deleted.")')).toBeDefined();
    });
});
