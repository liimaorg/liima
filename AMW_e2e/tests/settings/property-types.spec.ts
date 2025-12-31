import { test, expect } from '@playwright/test';

test.beforeEach(async ({ page }) => {
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
        const propertyTypeName = `test-property-type-${crypto.randomUUID()}`;

        await page.getByTestId('button-add').click();
        await page.locator('#name').fill(propertyTypeName);
        await page.locator('#regex').fill('*');
        // TODO: save should not be enabled when regex is wrong?
        await page.getByTestId('button-save').click();
        await expect(page.locator('ngb-toast').filter({ hasText: 'Invalid property type validation pattern.' })).toBeVisible();
        // close ngb-toast explicitly to not overlap edit/delete buttons
        await page.getByTestId('toast-close').last().click();
        await expect(page.locator('ngb-toast')).toHaveCount(0);
        await expect(page.getByRole('row').filter({ hasText: propertyTypeName })).toHaveCount(0);
        await expect(page.locator('body.modal-open')).toHaveCount(0);

        await page.getByTestId('button-add').click();
        await page.locator('#name').fill(propertyTypeName);
        await page.locator('#regex').fill('sd');
        await expect(page.getByTestId('button-save')).toBeEnabled();
        await page.getByTestId('button-save').click();
        await expect(page.locator('ngb-toast').filter({ hasText: 'Property type saved.' })).toBeVisible();
        await page.getByTestId('toast-close').last().click();
        await expect(page.locator('ngb-toast')).toHaveCount(0);
        await expect(page.locator('body.modal-open')).toHaveCount(0);

        await page.getByTestId('button-add').click();
        await page.locator('#name').fill(propertyTypeName);
        await page.locator('#regex').fill('sd');
        await expect(page.getByTestId('button-save')).toBeEnabled();
        await page.getByTestId('button-save').click();
        await expect(page.locator('ngb-toast').filter({ hasText: 'Property type already exists.' })).toBeVisible();
        await page.getByTestId('toast-close').last().click();
        await expect(page.locator('ngb-toast')).toHaveCount(0);
        await expect(page.locator('body.modal-open')).toHaveCount(0);

        const editButton = page.getByRole('row', { name: propertyTypeName }).getByRole('button').first();
        await editButton.click();
        // Wait for the modal content to be visible
        await expect(page.locator('.modal-dialog')).toBeVisible();
        await expect(page.locator('#encrypted')).toBeVisible();
        await page.locator('#encrypted').click();
        await expect(page.getByTestId('button-save')).toBeEnabled();
        await page.getByTestId('button-save').click();
        await expect(page.getByText(/Property type saved\./)).toBeVisible();
        await page.getByTestId('toast-close').last().click();
        await expect(page.locator('ngb-toast')).toHaveCount(0);
        await expect(page.locator('body.modal-open')).toHaveCount(0);

        const deleteButton = page.getByRole('row', { name: propertyTypeName }).getByRole('button').last();
        await deleteButton.click();
        await expect(page.getByTestId('button-delete')).toBeVisible();
        await page.getByTestId('button-delete').click();
        await expect(page.locator('ngb-toast').filter({ hasText: 'Property type deleted.' })).toBeVisible();
        await page.getByTestId('toast-close').last().click();
        await expect(page.locator('ngb-toast')).toHaveCount(0);
    });
});
