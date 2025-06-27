import { test, expect } from '@playwright/test';

test.beforeEach(async ({page}) => {
    await page.goto('./#/deployments');
});

// scenario:
// create a deployment
// click on tracking id
// deployment must be visible in list of deployments

test.describe('create a deployment', () => {
    test('should create a deployment', async ({ page }) => {

        await expect(page.locator('.page-title')).toHaveText('Deployments');
        await page.getByTestId('create-button').click();
        await expect(page.getByText(/Create new deployment/).first()).toBeVisible();
        await page.locator('#selectApplicationserver').click();
        await expect(page.getByText(/testapplicationserver/).first()).toBeVisible();
        await page
            .getByText(/testapplicationserver/)
            .first()
            .click();
        await page.locator('[data-cy="D"]').click();
        await page.locator('[data-cy="I"]').click();
        const today = new Date();
        const tomorrow = new Date(today);
        tomorrow.setDate(today.getDate() + 1);
        await page
            .locator('[data-cy="date-picker"]')
            .fill(tomorrow.toLocaleDateString('de-CH') + ' 00:00');
        await page.getByTestId('btn-deploy').click();
        await expect(
            page.getByText(/Deployment created: Tracking Id/).first()
        ).toBeVisible();

        await expect(page.getByText(/Tracking Id/).first()).toBeVisible();
        await page
            .getByText(/Tracking Id/)
            .first()
            .click();
    });
});
