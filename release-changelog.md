# v1.13

## API Changes / Important for peripheral systems 
* In ch.mobi.itc.mobiliar.rest.dtos.ResourceRelationDTO the new property *relationName* will replace the property *identifier*. The property *identifier* will be removed by v1.14. The following REST URIs are concerned:
  * /resources
  * /resources/{resourceGroupName}/{releaseName}
  * /resources/{resourceGroupName}/lte/{releaseName}
  * /resources/{resourceGroupName}/{releaseName}/relations
  * /resources/{resourceGroupName}
  * /resources/resourceGroups/{resourceGroupId}/releases/mostRelevant/
  * /resources/resourceGroups/{resourceGroupId}/releases/
  * /resources/resourceGroups/{resourceGroupId}/releases/{releaseId}
* appsWith**Mvn**Version in the ch.mobi.itc.mobiliar.rest.dtos.DeploymentDTO is depricated and will be removed in v1.14. It was replaced with appsWithVersion in v1.12. This change concerns all /deployment REST URIs. New and old deployment JSON:
```
{
  "id": 281100,
  "trackingId": 259609,
  "state": "progress",
  "deploymentDate": 1501145413850,
  "appServerName": "liima",
  # new
  "appsWithVersion": [
    {
      "applicationName": "org.liima.test",
      "version": "4.1.0-SNAPSHOT"
    }
  ],
  # old, depricated
  "appsWithMvnVersion": [
    {
      "applicationName": "org.liima.test",
      "mavenVersion": "4.1.0-SNAPSHOT"
    }
  ],
}
```
