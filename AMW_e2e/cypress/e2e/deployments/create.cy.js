// szenario:
// create a deployment
// click on tracking id
// deployment must be visible in list of deployments

describe("create a deployment", () => {
  it("should create a deployment", () => {
    cy.visit("AMW_angular/#/deployments", {
      auth: {
        username: "admin",
        password: "admin",
      },
    });
    cy.get(".page-title").should("have.text", "Deployments");
    cy.get('[data-cy="create-button "]').click({ force: true });
    cy.contains("Create new deployment");
    cy.get("#selectApplicationserver").click();
    cy.contains("testapplicationserver").should("be.visible").click();
    cy.get('[data-cy="D"]').click();
    cy.get('[data-cy="I"]').click();
    const today = new Date();
    const tomorrow = new Date(today);
    tomorrow.setDate(today.getDate() + 1);
    cy.get('[data-cy="date-picker"]').type(
      tomorrow.toLocaleDateString("de-CH") + " 00:00",
    );
    cy.get('[data-cy="btn-deploy"]').click();
    cy.contains("Deployment created: Tracking Id");
    cy.contains("Tracking Id").should("be.visible").click({ force: true });
  });
});
