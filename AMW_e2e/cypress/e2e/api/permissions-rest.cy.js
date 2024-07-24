describe("permissions-rest integration test", () => {
  describe("list restriction - validation", () => {
    it("list restriction fails with viewer user", () => {
      cy.request({
        method: "GET",
        url: "http://viewer:viewer@localhost:8080/AMW_rest/resources/permissions/restrictions",
        body: null,
        headers: {
          "accept": "application/json"
        },
        failOnStatusCode: false,
      }).then((response) => {
        expect(response.status).eq(403);
        expect(response.body.message).eq("Not Authorized!");
      });
    });
    it("list own restrictions succeeds with viewer user", () => {
      cy.request({
        method: "GET",
        url: "http://admin:admin@localhost:8080/AMW_rest/resources/permissions/restrictions/ownRestrictions",
        body: null,
        headers: {
          "accept": "application/json"
        },
        failOnStatusCode: false,
      }).then((response) => {
        expect(response.status).eq(200);
        expect(response.body).length.greaterThan(50);
      });
    });
    it("list restrictions succeeds with admin user", () => {
      cy.request({
        method: "GET",
        url: "http://admin:admin@localhost:8080/AMW_rest/resources/permissions/restrictions",
        body: null,
        headers: {
          "accept": "application/json"
        },
        failOnStatusCode: false,
      }).then((response) => {
        expect(response.status).eq(200);
        expect(response.body).length.greaterThan(50);
      });
    });

  });
});
