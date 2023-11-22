describe("tags-rest integration test", () => {
    it("should get all tags", () => {
        cy.request("http://admin:admin@localhost:8080/AMW_rest/resources/settings/tags")
            .then((response) => {
                const body = response.body;
                expect(body.length).to.equals(0)
/*                expect(body[0]).to.eql({
                    "id": 551,
                    "name": "a",
                    "tagType": "GLOBAL"*/
             /*   })*/
            }
        );
    });
})