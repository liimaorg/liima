describe("functions-rest integration test", () => {
    describe("getting all functions", () => {
        it("returns a list of functions", () => {
            cy.request({
                method: "GET",
                url: "http://admin:admin@localhost:8080/AMW_rest/resources/settings/functions",
                body: null,
                headers: {
                    "Content-Type": "application/json; charset=utf-8",
                },
            }).then((response) => {
                expect(response.status).eq(200);
                const functions = response.body;
            });
        });
    });
});