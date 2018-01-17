# v1.16.1
This release contains bug fixes and one new feature.

## New featrue
* Rest: multiple permissions in one post [#258](https://github.com/liimaorg/liima/issues/258)
  * Preparation for the new create permission GUI: Permission GUI: make it simple to create multiple permissions [#259](https://github.com/liimaorg/liima/issues/259)

## Bug fixes
* Add relation REST endpoint throws NullPointer exception [#320](https://github.com/liimaorg/liima/issues/320)
* Error: "Was not able to decrypt properties" after setting encrypted property over REST [#322](https://github.com/liimaorg/liima/issues/322)

# v1.16.0
This release adds some new features, fixes bugs and updates the used JavaScript libraries. Highlight is the new configuration overview that shows where on a resource properties have been overwritten.

## New features
* Configuration overview [#77](https://github.com/liimaorg/liima/issues/77)
  * The resource screen now shows where resource properties are overwritten in sub environments.
* Force delete PropertyDescriptor [#95](https://github.com/liimaorg/liima/issues/95)
  * If deleting a PropertyDescriptor doesn't work it can now be force deleted, deleting all Property values.
* Delegation of permission [#74](https://github.com/liimaorg/liima/issues/74)
  * Usage see [doc](https://github.com/liimaorg/docs/blob/master/content/permissions.md#selbstverwaltung-von-permissions)
  
## Bug fixes
* Deployment Excel Export: remove newline at the end of Applications column [#262](https://github.com/liimaorg/liima/issues/262)
* editResourceView does not refresh correctly after "Copy from resource" [#222](https://github.com/liimaorg/liima/issues/222)
* Permission Page TypeError: Cannot read property 'name' of undefined [#299](https://github.com/liimaorg/liima/issues/299)
  * Sometimes no permission where shown.
* Rename a resource with "-" to "\_" doesn't work [#223](https://github.com/liimaorg/liima/issues/223)
* deployments: reset Add filter after add bug [#306](https://github.com/liimaorg/liima/issues/306)
* REST resource '/resources' returns duplicates of resources if query parameter 'type' is not set [#213](https://github.com/liimaorg/liima/issues/213)
* AppServer can't be deleted [#284](https://github.com/liimaorg/liima/issues/284)
* Permissions: prevent adding the same permission multiple times [#260](https://github.com/liimaorg/liima/issues/260)
* Prevent potential XSS in success and error messages [#286](https://github.com/liimaorg/liima/issues/286)
* Update NodeJS and dependencies [#290](https://github.com/liimaorg/liima/issues/290), [#292](https://github.com/liimaorg/liima/issues/292), [#300](https://github.com/liimaorg/liima/issues/300)
* Update to Angular 4 [#71](https://github.com/liimaorg/liima/issues/71)
* Replace NPM with Yarn [#275](https://github.com/liimaorg/liima/issues/275)

# v1.15.2
Fixes some bugs on the new Angular deployment view and csv export.

## Bug fixes
* Confirming and changing the date of a deployment doesn't work via edit [#271](https://github.com/liimaorg/liima/issues/271)
* Deployment environment filter has wrong suggestions [#257](https://github.com/liimaorg/liima/issues/257)
* Deployment csv export: sort order in csv is slightly different from view [#268](https://github.com/liimaorg/liima/issues/268)
* editResourceView Properties not visible with window zoom [#251](https://github.com/liimaorg/liima/issues/251)
* Deployment filters come back after removing them [#245](https://github.com/liimaorg/liima/issues/245)
* Deployment filter are no longer written to the url [#250](https://github.com/liimaorg/liima/issues/250)
* Angular resources are cached forever [#255](https://github.com/liimaorg/liima/issues/255)
* Update swagger ui, the source is no longer checked into liima [#256](https://github.com/liimaorg/liima/issues/256)

# v1.15.1
Fixes some bugs on the new Angular deployment view.

## Bug fixes
* AMW_angular/#/deployments: filters come back [#245](https://github.com/liimaorg/liima/issues/245)
* AMW_angular/#/deployments: progress animation should also be shown on state progress [#244](https://github.com/liimaorg/liima/issues/244)
* AMW_angular/#/deployments: if filter is set, log view can not be shown and gives a http 500 [#243](https://github.com/liimaorg/liima/issues/243)

# v1.15.0

## New features
* Deployment screen was rewriten in angular: [#89](https://github.com/liimaorg/liima/issues/89)
  * Deployment Filters are now writen to the url automatically and can be bookmarked or copied to the clipboard:  [#35](https://github.com/liimaorg/liima/issues/35)
  * The most common deployment failure reasons are now shown directly on the deployment: [#75](https://github.com/liimaorg/liima/issues/75)
  * To whole deployment row is now colored if deployment failed (red) or is successful (green).
  * More space for the deployment table.
* To preserve the deployment histroy deployment entries are no longer deleted if an AppServer, Environment, Release or Runtime is deleted: [#79](https://github.com/liimaorg/liima/issues/79)

## Bug fixes
* Deployment filter: "Latest deployment job for ..." shows two deployments for one env: [#23](https://github.com/liimaorg/liima/issues/23)
  * Unfortunately this make the filter slower as paging happens now in code instead of SQL.
* The deployment filters "Latest deployment job for App & State" and "State filter" didn't work correctly together: [#42](https://github.com/liimaorg/liima/issues/42)
* Edit Resource View of AppServer: NODE not selectable: [#207](https://github.com/liimaorg/liima/issues/207)
* AMW_angular/#/deployment/: redeploy sometimes doesn't set an environment: [#122](https://github.com/liimaorg/liima/issues/122)

## API Changes
* Added multiple new REST endpoints for the new deployment screen, see Swagger for details.
* `GET /deployments` was replaced with `GET /deployments/filter` and is now depricated.

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
