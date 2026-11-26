package ch.mobi.itc.mobiliar.rest.resources;

import ch.puzzle.itc.mobiliar.business.property.boundary.PropertyValueLocator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@RequestScoped
@Path("/properties/locations")
@Tag(name = "/properties", description = "Property value definitions")
public class PropertyLocationsRest {

    @Inject
    PropertyValueLocator propertyValueLocator;



    // `GET /AMW_rest/resources/properties/locations?value=test` returns a
    // JSON array of definitions with that exact, case-sensitive stored value.
    // URL-encode the `value` query parameter; `%`, `_` and `*` are literal characters,
    // not wildcards. The endpoint uses the same authentication as the other REST APIs.

    // Each result contains `propertyId`, `descriptorId`, `propertyName`, `propertyValue`,
    // `definedOn`, `ownerId`, `ownerName`, `targetId`, `targetName`, `contextId`,
    // `contextName` and `relationId`. `definedOn` is `RESOURCE`, `RESOURCE_TYPE`,
    // `CONSUMED_RELATION`, `PROVIDED_RELATION` or `RESOURCE_TYPE_RELATION`.
    // For relations, owner/target mean master/slave; for resource-type relations they
    // are resource-type IDs. `relationId` is the relation's ID, not the target's ID.
    // Target and relation fields are null for direct resource/resource-type values.
    // The IDs can be used with the existing property-update endpoints.

    // Only current, unencrypted, directly stored definitions are searched. Results do
    // not include inherited copies, descriptor defaults, audit history or unowned
    // property rows. Values are not truncated. No matches returns `200` with `[]`;
    // a missing or blank `value` returns `400`.
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Find where an exact property value is defined",
            description = "Returns stored, unencrypted property values on resources, resource types, "
                    + "consumed/provided relations and resource-type relations. Includes IDs for scripting. "
                    + "Does not resolve inheritance, defaults or audit history. No matches returns an empty list.")
    public Response getLocations(
            @Parameter(description = "Exact property value; wildcards are treated literally", required = true)
            @QueryParam("value") String value) {
        if (value == null || value.trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Property value must not be blank").build();
        }
        return Response.ok(propertyValueLocator.findByValue(value)).build();
    }
}
