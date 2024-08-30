describe("property-types integration test", () => {
  describe("creating property types - validation", () => {
    it("validation fails when body is null", () => {
      cy.request({
        method: "POST",
        url: "http://admin:admin@localhost:8080/AMW_rest/resources/settings/propertyTypes",
        body: null,
        headers: {
          "Content-Type": "application/json; charset=utf-8",
        },
        failOnStatusCode: false,
      }).then((response) => {
        expect(response.status).eq(400);
        expect(response.body.message).eq("Property type must not be null.");
      });
    });

    it("validation fails when name property is null", () => {
      cy.request({
        method: "POST",
        url: "http://admin:admin@localhost:8080/AMW_rest/resources/settings/propertyTypes",
        body: { name: null, validationRegex: "string", propertyTags: [] },
        failOnStatusCode: false,
      }).then((response) => {
        expect(response.status).eq(400);
        expect(response.body.message).contains(
          "PropertyType name must not be null.",
        );
      });
    });

    it("validation fails when name property is blank", () => {
      cy.request({
        method: "POST",
        url: "http://admin:admin@localhost:8080/AMW_rest/resources/settings/propertyTypes",
        body: { name: "", validationRegex: "string", propertyTags: [] },
        failOnStatusCode: false,
      }).then((response) => {
        expect(response.status).eq(400);
        expect(response.body.message).eq(
          "PropertyType name must not be empty.",
        );
      });
    });

    it("validation fails when tag name property is blank", () => {
      cy.request({
        method: "POST",
        url: "http://admin:admin@localhost:8080/AMW_rest/resources/settings/propertyTypes",
        body: {
          name: "name",
          validationRegex: "string",
          propertyTags: [{ name: null, type: "LOCAL" }],
        },
        failOnStatusCode: false,
      }).then((response) => {
        expect(response.status).eq(400);
        expect(response.body.message).eq(
          "PropertyTag name must not be null or empty.",
        );
      });
    });
  });

  describe("deleting propertytype - validation", () => {
    it("fails if tag is not found", () => {
      cy.request({
        method: "DELETE",
        auth: {
          username: "admin",
          password: "admin",
        },
        url: "http://admin:admin@localhost:8080/AMW_rest/resources/settings/propertyTypes/-1",
        failOnStatusCode: false,
      }).then((response) => {
        expect(response.status).eq(404);
      });
    });
  });

  describe("working with propertytypes", () => {
    const name = `test-propertyType${new Date().getMilliseconds()}`;

    it("create a new propertytype, list them, update it and delete it afterwards", () => {
      cy.request({
        method: "POST",
        url: "http://admin:admin@localhost:8080/AMW_rest/resources/settings/propertyTypes",
        body: {
          name: name,
          validationRegex: "string",
          propertyTags: [{ name: name, type: "LOCAL" }],
        },
      }).then((response) => {
        expect(response.status).eq(201);
        const location = response.headers.location;
        cy.request({
          method: "GET",
          url: "http://admin:admin@localhost:8080/AMW_rest/resources/settings/propertyTypes",
        }).then((response) => {
          expect(response.status).eq(200);
          const propertyTypes = response.body;
          expect(
            propertyTypes.filter((propertyTyp) => propertyTyp.name === name)
              .length,
          ).eq(1);
        });
        cy.request({
          method: "PUT",
          url: location,
          auth: {
            username: "admin",
            password: "admin",
          },
          body: {
            name: name,
            validationRegex: "string3",
            encryption: true,
            propertyTags: [{ name: name, type: "LOCAL" }],
          },
        }).then((response) => {
          expect(response.status).eq(200);
          cy.request({
            method: "DELETE",
            url: location,
            auth: {
              username: "admin",
              password: "admin",
            },
          }).then((response) => {
            expect(response.status).eq(204);
            cy.request({
              method: "GET",
              url: "http://admin:admin@localhost:8080/AMW_rest/resources/settings/propertyTypes",
            }).then((response) => {
              expect(
                response.body.filter((propertyTyp) => propertyTyp.name === name)
                  .length,
              ).eq(0);
            });
          });
        });
      });
    });
  });
});
