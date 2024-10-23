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
   cy.get('.CodeMirror-line').type("testContent");
   cy.get('[data-cy="button-save"]').click();
   cy.wait(5000);
   cy.get('[data-cy="button-add"]').click();
   cy.get("#name").type("testFunction");
   cy.get('.CodeMirror-line').type("differentContent");
   cy.get('[data-cy="button-save"]').click()
   cy.contains("Function with same name already exists");
   cy.wait(15000);
   cy.get('[data-cy="button-add"]').click();
   cy.get('.CodeMirror-line').type("testContent");
   cy.get('[data-cy="button-save"]').should("be.disabled");
   cy.get("#name").type("differentFunction");
   cy.get('[data-cy="button-save"]').click();
   cy.get('[data-cy="button-delete"]').click();
/*    cy.get("#tagName").should("be.empty");
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
    cy.contains("Tag deleted");*/
  });
});
