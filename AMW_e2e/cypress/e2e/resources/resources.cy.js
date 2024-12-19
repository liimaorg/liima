describe("Resources Page", () => {
  it("should navigate to the resources page", () => {
    cy.visit("AMW_angular/#/resources", {
      username: "admin",
      password: "admin",
    });

    cy.get('[data-cy="page-title"]').contains("Resources");
  });
});
