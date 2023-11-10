describe("login", () => {
    it("should login with basic auth", () => {
        cy.visit("http://admin:admin@localhost:8080/AMW_web");
    });
})