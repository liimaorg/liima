import { test, expect } from '@playwright/test';

test.beforeEach(async ({page}) => {
    await page.goto('./#/settings/tags');
});

/**
 * Test scenario:
 * 1. add a tag called test-tag
 * 2. adding the same tag again should not be possible
 * 3. adding a different tag should work
 * 4. delete both tags again
 */

test.describe('Tags -CRUD', () => {
    test('should create, read and delete a tag', async ({ page }) => {

        await page.locator('#tagName').fill('test-tag');
        await page.locator('#tagName').press('Enter');
        await expect(page.locator('#tagName')).toBeEmpty();
        expect(page.locator('ngb-toast :has-text("Tag added.")')).toBeDefined();

        await page.locator('#tagName').fill('test-tag');
        await page.locator('#tagName').press('Enter');
        await expect(page.locator('#tagName')).toBeEmpty();
        expect(page.locator('ngb-toast :has-text("Tag with name test-tag already exists.")')).toBeDefined();

        await page.locator('#tagName').clear();
        await page.locator('#tagName').fill('other-tag');
        await page.getByTestId('button-add').click();
        await expect(page.locator('#tagName')).toBeEmpty();
        await page.getByRole('row').filter({ hasText: 'other-tag' }).getByRole('button').last().click();
        await expect(page.getByText(/Tag deleted/).first()).toBeVisible();
        await page.getByRole('row').filter({ hasText: 'test-tag' }).getByRole('button').last().click();
        expect(page.locator('ngb-toast :has-text("Tag deleted.")')).toBeDefined();
    });
});
