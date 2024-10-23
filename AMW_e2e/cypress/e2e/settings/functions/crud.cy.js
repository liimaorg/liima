/**
 * Test scenario:
 * 1. adding a function without content should not be possible
 * 2. add a function called test-function
 * 3. adding a function with same name should not be possible
 * 4. adding function without name should not be possible
 * 5. adding a different function should work
 * 6. delete both functions again
 */

describe("Functions -CRUD", () => {
  it("should create, read and delete a function", () => {
    cy.visit("AMW_angular/#/settings/functions", {
      auth: {
        username: "admin",
        password: "admin",
      },
    });

    cy.get('[data-cy="button-add"]').click();
    cy.get("#name").type("testFunction");
    cy.get('[data-cy="button-save"]').should("be.disabled");
    cy.get(".CodeMirror-line").type("testContent");
    cy.get('[data-cy="button-save"]').click();
    cy.contains("Function saved successfully.");
    cy.get('[data-cy="button-add"]').click();
    cy.get("#name").type("testFunction");
    cy.get(".CodeMirror-line").type("differentContent");
    cy.get('[data-cy="button-save"]').click({ force: true });
    cy.contains("Function with same name already exists");
    cy.get('[data-cy="button-add"]').click();
    cy.get(".CodeMirror-line").type("testContent");
    cy.get('[data-cy="button-save"]').should("be.disabled");
    cy.get("#name").type("differentFunction");
    cy.get('[data-cy="button-save"]').click({ force: true });
    cy.get('[data-cy="button-delete-1"]').click();
    cy.get('[data-cy="button-delete"]').click();
    cy.contains("Function deleted");
    cy.get('[data-cy="button-delete-0"]').click();
    cy.get('[data-cy="button-delete"]').click();
    cy.contains("Function deleted");
  });
});
