import { expect, test } from "@playwright/test";

test.beforeEach(async ({ page }) => {
  await page.goto("./#/resource/edit?ctx=1&id=251811&selectedResourceTypeId=1");
});

test.describe.serial("Resource Edit Page - Properties", () => {
  test("should edit and save a property value", async ({ page }) => {
    await page.getByRole("button", { name: "D (TEST)" }).click();
    await page.waitForURL(/ctx=\d+/); // Wait for context switch

    const propertyInput = page.locator("#property-Version");
    await propertyInput.fill("whatever" + Math.random());
    await page.keyboard.press("Tab");

    const saveButton = page.getByTestId("button-save");
    await expect(saveButton).toBeVisible();
    expect(saveButton.isEnabled());
    await saveButton.click();
  });

  test("should reset property to parent context value", async ({ page }) => {
    await page.getByRole("button", { name: "DEV" }).click();
    await page.waitForURL(/ctx=\d+/); // Wait for context switch

    const propertyInput = page.locator("#property-Version");
    const originalValue = await propertyInput.inputValue();
    await page.getByRole("button", { name: "D (TEST)" }).click();
    await page.waitForURL(/ctx=\d+/); // Wait for context switch

    await propertyInput.fill("temporary-value");
    await page.keyboard.press("Tab");

    const resetButton = page.getByTestId("button-reset");
    await expect(resetButton).toBeVisible();
    expect(resetButton.isEnabled());
    await resetButton.click();

    await expect(propertyInput).toHaveValue(originalValue);

    const saveButton = page.getByTestId("button-save");
    await expect(saveButton).toBeVisible();
    expect(saveButton.isEnabled());
    await saveButton.click();
  });

  test("should cancel property changes", async ({ page }) => {
    await page.getByRole("button", { name: "D (TEST)" }).click();
    await page.waitForURL(/ctx=\d+/); // Wait for context switch

    const propertyInput = page.locator("#property-Version");
    const originalValue = await propertyInput.inputValue();

    await propertyInput.fill("temporary-value");
    await page.keyboard.press("Tab");

    const cancelButton = page.getByTestId("button-cancel");
    await expect(cancelButton).toBeVisible();
    await cancelButton.click();

    await expect(propertyInput).toHaveValue(originalValue);
  });

  test("should switch between contexts", async ({ page }) => {
    // Click on GLOBAL context
    await page.getByRole("button", { name: "GLOBAL" }).click();

    // Verify URL updated
    await expect(page).toHaveURL(/ctx=1/);

    // Click on D (TEST) context
    await page.getByRole("button", { name: "D (TEST)" }).click();

    // Verify URL updated (ctx should change)
    await page.waitForURL(/ctx=\d+/);
  });

  test("should disable save button when no changes", async ({ page }) => {
    await page.getByRole("button", { name: "D (TEST)" }).click();

    // Save button should not be visible when no changes
    await expect(page.getByTestId("button-save")).not.toBeVisible();
  });
});

test.describe("Resource Edit Page - Navigation", () => {
  test("should display resource name in card title", async ({ page }) => {
    await expect(page.getByRole("heading", { name: "liima" })).toBeVisible();
  });

  test("should show contexts list", async ({ page }) => {
    // Verify GLOBAL context is visible
    await expect(page.getByRole("button", { name: "GLOBAL" })).toBeVisible();
  });
});

test.describe("Resource Edit Page - Releases", () => {
  test("should have release dropdown", async ({ page }) => {
    // Find the release dropdown button
    const releaseDropdown = page.locator(".dropdown-toggle").first();
    await expect(releaseDropdown).toBeVisible();
  });
});

test.describe("Resource Edit Page - Validation", () => {
  test("should show validation errors for invalid property values", async ({
    page,
  }) => {
    await page.getByRole("button", { name: "D (TEST)" }).click();
    await page.waitForURL(/ctx=\d+/);
    const propertyInput = page.locator("#property-Version");
    await propertyInput.fill("");
    await page.keyboard.press("Tab");

    const saveButton = page.getByTestId("button-save");
    await expect(saveButton).toBeVisible(); // Wait for button to appear
    await expect(saveButton).toBeDisabled();
  });
});
