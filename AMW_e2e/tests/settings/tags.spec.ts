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
        const uid = crypto.randomUUID().substring(0, 8);
        const tagName = `test-tag-${uid}`;
        const otherTagName = `other-tag-${uid}`;

        await page.locator('#tagName').fill(tagName);
        await page.locator('#tagName').press('Enter');
        await expect(page.locator('#tagName')).toBeEmpty();
        await expect(page.locator('ngb-toast').filter({ hasText: 'Tag added.' }).first()).toBeVisible({ timeout: 5000 });

        // Close first toast before adding another tag
        await page.getByTestId('toast-close').first().click();

        await page.locator('#tagName').clear();
        await page.locator('#tagName').fill(otherTagName);
        await page.getByTestId('button-add').click();
        await expect(page.locator('#tagName')).toBeEmpty();
        await page.getByRole('row').filter({ hasText: otherTagName }).getByRole('button').last().click();
        await expect(page.getByText(/Tag deleted/).first()).toBeVisible();
        await page.getByRole('row').filter({ hasText: tagName }).getByRole('button').last().click();
        await expect(page.locator('ngb-toast').filter({ hasText: 'Tag deleted.' }).first()).toBeVisible({ timeout: 5000 });
    });
});
