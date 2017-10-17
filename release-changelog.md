# v1.14.0

## New features
* Access parent resource from template, e.g. in a resource consumed by an app `${appServer.consumedResTypes.APPLICATION?values[0].Version}` can now be written as `${$parent.Version}`: [#12](https://github.com/liimaorg/liima/issues/12)
* Liima is now compatible with Java EE 7 and is tested on Wildfy 10: [#9](https://github.com/liimaorg/liima/issues/9)
* Docker image for Liima. The image can be found on [Dockerhub](https://hub.docker.com/r/liimaorg/liima/): [#82](https://github.com/liimaorg/liima/issues/82)

## API Changes
* In this release the fields in the rest API that have been marked as depricated have been removed. Deteils see release [v1.13.0 API Changes](#api-changes-1)

# v1.13.1

This release contains mostly bug fixes and some small features.

## New features
* A new check was added to prevent that multiple resources can provide the same resource: [#112](https://github.com/liimaorg/liima/issues/112)
* Permission can now be set on domains: [#182](https://github.com/liimaorg/liima/issues/182)
* New rest methods: add resources, add resource release, copy resources, add resource relation: [#57](https://github.com/liimaorg/liima/issues/57)

## Bug fixes
* Date filter comparator gets reset when a new filter gets added: [#132](https://github.com/liimaorg/liima/issues/132)
* Under certain circumstances the wrong version was select in a new deployment: [#127](https://github.com/liimaorg/liima/issues/127), [#113](https://github.com/liimaorg/liima/issues/113)
* Added a check to redeploy which makes sure the redeployed App/AppServer matches the current App names and release: [#52](https://github.com/liimaorg/liima/issues/52) 
* In the `add related resource` popup Apps were shown: [#112](https://github.com/liimaorg/liima/issues/112)
* Under certain circumstances it was not possible to delete an AppServer: [#165](https://github.com/liimaorg/liima/issues/165)
* Fixes on the permission GUI:
  * Sorting of permission: [#164](https://github.com/liimaorg/liima/issues/164), [#152](https://github.com/liimaorg/liima/issues/152)
  * Only one permission was deleted: [#161](https://github.com/liimaorg/liima/issues/161)
* Fix a bug in user permissions that would only check the first matching permission: [#174](https://github.com/liimaorg/liima/issues/174)
* Fix wrong default method in copy from permission: [#168](https://github.com/liimaorg/liima/issues/168)
* A user could not view encrypted properties if she/he had only permissions on a certain resources/resource types: [#143](https://github.com/liimaorg/liima/issues/143)
* Properties were not encrypted correctly via rest interface: [#131](https://github.com/liimaorg/liima/issues/131)

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

## Bug fixes
* The REST service did not encrypt property values before writing it to the DB [#131](https://github.com/liimaorg/liima/issues/131)
* The Angular page `Create new deployment` showed the wrong apps and app versions for some releases [#113](https://github.com/liimaorg/liima/issues/113) and [#127](https://github.com/liimaorg/liima/issues/127)
* The log dropdown of the deployment log page is sorted again [#128](https://github.com/liimaorg/liima/issues/128)

## API Changes
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
