/**
 * Test scenario:
 * 1. add a tag called test-tag
 * 2. adding the same tag again should not be possible
 * 3. adding a different tag should work
 * 4. delete both tags again
 */

describe("Tags -CRUD", () => {
  it("should create, read and delete a tag", () => {
    cy.visit("AMW_angular/#/settings/tags", {
      auth: {
        username: "admin",
        password: "admin",
      },
    });

    cy.get("#tagName").type("test-tag{enter}");
    cy.get("#tagName").should("be.empty");
    cy.contains("Tag added");
    cy.get("#tagName").type("test-tag{enter}");
    cy.get("#tagName").should("be.empty");
    cy.contains("Tag with name test-tag already exists");
    cy.get("#tagName").clear();
    cy.get("#tagName").type("other-tag");
    cy.get('[data-cy="button-add"]').click();
    cy.get("#tagName").should("be.empty");
    cy.get('[data-cy="delete-other-tag"]').click();
    cy.contains("Tag deleted");
    cy.get('[data-cy="delete-test-tag"]').click();
    cy.contains("Tag deleted");
  });
});
