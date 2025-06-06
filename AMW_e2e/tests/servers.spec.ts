import {test, expect} from '@playwright/test';


test.beforeEach(async ({page}) => {
    await page.goto('./#/servers');
});


test.describe('Servers Page', () => {
    test('should navigate to the servers page', async ({ page }) => {
        await expect(
            page
                .locator('[data-cy="page-title"]')
                .getByText(/Servers/)
                .first()
        ).toBeVisible();
    });
});
