import { test, expect } from '@playwright/test';

test.beforeEach(async ({page}) => {
    await page.goto('./#/resources');
});

test.describe('Resources Page', () => {
    test('should navigate to the resources page', async ({ page }) => {

        await expect(
            page
                .locator('[data-cy="page-title"]')
                .getByText(/Resources/)
                .first()
        ).toBeVisible();
    });
});
