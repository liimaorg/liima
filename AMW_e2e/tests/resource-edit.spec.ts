import { expect, test, type Page } from "@playwright/test";

async function switchContext(page: Page, contextName: string) {
  const previousUrl = page.url();
  await page.getByRole("button", { name: contextName }).click();
  await page.waitForURL((url) => url.href !== previousUrl);
  // properties reload asynchronously and discard pending edits, so wait for it to settle
  await page.waitForLoadState("networkidle");
}

test.beforeEach(async ({ page }) => {
  await page.goto("./#/resource/edit?ctx=1&id=251811&selectedResourceTypeId=1");
});

test.describe.serial("Resource Edit Page - Properties", () => {
  test("should edit and save a property value", async ({ page }) => {
    await switchContext(page, "D (TEST)");

    const propertyInput = page.getByRole("textbox", {
      name: "Version",
    });
    await propertyInput.fill("whatever" + Math.random());
    await page.keyboard.press("Tab");

    const saveButton = page.getByRole("button", { name: "Save" });
    await expect(saveButton).toBeVisible();
    await expect(saveButton).toBeEnabled();
    await saveButton.click();
  });

  test("should reset property to parent context value", async ({ page }) => {
    await switchContext(page, "DEV");

    const propertyInput = page.getByRole("textbox", { name: "Version" });
    const originalValue = await propertyInput.inputValue();
    await switchContext(page, "D (TEST)");

    await propertyInput.fill("temporary-value");
    await page.keyboard.press("Tab");

    const resetButton = page.getByTestId("button-reset");
    await expect(resetButton).toBeVisible();
    await expect(resetButton).toBeEnabled();
    await resetButton.click();

    await expect(propertyInput).toHaveValue(originalValue);

    const saveButton = page.getByRole("button", { name: "Save" });
    await expect(saveButton).toBeVisible();
    await expect(saveButton).toBeEnabled();
    await saveButton.click();
  });

  test("should cancel property changes", async ({ page }) => {
    await switchContext(page, "D (TEST)");

    const propertyInput = page.getByRole("textbox", { name: "Version" });
    const originalValue = await propertyInput.inputValue();

    await propertyInput.fill("temporary-value");
    await page.keyboard.press("Tab");

    const cancelButton = page.getByRole("button", { name: "Cancel" });
    await expect(cancelButton).toBeVisible();
    await cancelButton.click();

    await expect(propertyInput).toHaveValue(originalValue);
  });

  test("should switch between contexts", async ({ page }) => {
    await switchContext(page, "D (TEST)");
    await switchContext(page, "GLOBAL");

    await expect(page).toHaveURL(/ctx=1/);
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
  test("should show releases tile", async ({ page }) => {
    // Releases tile starts collapsed - click the header to expand it
    const releasesHeader = page.getByRole("heading", { name: "Releases" });
    await expect(releasesHeader).toBeVisible({ timeout: 15000 });
    await releasesHeader.click();
    const newReleaseButton = page.getByRole("button", {
      name: "New Release",
      exact: true,
    });
    await expect(newReleaseButton).toBeVisible({ timeout: 5000 });
    await expect(newReleaseButton).toBeEnabled();
    await expect(page.getByRole("cell", { name: "RL-" })).toBeVisible();
  });
});

test.describe("Resource Edit Page - Validation", () => {
  test("should show validation errors for invalid property values", async ({
    page,
  }) => {
    await switchContext(page, "D (TEST)");
    const propertyInput = page.getByRole("textbox", { name: "Version" });
    await propertyInput.fill("");
    await page.keyboard.press("Tab");

    const saveButton = page.getByRole("button", { name: "Save" });
    await expect(saveButton).toBeVisible(); // Wait for button to appear
    await expect(saveButton).toBeDisabled();
  });
});
