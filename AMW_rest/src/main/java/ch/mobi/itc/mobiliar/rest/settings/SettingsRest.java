package ch.mobi.itc.mobiliar.rest.settings;

import ch.puzzle.itc.mobiliar.business.applicationinfo.boundary.ApplicationVersionService;
import ch.puzzle.itc.mobiliar.business.applicationinfo.entity.ApplicationBuildInfoKeyValue;
import ch.puzzle.itc.mobiliar.business.applicationinfo.entity.ConfigurationKeyValuePair;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.util.List;

@Stateless
@Path("/settings")
@Api(value = "/settings", description = "Settings")
public class SettingsRest {

    @Inject
    ApplicationVersionService applicationVersionService;

    @GET
    @ApiOperation(value = "Get Liima configuration information")
    public List<ConfigurationKeyValuePair> getAppConfig() {
        return applicationVersionService.getObfuscatedApplicationConfigurationKeyValuePairs();
    }

    @GET
    @Path("/appInfo")
    @ApiOperation(value = "Get Liima application information")
    public List<ApplicationBuildInfoKeyValue> getAppInfo() {
        return applicationVersionService.getApplicationBuildInfo().getAsList();
    }
}
