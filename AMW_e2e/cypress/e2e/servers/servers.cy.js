describe("Servers Page", () => {
  it("should navigate to the servers page", () => {
    cy.visit("AMW_angular/#/servers", {
      auth: {
        username: "admin",
        password: "admin",
      },
    });

    cy.get('[data-cy="page-title"]').contains("Servers");
  });
});
