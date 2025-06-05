import {test, expect} from '@playwright/test';


test.beforeEach(async ({page}) => {
    await page.goto('./#/settings/functions');
});

test.describe('CRUD for Functions', () => {
    /**
     * Test scenario:
     * 1. adding a function without content should not be possible
     * 2. add a function called test-function
     * 3. adding a function with same name should not be possible
     * 4. adding function without name should not be possible
     * 5. adding a different function should work
     * 6. delete both functions again
     */

    test('should create a function', async ({page}) => {

        await page.getByRole('button', {name: 'Add function', exact: true}).click()

        await expect(page.getByRole('heading', {name: 'Add function'})).toBeVisible();
        await page.getByRole('textbox', {name: 'Function name'}).fill('testFunction')
        await expect(page.getByRole('button', {name: 'Save'})).toBeDisabled();
        await page.locator('css=.cm-activeLine').fill('testContent');
        await expect(page.getByRole('button', {name: 'Save'})).toBeEnabled();
        await page.getByRole('button', {name: 'Save'}).click()
        expect(page.locator('ngb-toast :has-text("Function saved successfully.")')).toBeDefined()


        await page.getByRole('button', {name: 'Add function', exact: true}).click();
        await page.getByRole('textbox', {name: 'Function name'}).fill('testFunction');
        await page.locator('css=.cm-activeLine').fill('testContent');
        await expect(page.getByRole('button', {name: 'Save'})).toBeEnabled();
        await page.getByRole('button', {name: 'Save'}).click();
        expect(page.locator('ngb-toast :has-text("Function with same name already exists.")')).toBeDefined();

        await page.getByRole('button', {name: 'Add function', exact: true}).click();

        await expect(page.getByRole('heading', {name: 'Add function'})).toBeVisible();
        await page.locator('css=.cm-activeLine').fill('testContent');
        await expect(page.getByRole('button', {name: 'Save'})).toBeDisabled();
        await page.getByRole('textbox', {name: 'Function name'}).fill('differentFunction');
        await expect(page.getByRole('button', {name: 'Save'})).toBeEnabled();
        await page.getByRole('button', {name: 'Save'}).click();
        expect(page.locator('ngb-toast :has-text("Function saved successfully.")')).toBeDefined();

        await page.getByTestId("button-delete-testFunction")?.first().click();
        await page.getByRole('button', {name: 'Delete'}).click();
        expect(page.getByRole('table').locator('td :has-text("differentFunction")')).toBeDefined();

        await page.getByTestId("button-delete-differentFunction")?.first().click();
        await page.getByRole('button', {name: 'Delete'}).click();
        expect(page.locator('ngb-toast :has-text("Function deleted.")')).toBeDefined();
    })



    test('should create, edit, compare and delete a function', async ({
                                                                          page,
                                                                      }) => {
        /**
         * Test scenario:
         * 1. adding a function without content should not be possible
         * 2. add a function called test-function-edit
         * 3. updating the same function through editing
         * 4. compare both versions of this function
         * 5. app-diff-editor component should show up
         * 6. delete the function
         */

        await page.getByTestId('button-add').click();
        await page.locator('#name').fill('testFunctionEdit');
        await expect(page.getByTestId('button-save')).toBeDisabled();
        await page.locator('css=.cm-activeLine').fill('testContentBla');
        await page.getByTestId('button-save').click();
        expect(page.locator('ngb-toast :has-text("Function saved successfully.")')).toBeDefined();

        await page.getByTestId('button-edit-testFunctionEdit')?.first().click();
        await page.locator('css=.cm-activeLine').fill('{enter}differentContent');
        await page.getByTestId('button-save').click();
        expect(page.locator('ngb-toast :has-text("Function saved successfully.")')).toBeDefined();

        await page.getByTestId('button-edit-testFunctionEdit')?.first().click();
        await page.getByTestId('button-dropdown').click();
        await page.locator('.dropdown-item').first().click();
        await expect(page.locator('app-diff-editor')).toBeVisible();
        await page.getByTestId('button-cancel').click();
        await page.getByTestId('button-delete-testFunctionEdit').first().click();
        await page.getByTestId('button-delete').click();
        expect(page.locator('ngb-toast :has-text("Function deleted.")')).toBeDefined();
    });

})

