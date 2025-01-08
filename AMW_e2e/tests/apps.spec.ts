import {test, expect} from '@playwright/test';

test.beforeEach(async ({page}) => {
    await page.goto('./#/apps');
});

test.describe('Create App', () => {
    test('should open add app dialog', async ({page}) => {

        await page.getByRole('button', {name: 'Add Application', exact: true}).click()

        await expect(page.getByText('Create application')).toBeVisible();
        await expect(page.getByRole('button', {name: 'Save'})).toBeDisabled();

        await page.getByRole('button', {name: 'Cancel'}).click()
    })
})

