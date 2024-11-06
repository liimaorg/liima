/**
 * Test scenario:
 * 1. add a propertyType called test-property-type with invalid regex should not be possible
 * 2. add a propertyType called test-property-type
 * 3. adding the same propertyType again should not be possible
 * 4. updating the propertyType
 * 5. delete propertyType
 */

describe("PropertyTypes -CRUD", () => {
  it("should create, read and delete a propertyType", () => {
    cy.visit("AMW_angular/#/settings/property-types", {
      auth: {
        username: "admin",
        password: "admin",
      },
    });
    cy.get('[data-cy="button-add"]').click();
    cy.get("#name").type("test-property-type");
    cy.get("#regex").type("*");
    cy.get('[data-cy="button-save"]').click();
    cy.contains("Invalid property type validation pattern.");
    cy.get('[data-cy="button-add"]').click();
    cy.get("#name").type("test-property-type");
    cy.get("#regex").type("sd");
    cy.get('[data-cy="button-save"]').click();
    cy.contains("Property type saved.");
    cy.get('[data-cy="button-add"]').click();
    cy.get("#name").type("test-property-type");
    cy.get("#regex").type("sd");
    cy.get('[data-cy="button-save"]').click();
    cy.contains("Property type already exists.");
    cy.get('[data-cy="edit-test-property-type"]').click({ force: true });
    cy.get("#encrypted").click();
    cy.get('[data-cy="button-save"]').click();
    cy.contains("Property type saved.");
    cy.get('[data-cy="delete-test-property-type"]').click({ force: true });
    cy.get('[data-cy="button-delete"]').click();
    cy.contains("Property type deleted.");
  });
});