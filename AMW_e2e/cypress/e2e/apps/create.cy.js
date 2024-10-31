describe("Apps -CRUD", () => {
  it("should create, read apps and appServers", () => {
    cy.visit("AMW_angular/#/apps", {
      auth: {
        username: "admin",
        password: "admin",
      },
    });
    cy.get('[data-cy="button-add-app"]').click({ force: true });
    cy.get("#name").type("test-app");
    cy.get('[data-cy="button-save"]').should('not.be.enabled');
    cy.get("#selectRelease").click({ force: true });
    cy.get('[data-cy="button-cancel"]').click({ force: true });
  });
});
