# v1.13.0

This release contains a new and enhanced permission system and bugfixes.

## New features
* New permission system [#25](https://github.com/liimaorg/liima/issues/25): the permission system has been rewriten to allow for permissions to be scoped by environement, resource and resource type. See [documentation](https://github.com/liimaorg/docs/blob/master/content/permissions.md) for more details. Important:
  * Migration to the new permission system happens automatically via Liquibase. Please check if the permissions of the roles are still correct after the migration.
  * The role app_developer can now also rename applications. This is because rename is part of the update action.
  * The role app_developer can no longer edit encrypted properties by default.
* Resource relation names are now editable [#78](https://github.com/liimaorg/liima/issues/78)
* New REST function to add relations to resources [#51](https://github.com/liimaorg/liima/issues/51)
* New REST function `GET /resources/{resourceGroupName}/lte/{releaseName}` that fetches the lower or equal release to the given one [#86](https://github.com/liimaorg/liima/issues/86)

## Bugfixes
* The REST service did not encrypt property values before writing it to the DB [#131](https://github.com/liimaorg/liima/issues/131)
* The Angular page `Create new deployment` showed the wrong apps and app versions for some releases [#113](https://github.com/liimaorg/liima/issues/113) and [#127](https://github.com/liimaorg/liima/issues/127)
* The log dropdown of the deployment log page is sorted again [#128](https://github.com/liimaorg/liima/issues/128)

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
