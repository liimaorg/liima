package ch.mobi.itc.mobiliar.rest.settings;

import ch.mobi.itc.mobiliar.rest.dtos.ConfigurationDTO;
import ch.puzzle.itc.mobiliar.business.applicationinfo.boundary.ApplicationVersionService;
import ch.puzzle.itc.mobiliar.business.applicationinfo.entity.ConfigurationKeyValuePair;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.util.ArrayList;
import java.util.List;

@Stateless
@Path("/settings")
@Api(value = "/settings", description = "Settings")
public class SettingsRest {

    final static String OBFUSCATED = "***************";

    @Inject
    ApplicationVersionService applicationVersionService;

    @GET
    @ApiOperation(value = "Get Liima configuration information")
    public List<ConfigurationKeyValuePair>  getAppConfig() {
        return applicationVersionService.getObfuscatedApplicationConfigurationKeyValuePairs();
    }
/*    public List<ConfigurationDTO> getAppInfo() {
        List<ConfigurationKeyValuePair> configurationKeyValuePairs = applicationVersionService.getApplicationConfigurationInfo().getConfigurationKeyValuePairs();
        List<ConfigurationDTO> configurations = new ArrayList<>();
        for (ConfigurationKeyValuePair keyValuePair : configurationKeyValuePairs) {
            configurations.add(createConfigurationDTO(keyValuePair));
        }
        return configurations;
    }*/

    protected ConfigurationDTO createConfigurationDTO(ConfigurationKeyValuePair keyValuePair) {
        ConfigurationDTO configurationDTO = new ConfigurationDTO();
        configurationDTO.setKey(keyValuePair.getKey().getValue());
        if (keyValuePair.getKey().isSecretValue()) {
            configurationDTO.setValue(OBFUSCATED);
        } else {
            configurationDTO.setValue(keyValuePair.getValue());
        }
        configurationDTO.setEnv(keyValuePair.getKey().getEnvName());
        configurationDTO.setDefaultValue(keyValuePair.getDefaultValue());
        return configurationDTO;
    }
}
