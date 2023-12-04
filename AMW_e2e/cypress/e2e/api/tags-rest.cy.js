describe("tags-rest integration test", () => {
  describe("creating tags - validation", () => {
    it("validation fails when body is null", () => {
      cy.request({
        method: "POST",
        url: "http://admin:admin@localhost:8080/AMW_rest/resources/settings/tags",
        body: null,
        headers: {
          "Content-Type": "application/json; charset=utf-8",
        },
        failOnStatusCode: false,
      }).then((response) => {
        expect(response.status).eq(400);
        expect(response.body).eq("TagDTO must not be null");
        const body = response.body;
      });
    });

    it("validation fails when name property is null", () => {
      cy.request({
        method: "POST",
        url: "http://admin:admin@localhost:8080/AMW_rest/resources/settings/tags",
        body: { name: null },
        failOnStatusCode: false,
      }).then((response) => {
        expect(response.status).eq(400);
        expect(response.body).eq("Tag name must not be null or empty");
      });
    });

    it("validation fails when name property is blank", () => {
      cy.request({
        method: "POST",
        url: "http://admin:admin@localhost:8080/AMW_rest/resources/settings/tags",
        body: { name: "" },
        failOnStatusCode: false,
      }).then((response) => {
        expect(response.status).eq(400);
        expect(response.body).eq("Tag name must not be null or empty");
      });
    });
  });

  describe("deleting tags - validation", () => {
    it("fails if tag is not found", () => {
      cy.request({
        method: "DELETE",
        auth: {
          username: "admin",
          password: "admin",
        },
        url: "http://admin:admin@localhost:8080/AMW_rest/resources/settings/tags/-1",
        failOnStatusCode: false,
      }).then((response) => {
        expect(response.status).eq(400);
      });
    });
  });

  describe("working with tags", () => {
    const tagName = `test-tag${new Date().getMilliseconds()}`;

    it("create a new tag, list them all and delete it afterwards", () => {
      cy.request({
        method: "POST",
        url: "http://admin:admin@localhost:8080/AMW_rest/resources/settings/tags",
        body: {
          name: tagName,
          failOnStatusCode: false,
        },
      }).then((response) => {
        expect(response.status).eq(201);
        const location = response.headers.location;
        cy.request({
          method: "GET",
          url: "http://admin:admin@localhost:8080/AMW_rest/resources/settings/tags",
        }).then((response) => {
          expect(response.status).eq(200);
          const tags = response.body;
          expect(tags.filter((tag) => tag.name === tagName).length).eq(1);
          cy.request({
            method: "DELETE",
            url: location,
            auth: {
              username: "admin",
              password: "admin",
            },
          }).then((response) => {
            expect(response.status).eq(200);
            cy.request({
              method: "GET",
              url: "http://admin:admin@localhost:8080/AMW_rest/resources/settings/tags",
            }).then((response) => {
              expect(
                response.body.filter((tag) => tag.name === tagName).length,
              ).eq(0);
            });
          });
        });
      });
    });
  });
});
