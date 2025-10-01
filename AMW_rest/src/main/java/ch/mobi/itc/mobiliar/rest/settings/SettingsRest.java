package ch.mobi.itc.mobiliar.rest.settings;

import ch.puzzle.itc.mobiliar.business.applicationinfo.boundary.ApplicationVersionService;
import ch.puzzle.itc.mobiliar.business.applicationinfo.entity.ApplicationBuildInfoKeyValue;
import ch.puzzle.itc.mobiliar.business.applicationinfo.entity.ConfigurationKeyValuePair;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import java.util.List;

@Stateless
@Path("/settings")
@Tag(name = "/settings", description = "Settings")
public class SettingsRest {

    @Inject
    ApplicationVersionService applicationVersionService;

    @GET
    @Produces(APPLICATION_JSON)
    @Operation(summary = "Get Liima configuration information")
    public List<ConfigurationKeyValuePair> getAppConfig() {
        return applicationVersionService.getObfuscatedApplicationConfigurationKeyValuePairs();
    }

    @GET
    @Path("/appInfo")
    @Produces(APPLICATION_JSON)
    @Operation(summary = "Get Liima application information")
    public List<ApplicationBuildInfoKeyValue> getAppInfo() {
        return applicationVersionService.getApplicationBuildInfo().getAsList();
    }
}
