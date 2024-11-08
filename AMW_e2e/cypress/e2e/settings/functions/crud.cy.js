describe("Functions -CRUD", () => {

  /**
   * Test scenario:
   * 1. adding a function without content should not be possible
   * 2. add a function called test-function
   * 3. adding a function with same name should not be possible
   * 4. adding function without name should not be possible
   * 5. adding a different function should work
   * 6. delete both functions again
   */

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
    cy.get('.cm-activeLine').invoke('text', "testContent");
    cy.get('[data-cy="button-save"]').click();
    cy.contains("Function saved successfully.");
    cy.get('[data-cy="button-add"]').click();
    cy.get("#name").type("testFunction");
    cy.get('.cm-activeLine').invoke('text', "differentContent");
    cy.get('[data-cy="button-save"]').click();
    cy.contains("Function with same name already exists");
    cy.get('[data-cy="button-add"]').click();
    cy.get('.cm-activeLine').invoke('text', "testContent");
    cy.get('[data-cy="button-save"]').should("be.disabled");
    cy.get("#name").type("differentFunction");
    cy.get('[data-cy="button-save"]').click({ force: true });
    cy.get('[data-cy="icon-delete"]').first().click();
    cy.get('[data-cy="button-delete"]').click();
    cy.contains("Function deleted");
    cy.get('[data-cy="icon-delete"]').first().click();
    cy.get('[data-cy="button-delete"]').click();
    cy.contains("Function deleted");
  });

  it("should create, edit, compare and delete a function", () => {

    /**
     * Test scenario:
     * 1. adding a function without content should not be possible
     * 2. add a function called test-function-edit
     * 3. updating the same function through editing
     * 4. compare both versions of this function
     * 5. app-diff-editor component should show up
     * 6. delete the function
     */

    cy.visit("AMW_angular/#/settings/functions", {
      auth: {
        username: "admin",
        password: "admin",
      },
    });

    cy.get('[data-cy="button-add"]').click();
    cy.get("#name").type("testFunctionEdit");
    cy.get('[data-cy="button-save"]').should("be.disabled");
    cy.get('.cm-activeLine').invoke('text', "testContentBla");
    cy.get('[data-cy="button-save"]').click();
    cy.contains("Function saved successfully.");
    cy.get('[data-cy="button-edit"]').click();
    cy.get('.cm-activeLine').invoke('text', "{enter}differentContent");
    cy.get('[data-cy="button-save"]').click({ force: true });
    cy.contains("Function saved successfully.");
    cy.get('[data-cy="button-edit"]').click();
    cy.get('[data-cy="button-dropdown"]').click();
    cy.get('.dropdown-item').first().click();
    cy.get('app-diff-editor').should('be.visible');
    cy.get('[data-cy="button-cancel"]').click();
    cy.get('[data-cy="icon-delete"]').first().click();
    cy.get('[data-cy="button-delete"]').click();
    cy.contains("Function deleted");
  });
});
