# v1.18.6
* fix(rest): add produces csv annotation to deployment rest, consolidate servers and hostNames endpoint [#913](https://github.com/liimaorg/liima/pull/913)

# v1.18.5
* chore(deps): update JavaScript dependencies [#912](https://github.com/liimaorg/liima/pull/912)
* Improve permission cache management with revision-based invalidation [#911](https://github.com/liimaorg/liima/pull/911)
* run ng test in separate maven phase so tests can be skipped via -DskipTests [#910](https://github.com/liimaorg/liima/pull/910)
* chore(deps): bump playwright and @playwright/test in /AMW_e2e [#909](https://github.com/liimaorg/liima/pull/909)
* fix(resources rest): /resources/{resourceGroupName}/{releaseName} hiding /resources/exists/{resourceId} [#908](https://github.com/liimaorg/liima/pull/908)
* swagger fix and cleanup [#907](https://github.com/liimaorg/liima/pull/907)
* chore(deps): bump tar-fs from 3.1.0 to 3.1.1 in /AMW_angular/io [#906](https://github.com/liimaorg/liima/pull/906)

# v1.18.4
* resources -> New Resource doesn't check permission correctly [#893](https://github.com/liimaorg/liima/issues/893)
* ADD_ADMIN_PERMISSIONS_ON_CREATED_RESOURCE doesn't work anymore [#892](https://github.com/liimaorg/liima/issues/892)
* release of /AMW_rest/resources/resources/<name>/lte/<release> is now case sensitive [#891](https://github.com/liimaorg/liima/issues/891)
* feature: return to selected resource type [#878](https://github.com/liimaorg/liima/pull/878)
* Update Angular and JavaScript deps [#897](https://github.com/liimaorg/liima/issues/897)
* Angular: fix some lint errors [#900](https://github.com/liimaorg/liima/issues/900)
* Rest: switch to OpenAPI [#903](https://github.com/liimaorg/liima/issues/903)

# v1.18.3
* extend test database with more example data [#890](https://github.com/liimaorg/liima/pull/890)
* chore: bump form-data from 4.0.2 to 4.0.4 in /AMW_e2e [#889](https://github.com/liimaorg/liima/pull/889)
* fix: fix the check if release already exists and actually create it [#888](https://github.com/liimaorg/liima/pull/888)
* chore: bump on-headers and compression in /AMW_angular/io [#887](https://github.com/liimaorg/liima/pull/887)
* fix: ResourceGroupsRest get resource with type id shadowing get resource with type name [#885](https://github.com/liimaorg/liima/pull/885)
* chore: bump org.apache.commons:commons-lang3 from 3.10 to 3.18.0 [#884](https://github.com/liimaorg/liima/pull/884)

Work continues on the edit resource screen. It is currently hidden behind a feature toggle only active for config_admins. Permission name is `ANGULAR_EDIT_RESOURCE`.
* Merge development into master [#886](https://github.com/liimaorg/liima/pull/886)
* feat: wrap line [#879](https://github.com/liimaorg/liima/pull/879)
* Link to migrated resource edit pages in table component [#877](https://github.com/liimaorg/liima/pull/877)
* Remove dataCyNameKey from table component [#874](https://github.com/liimaorg/liima/pull/874)
* fix: sorting of resource and resource type templates [#849](https://github.com/liimaorg/liima/pull/849)
* feat: resource release dropdown [#848](https://github.com/liimaorg/liima/pull/848)
* fix: Session/EntityManager is closed [#847](https://github.com/liimaorg/liima/pull/847)
* feat: compare revision component [#846](https://github.com/liimaorg/liima/pull/846)
* feat: edit resource add edit template [#828](https://github.com/liimaorg/liima/pull/828)
* feat: delete resource templates [#826](https://github.com/liimaorg/liima/pull/826)
* feat: improve input validation for new resource [#825](https://github.com/liimaorg/liima/pull/825)
* refactor: simplify endpoint paths in ResourceTypesRest [#824](https://github.com/liimaorg/liima/pull/824)
* feat: list resource templates [#821](https://github.com/liimaorg/liima/pull/821)
* feat: delete function [#820](https://github.com/liimaorg/liima/pull/820)
* feat: add edit functions [#819](https://github.com/liimaorg/liima/pull/819)
* feat: list functions [#818](https://github.com/liimaorg/liima/pull/818)
* feat: edit resource page [#815](https://github.com/liimaorg/liima/pull/815)

# v1.18.2
* feat: turn servers and apps filters into form so enter works [#881](https://github.com/liimaorg/liima/pull/881)
* fix: improve performance of permissions table by mapping icon directly in datasource [#882](https://github.com/liimaorg/liima/pull/882)

# v1.18.1
* fix(restriction-list): use signals for restrictionsHeader to prevent unnecessary rerenders [#880](https://github.com/liimaorg/liima/pull/880)

# v1.18.0
* All screens except for Resource and Resource type edit where migrated to Angular.
* Various updates of dependencies.

## BREAKING CHANGEs
* Favorites and Shakedown Test where removed from Liima.

## Pull Requests
* UI improvements [#876](https://github.com/liimaorg/liima/pull/876)
* chore(deps): bump com.fasterxml.jackson.core:jackson-core from 2.12.6 to 2.15.0 [#873](https://github.com/liimaorg/liima/pull/873)
* fix: JSF set empty string relation name instead of null, add check [#872](https://github.com/liimaorg/liima/pull/872)
* fix: redirect to angular page when last release of res is deleted [#868](https://github.com/liimaorg/liima/pull/868)
* fix: don't show appserver select on redeploy, group together @ifs [#867](https://github.com/liimaorg/liima/pull/867)
* chore(deps): bump webpack-dev-server and @angular-devkit/build-angular in /AMW_angular/io [#866](https://github.com/liimaorg/liima/pull/866)
* chore(deps-dev): bump axios from 1.7.4 to 1.9.0 in /AMW_e2e [#865](https://github.com/liimaorg/liima/pull/865)
* {hostName} will be replaced in server url, fix js config lookup [#864](https://github.com/liimaorg/liima/pull/864)
* Remove duplicate Applicationserver label in Redeployments [#863](https://github.com/liimaorg/liima/pull/863)
* replace cypress with playwright for e2e-tests [#862](https://github.com/liimaorg/liima/pull/862)
* Ensure UI update after permissions deletion [#861](https://github.com/liimaorg/liima/pull/861)
* Prevent initial loading of servers [#860](https://github.com/liimaorg/liima/pull/860)
* chore(deps): bump tar-fs from 3.0.8 to 3.0.9 in /AMW_angular/io [#859](https://github.com/liimaorg/liima/pull/859)
* chore(deps): bump koa from 2.15.3 to 2.16.1 in /AMW_angular/io [#857](https://github.com/liimaorg/liima/pull/857)
* Update angular, bootstrap and other js deps [#856](https://github.com/liimaorg/liima/pull/856)
* chore(deps): bump @babel/runtime and @angular-devkit/build-angular in /AMW_angular/io [#853](https://github.com/liimaorg/liima/pull/853)
* fix: final review changes [#850](https://github.com/liimaorg/liima/pull/850)
* fix: appServer-dropdown when creating apps [#844](https://github.com/liimaorg/liima/pull/844)
* fix: apps release link on apps page [#842](https://github.com/liimaorg/liima/pull/842)
* feat: improve ui, make app table more consistent with the others [#841](https://github.com/liimaorg/liima/pull/841)
* Bugfix/1077 apps page reload pagination [#840](https://github.com/liimaorg/liima/pull/840)
* Refactor/app page [#839](https://github.com/liimaorg/liima/pull/839)
* Feature/1070 table component [#838](https://github.com/liimaorg/liima/pull/838)
* Servers page fix: host urls [#837](https://github.com/liimaorg/liima/pull/837)
* Refactor/1089 use signals for settings releases [#836](https://github.com/liimaorg/liima/pull/836)
* Settings page fixes: release dates, save new functions [#835](https://github.com/liimaorg/liima/pull/835)
* Apps and Servers page fixes: add table filters to url [#834](https://github.com/liimaorg/liima/pull/834)
* Apps page fix: loading spinner [#833](https://github.com/liimaorg/liima/pull/833)
* Apps and Deployment page fixes: list Applicationservers, display toast errors [#831](https://github.com/liimaorg/liima/pull/831)
* Settings page fixes: tab naming, environments page selection [#830](https://github.com/liimaorg/liima/pull/830)
* Resources page fixes [#829](https://github.com/liimaorg/liima/pull/829)
* Angular: make tables more similar, fix some UI bugs [#827](https://github.com/liimaorg/liima/pull/827)
* update js deps [#817](https://github.com/liimaorg/liima/pull/817)
* Development [#813](https://github.com/liimaorg/liima/pull/813)
* fix: adjust navigation and delete unused code [#812](https://github.com/liimaorg/liima/pull/812)
* fix: links and add sorting for runtimes [#811](https://github.com/liimaorg/liima/pull/811)
* fix: small issues on apps-page [#810](https://github.com/liimaorg/liima/pull/810)
* Feat/1049 styling resource page [#809](https://github.com/liimaorg/liima/pull/809)
* Feat/1045 tile component [#808](https://github.com/liimaorg/liima/pull/808)
* Feat/1046 dummy page [#807](https://github.com/liimaorg/liima/pull/807)
* Feat/1039 fix resource navigation links [#806](https://github.com/liimaorg/liima/pull/806)
* feat: add loading component when searching servers with the filter [#805](https://github.com/liimaorg/liima/pull/805)
* feature: Create new resourcetype [#804](https://github.com/liimaorg/liima/pull/804)
* Feature/943 delete resource type [#803](https://github.com/liimaorg/liima/pull/803)
* Feature/942 add new resource [#802](https://github.com/liimaorg/liima/pull/802)
* Feature/1031 add input field when adding new env [#801](https://github.com/liimaorg/liima/pull/801)
* feat: new ui settings-environment (show alias of env) [#800](https://github.com/liimaorg/liima/pull/800)
* feat: change routing from settings [#799](https://github.com/liimaorg/liima/pull/799)
* Feature/940 resources listview [#798](https://github.com/liimaorg/liima/pull/798)
* Fix/apps page [#797](https://github.com/liimaorg/liima/pull/797)
* refactor: remove inital server load [#796](https://github.com/liimaorg/liima/pull/796)
* refactor: align styling for servers page #1035 [#795](https://github.com/liimaorg/liima/pull/795)
* chore: update to Mockito to 4.11.0 [#794](https://github.com/liimaorg/liima/pull/794)
* Feature/1004 servers implement filter search [#793](https://github.com/liimaorg/liima/pull/793)
* chore(deps): bump org.hibernate.validator:hibernate-validator from 6.1.5.Final to 6.2.0.Final [#792](https://github.com/liimaorg/liima/pull/792)
* Feature/1022 codemirror v6 [#791](https://github.com/liimaorg/liima/pull/791)
* chore: remove sideeffect from permissions compution #789 [#790](https://github.com/liimaorg/liima/pull/790)
* Fix/permissions signal [#789](https://github.com/liimaorg/liima/pull/789)
* feature: Resources page with routing and list of resource types [#788](https://github.com/liimaorg/liima/pull/788)
* Tidying application-info component [#787](https://github.com/liimaorg/liima/pull/787)
* feat: set preselected release to upcoming release in app-filter [#786](https://github.com/liimaorg/liima/pull/786)
* Button component [#785](https://github.com/liimaorg/liima/pull/785)
* Feature/796: add functionality to environments page with new ui [#784](https://github.com/liimaorg/liima/pull/784)
* Modal-header component [#783](https://github.com/liimaorg/liima/pull/783)
* Show list of servers [#782](https://github.com/liimaorg/liima/pull/782)
* feat: add environments page with new ui without functionality [#781](https://github.com/liimaorg/liima/pull/781)
* new servers page with routing [#780](https://github.com/liimaorg/liima/pull/780)
* chore(deps): bump cookie, socket.io and express in /AMW_angular/io [#779](https://github.com/liimaorg/liima/pull/779)
* Screen Apps [#778](https://github.com/liimaorg/liima/pull/778)
* Run Test Suites for Development Branch [#776](https://github.com/liimaorg/liima/pull/776)
* chore(deps): bump commons-io:commons-io from 2.7 to 2.14.0 in /AMW_business [#775](https://github.com/liimaorg/liima/pull/775)
* Feature/769 angular functions [#774](https://github.com/liimaorg/liima/pull/774)
* Remove ShakedownTest and Testing [#770](https://github.com/liimaorg/liima/pull/770)
* fix: permission handling in components [#768](https://github.com/liimaorg/liima/pull/768)
* Remove My favorites [#766](https://github.com/liimaorg/liima/pull/766)
* Feature/740 angular property types [#765](https://github.com/liimaorg/liima/pull/765)
* Angular signals [#763](https://github.com/liimaorg/liima/pull/763)
* Remove karma and fix vunerabilities [#762](https://github.com/liimaorg/liima/pull/762)

# v1.17.36
* Fix: Permissions can be changed by unprivileged users: https://github.com/liimaorg/liima/security/advisories/GHSA-cghr-2r42-868j
* Update JavaScript dependencies
* Cleanup Java Script Imports of NgIf and NgFor
* fix: allow to add node to AppServer with update resouce permission [#760](https://github.com/liimaorg/liima/pull/760)

# v1.17.35
* Replace JSF GUI in settings/application-info page with Angular [#725](https://github.com/liimaorg/liima/issues/725)
* Replace JSF GUI in Settings -> Releases page with Angular [#729](https://github.com/liimaorg/liima/issues/729)
* Replace JSF GUI in Settings -> Deployment Parameter page with Angular [#737](https://github.com/liimaorg/liima/issues/737)
* Upgrade to Angular 17.1 and switch to new Vite build [#734](https://github.com/liimaorg/liima/issues/734)
* Migrate to angular17 control-flow [#727](https://github.com/liimaorg/liima/issues/727)
* Angular 18 Upgrade [#755](https://github.com/liimaorg/liima/issues/755)
* Angular: create ToastContainer Component [#744](https://github.com/liimaorg/liima/issues/744)
* Limit deployment log file size to 10MB [#413](https://github.com/liimaorg/liima/issues/413)
* Node and js version update, GH action updates [#741](https://github.com/liimaorg/liima/pull/741)

# v1.17.34
* Angular Cleanup and Consolidation [#723](https://github.com/liimaorg/liima/pull/723)
* fix nullpointer in notification service with preserved deployments [#724](https://github.com/liimaorg/liima/pull/724)

# v1.17.33
* Settings -> Tags migrated from Angular to JSF: [#719](https://github.com/liimaorg/liima/pull/719)
* Angular deployments: missing colors of deployment entries [#718](https://github.com/liimaorg/liima/issues/718)
* Fix error when sending mail for archived deployment, fix sending mail on predeploy failure [#721](https://github.com/liimaorg/liima/pull/721)
* chore(deps): bump axios and wait-on in /AMW_e2e [#722](https://github.com/liimaorg/liima/pull/722)

# v1.17.32
* Update to Angular 17 and update JS deps [#705](https://github.com/liimaorg/liima/issues/705) [#715](https://github.com/liimaorg/liima/pull/715)
* Angular deployments page: Clipboard button doesn't work [#699](https://github.com/liimaorg/liima/issues/699)
* Exception when deleting resources with functions [#681](https://github.com/liimaorg/liima/issues/681)
* NullPointerException on editTemplateView.xhtml after remove of relation [#704](https://github.com/liimaorg/liima/issues/704)
* Duplicate resource names via renaming [#682](https://github.com/liimaorg/liima/issues/682)

# v1.17.31
* fix several permission related issues [#701](https://github.com/liimaorg/liima/pull/701)
  * runtime resource type is now an normal default resource type. Permissions for default resource types now apply to runtimes also.
  * fix a bug where resource create permission on one resource would allow the creation of other resources: https://github.com/liimaorg/liima/security/advisories/GHSA-q4hh-r2m7-3c22
  * the jsf views apps and resource list now don't have an edit and remove column. As a replacement the release column is now also a link to the resource. Adjusted the with of the side bar.
  * fix several permissions bugs on the edit resource view and related screens:
    * toggling of property encryption
    * permissions on templates
    * permission on add application to app server
  * added missing permission RESOURCE_TEMPLATE to config_admin role in test database
  * added several test to the permissionService
* Update Angular, js deps and wildfly [#700](https://github.com/liimaorg/liima/pull/700)
* AMW_web: set secure and http-only flags for cookie, make sure cookie isn't sent via url param [#694](https://github.com/liimaorg/liima/pull/694)
* Bump guava from 30.1.1-jre to 32.0.0-jre [#693](https://github.com/liimaorg/liima/pull/693)

# v1.17.30
* editResourceView.xhtml: info popup of property doesn't work [#689](https://github.com/liimaorg/liima/issues/689)

# v1.17.29
* Fixed security issues:
  * no restrictive csp [#684](https://github.com/liimaorg/liima/pull/684)
  * update bootstrap js in JSF UI [#685](https://github.com/liimaorg/liima/pull/685)
  * missing hsts header [#686](https://github.com/liimaorg/liima/pull/686)
  * download allows content sniffing [#687](https://github.com/liimaorg/liima/pull/687)
* Update Java Script dependencies [#688](https://github.com/liimaorg/liima/pull/688)

# v1.17.28
* **BREAKING CHANGE** Due to a new unique constraint for issue [#622](https://github.com/liimaorg/liima/issues/622), `TAMW_RESOURCE` needs to be free of duplicate `RELEASE_ID, RESOURCEGROUP_ID` pairs **before** applying the change-set. Duplicates can be found using following sql statement:
  ```
  select count(*), RELEASE_ID, RESOURCEGROUP_ID from TAMW_RESOURCE group by RELEASE_ID, RESOURCEGROUP_ID having count(*) > 1;
  select * from TAMW_RESOURCEGROUP where id = <found id>
  ```
  You can delete the duplicate release via UI. The UI will only show one release and when you delete it the hidden release will surface. Copy the currently visible release to a backup resource and the delete the visible release.
* Fixed security issues:
  * Fix HQL Injection via application name: [#679](https://github.com/liimaorg/liima/pull/679)
  * Fix possible XSS vulnerabilities in Tooltips and Notifications: [#680](https://github.com/liimaorg/liima/pull/680)
  * Fix server side template injection: [#678](https://github.com/liimaorg/liima/pull/678)
  * Remove stacktrace from jsf error.xhtml and embed jquery-ui.min.css: [#671](https://github.com/liimaorg/liima/pull/671)
  * Fix HQL injection via deployment filter: [#663](https://github.com/liimaorg/liima/pull/663)
* Update to Angular 14 and Bootstrap 5: [#661](https://github.com/liimaorg/liima/issues/661)
* Deployment filter Reason has no values: [#594](https://github.com/liimaorg/liima/issues/594)
* resourceDependenciesView.xhtml sort: [#607](https://github.com/liimaorg/liima/issues/607)
* Add audit tables for permissions entities: [#653](https://github.com/liimaorg/liima/issues/653)
* Replace net.sf.json-lib with another JSON library: [#566](https://github.com/liimaorg/liima/issues/566)
* Various JS and Java lib updates

# v1.17.27
* **BREAKING CHANGE** New release of relation uses old prop descriptor for copied instance property [#487](https://github.com/liimaorg/liima/issues/487)
  * This fixes a long-standing bug in Liima and requires manual database cleanup before the updated can be deployed. Instructions can be found [here](./AMW_db_scripts/v1.17.27_property_cleanup.md)
* REST DELETE: /resources/resourceGroups/{resourceGroupId}/releases/{releaseId} deletes non matching releases [#623](https://github.com/liimaorg/liima/issues/623)
* Update Angular and other JavaScript dependencies:
  * update angular [#656](https://github.com/liimaorg/liima/pull/656)
  * Bump async from 2.6.3 to 2.6.4 in /AMW_angular/io [#654](https://github.com/liimaorg/liima/pull/654)
  * Bump minimist from 1.2.5 to 1.2.6 in /AMW_angular/io [#649](https://github.com/liimaorg/liima/pull/649)
  * Bump karma from 6.3.14 to 6.3.16 in /AMW_angular/io [#648](https://github.com/liimaorg/liima/pull/648)
* Update java dependencies:
  * Bump jackson-databind from 2.11.2 to 2.12.6.1 [#655](https://github.com/liimaorg/liima/pull/655)
  * Bump junit from 4.11 to 4.13.1 in /AMW_shakedown/AMW_stp_archetype/src/main/resources/archetype-resources [#645](https://github.com/liimaorg/liima/pull/645)
  * Bump postgresql from 9.4-1200-jdbc41 to 42.2.25 in /AMW_db_scripts [643](https://github.com/liimaorg/liima/pull/643)
  * Update liquibase [658](https://github.com/liimaorg/liima/issues/658)

# v1.17.26
* Update to angular 13 and wildfly 26 [#634](https://github.com/liimaorg/liima/pull/634)
* Properties sometimes missing in test generation/deployment [#550](https://github.com/liimaorg/liima/issues/550)
  * This adds a unique constraint to avoid the problem and violations have to cleanup up manually before the update can be applied. Find violations:
    ```
    select res1.resource_id, res.name, res1.context_id, res1.id from TAMW_RESOURCECONTEXT res1
    inner join
    (select context_id, resource_id from TAMW_RESOURCECONTEXT group by (context_id, resource_id) having count(*) > 1) res2
    on res1.context_id = res2.context_id and res1.resource_id = res2.resource_id
    join TAMW_RESOURCE res on res1.resource_id = res.id;
    ```
  * To clean them up you have to delete the affected resources and recreate them manually via UI.
  * Sometimes I was able to fix it by creating a new release of the resource, deleting the old release and changing the release of the new release. You have to check that all properties were copied to the new release before deleting because they can be missing.
* getResourceRelationListForRelease should always return same release of relation [#626](https://github.com/liimaorg/liima/issues/626)
* Implement REST GET /resources/{resourceId} [#627](https://github.com/liimaorg/liima/issues/627)

# v1.17.25
* Implement update (put) and delete for Resource templates via Rest [#597](https://github.com/liimaorg/liima/issues/597)
  * Breaking change: `/resources/{resourceGroupName}/{releaseName}/templates/{templateName}` no longer returns an array
  * Code cleanup and refactoring
  * Fix REST exception mapping
* Rest copy cleanup, Dockerfile cleanup, remove REQUIRES_NEW transaction for boundaries [#617](https://github.com/liimaorg/liima/pull/617)
  * Don't log freemarker template exceptions in application server log
* Java Script updates

# v1.17.24
* Fix Rest DELETE of resource, update JS deps, update Wildfly Docker Image: [#613](https://github.com/liimaorg/liima/pull/613)
* Fix NullPointer when resource to delete doesn't exit: [#612](https://github.com/liimaorg/liima/pull/612)

# v1.17.23
* The Deployment log view was migrated to Angular. Log lines container errors or warnings are now highlited: [#443](https://github.com/liimaorg/liima/issues/443) and [#450](https://github.com/liimaorg/liima/issues/450)
* Implement Rest DELETE resource: [#602](https://github.com/liimaorg/liima/issues/602)
* Add environmentNameAlias to the deployment excel export: [#527](https://github.com/liimaorg/liima/issues/527)
* Angular deployment view: use ng-select for appserver and release :[#606](https://github.com/liimaorg/liima/pull/606)
* Prevent that templates can be writen outside of generation directory: [#434](https://github.com/liimaorg/liima/issues/434)
* editResourceView: UI doesn't update correctly after removing runtime: [#479](https://github.com/liimaorg/liima/issues/479)
* Switch from TravisCI to GitHub Actions: [#585](https://github.com/liimaorg/liima/pull/585)
* Replace Moment.js with data-fns: [#589](https://github.com/liimaorg/liima/issues/589)
* Convert modals to Ngb: [#587](https://github.com/liimaorg/liima/pull/587)
* JavaScript dependency updates: [#593](https://github.com/liimaorg/liima/pull/593) and [#610](https://github.com/liimaorg/liima/pull/610)

# v1.17.22
* Fix copyFrom in REST: [#584](https://github.com/liimaorg/liima/pull/584)

# v1.17.21
* fix confirm modal not showing: [#583](https://github.com/liimaorg/liima/pull/583)

# v1.17.20
* Deployment datetime picker fixes: [#580](https://github.com/liimaorg/liima/issues/580)
* Rest copyFrom can now be used on all resources: [#522](https://github.com/liimaorg/liima/issues/522)
* Properties set via REST are now visible in the audit view: [#549](https://github.com/liimaorg/liima/issues/549)

# v1.17.19
Same as v1.17.20

# v1.17.18
* Fix deployment dateTime picker and fix layout: [#570](https://github.com/liimaorg/liima/issues/570) and [#510](https://github.com/liimaorg/liima/issues/510)
* Migrat to Bootstrap 4: [#574](https://github.com/liimaorg/liima/pull/574)
* Update to Angular 11: [#577](https://github.com/liimaorg/liima/pull/577)

# v1.17.17
This release fixes a NullPointer and cleans up some java dependencies.

## Bug fixes
* Fix a bug in GET `AMW_rest/resources/deployments/filter` that leads to a NullPointer when a runtime was deleted: [#572](https://github.com/liimaorg/liima/pull/572)
* Remove PowerMockito [#568](https://github.com/liimaorg/liima/issues/568)
* Update to Commons Lang 3 [#565](https://github.com/liimaorg/liima/issues/565)

# v1.17.16
With this release Liima is built using JDK 11 and is required to run it. We now use the Angular cli to simplify updates and switched to Angular 10.

## Bug fixes
* Switch to angular cli [#512](https://github.com/liimaorg/liima/pull/512)
* Update to Angular 10 [#543](https://github.com/liimaorg/liima/pull/543), [#562](https://github.com/liimaorg/liima/pull/562)
* Update to JDK 11 and require it to build [#563](https://github.com/liimaorg/liima/pull/563)
* Update div Java and Angular libraries [#561](https://github.com/liimaorg/liima/pull/561)
* Don't cache Angular index.html [#551](https://github.com/liimaorg/liima/issues/551)
* Wildfly is now configured to use jsf 2.2 [#547](https://github.com/liimaorg/liima/pull/547), which fixes two issues:
  * Warning "A newer release for this resource exist. Are you..." doesn't disappear [#520](https://github.com/liimaorg/liima/issues/520)
  * Warning "Changes you made may not be saved." with default properties [#513](https://github.com/liimaorg/liima/issues/513)
* Update to Docker Image to Wildfly 20 [#563](https://github.com/liimaorg/liima/pull/563)
* Fix amwFileDbIntegrationEmpty [#526](https://github.com/liimaorg/liima/pull/526)
* Document build preconditions [#534](https://github.com/liimaorg/liima/pull/534)
* Environment Strings in the REST API are case sensitive [#418](https://github.com/liimaorg/liima/issues/418)

# v1.17.15
## Bug fixes
* copyFrom TransientPropertyValueException [#480](https://github.com/liimaorg/liima/issues/480)

# v1.17.14
## Bug fixes
* Remove release list dropdown in resource list view [#496](https://github.com/liimaorg/liima/pull/496)
* Update js modules and jackson-databind [#494](https://github.com/liimaorg/liima/pull/494)
* Reload permission caches explicitly [#492](https://github.com/liimaorg/liima/pull/492)

# v1.17.13
## Bug fixes
* IllegalArgumentException after new release of relation [#484](https://github.com/liimaorg/liima/issues/484)
* Add shakedown test works again but is still very slow [#474](https://github.com/liimaorg/liima/issues/474)
* Speed up "only latest" deployment queries [#488](https://github.com/liimaorg/liima/pull/488)
* Speed up deployment queries that are not paged [#485](https://github.com/liimaorg/liima/pull/485)

# v1.17.12
## Bug fixes
* Remove the maia SOAP webservice and web module [#478](https://github.com/liimaorg/liima/pull/478)
* Cache isCallerInRole result in request [#475](https://github.com/liimaorg/liima/pull/475)
* Update jquery to 3.4.1 [#482](https://github.com/liimaorg/liima/issues/482)

# v1.17.11
This release contains mainly performance enhancements and bug fixes.

## Bug fixes
* The editResourceView should be faster: opening the view, switching environments, saving. [#462](https://github.com/liimaorg/liima/pull/462)
* The deployments view should now load faster [#461](https://github.com/liimaorg/liima/pull/461)
* Confirming and changing the date of a deployment gives error [#459](https://github.com/liimaorg/liima/issues/459)
* REST: delete relation properties call very slow [#460](https://github.com/liimaorg/liima/issues/460)
* Update libs to prevent potential security issues [#466](https://github.com/liimaorg/liima/pull/466)

## New features
* Make liquibase data source jndi configurable [#467](https://github.com/liimaorg/liima/pull/467)

# v1.17.10
Revert "Cache roles a user hase to reduce calls to sessionContext.isCallerInRole" [#458](https://github.com/liimaorg/liima/issues/459)

# v1.17.9
Bug fixes and speed improvements in this release.

## Bug fixes
* Corrected padding of environments navigation [#449](https://github.com/liimaorg/liima/pull/449)
* editResourceView: Test Generation button on App checks for permission on App instead of AppServer [#448](https://github.com/liimaorg/liima/issues/448)
* After deleting an env the env filter no long finds any deployments [#452](https://github.com/liimaorg/liima/issues/452)
* Update Maven dependencies [#433](https://github.com/liimaorg/liima/issues/433)
* serverView: cache user permissions in view. Speeds up the view considrably [#453](https://github.com/liimaorg/liima/pull/453)
* REST: put properties call very slow [#446](https://github.com/liimaorg/liima/issues/446)
* Cache roles a user has to reduce calls to sessionContext.isCallerInRole [#455](https://github.com/liimaorg/liima/pull/455)

# v1.17.8
## Bug fixes
* Multiple releases of Runtime displayed wrong on Appserver [#442](https://github.com/liimaorg/liima/issues/442)

## New features
* REST: add 'Test Generation' endpoint [#360](https://github.com/liimaorg/liima/issues/360)
  * Returns the rendered templates of an AppServer and any errors.
  * Endpoint: `AMW_Rest/resources/analyze/testGeneration/{resourceGroupName}/{releaseName}/{env}`
* Alias name for environment [#441](https://github.com/liimaorg/liima/issues/441)
  * The alias name is shown in the editResourceView and deployment view
  * In templates it can be accessed via `${env.nameAlias}`, default value is empty.

# v1.17.7
Revert back to Wildfly 13 because of JSF issues, fix a JSF init bug, update RichFace [#437](https://github.com/liimaorg/liima/pull/437)

# v1.17.6
Fixes some JSF issues on Wildfly 14 [#435](https://github.com/liimaorg/liima/pull/435)

# v1.17.5
Only small fixes in this release.  
__Action Required__: with [#428](https://github.com/liimaorg/liima/pull/428) a user must now have to following permissions to copy form a resource:
* On the target: RESOURCE_RELEASE_COPY_FROM_RESOURCE action ALL
* On the source: RESOURCE, RESOURCE_TEMPLATE, RESOURCE_AMWFUNCTION action READ and RESOURCE_PROPERTY_DECRYPT action ALL

The config_admin role has this permissions already, custom roles might need to be updated.

## New features
* Disable tip im Freemarker error [#423](https://github.com/liimaorg/liima/issues/423)
* Update Docker image to WildFly 14.0.1 [#426](https://github.com/liimaorg/liima/pull/426)

## Bug fixes
* Fix a permission bug in copy from resource [#428](https://github.com/liimaorg/liima/pull/428)
* After a deployment is confirmed it can only be canceled by the requestor [#424](https://github.com/liimaorg/liima/issues/424)
* REST AMW_rest/resources/resources/{resource}/lte/{RL} returns http 500 instead of 404 [#421](https://github.com/liimaorg/liima/issues/421)
* Update Lodash version [#422](https://github.com/liimaorg/liima/issues/422)

# v1.17.4
Fixes a deployment confirmation bug [#420](https://github.com/liimaorg/liima/pull/420)

# v1.17.3
This is mostly a bug fix and enhancement release of the REST interface.

## New features
* Show Runtime of AS in Apps overview [#198](https://github.com/liimaorg/liima/issues/198)
* REST permission endpoints enhancements
  * Add method for deleting roles [#416](https://github.com/liimaorg/liima/pull/416)
  * Default exception mapper added that converts unexpected errors to json [#416](https://github.com/liimaorg/liima/pull/416)
  * Reloading of permissions is now optional [#415](https://github.com/liimaorg/liima/issues/415)
  * ADD_ADMIN_PERMISSIONS_ON_CREATED_RESOURCE permission: only add DEPLOYMENT permissions if resource is an appServer [#414](https://github.com/liimaorg/liima/issues/414)
* REST API: accept unknown properties for forward compatibility [#8](https://github.com/liimaorg/liima/issues/8)
* Update fronte libs [#412](https://github.com/liimaorg/liima/pull/412)

## Bug fixes
* Changes from copy from are not shown in audit view: [#338](https://github.com/liimaorg/liima/issues/338)
* editResourceView: Node properties disappear after save bug [#366](https://github.com/liimaorg/liima/issues/366)

# v1.17.2
This is mostly a bug fix and enhancement release of the REST interface.

## New features
* Added `/{resourceGroupName}/{releaseName}/dependencies/` REST endpoint to get dependencies of a resource [#208](https://github.com/liimaorg/liima/issues/208)
* Added `/configuration` REST endpoint to get the application configuration [#373](https://github.com/liimaorg/liima/pull/373)
* Added readiness and liveness endpoint [#346](https://github.com/liimaorg/liima/pull/346)
* Added `DELETE` REST method to delete resource relations [#361](https://github.com/liimaorg/liima/pull/361)
* Removed the `/resources/deployments/{deploymentId}/detail` endpoint, deployment details are now included deployment entity directly [#392](https://github.com/liimaorg/liima/pull/392)
* Update docker image to Wildfly 13 [#400](https://github.com/liimaorg/liima/pull/400)

## Bug fixes
* Angular: confirm of multiple deployments doesn't work [#386](https://github.com/liimaorg/liima/issues/386)
* Angular: auditview no longer works [#383](https://github.com/liimaorg/liima/issues/383)
* The model now also contains all unrendered relation templates [#231](https://github.com/liimaorg/liima/issues/231)
* Angular Logout tab points to applist.xhtml instead of logout url [#237](https://github.com/liimaorg/liima/issues/237)
* `REST GET /resources/{resourceGroupName}/{releaseName}/relations` only returns consumed relations [#387]
* REST error response body isn't JSON encoded [#343](https://github.com/liimaorg/liima/issues/343)
* Get property REST endpoints don't return default values [#347](https://github.com/liimaorg/liima/issues/347)
* REST restrictions endpoints returns 500 instead of 403 if user has no rights [#348](https://github.com/liimaorg/liima/issues/348)
* Clean up Swagger definition of `/resources/{resourceGroupName}/{releaseName}/relations` [#388](https://github.com/liimaorg/liima/issues/388)
* JSF applist doesn't work with h2 anymore [#397](https://github.com/liimaorg/liima/issues/397)
(https://github.com/liimaorg/liima/issues/387)

# v1.17.1
This is mostly a bug fix release.

## New features
* Angular create deploy: loader for new deployment [#357](https://github.com/liimaorg/liima/issues/357)
* REST create deployment now returns a 424 instead of 400 if no node is active [#352](https://github.com/liimaorg/liima/issues/352)
* REST get releases endpoint add, release JSON got additional fields [#354](https://github.com/liimaorg/liima/issues/354)
* Update swagger-ui [#370](https://github.com/liimaorg/liima/issues/370)

## Bug fixes
* applist.xhtml is now sorted case insensitive [#334](https://github.com/liimaorg/liima/issues/334)
* editResourceView: selecting release of relation causes display error [#335](https://github.com/liimaorg/liima/issues/335)
* Deployment CSV export: columns shifted to the left if "Deployment parameters" empty bug [#344](https://github.com/liimaorg/liima/issues/344)
* Feedback multi permission GUI: shows loader and success message now, Global permissions are marked as such again, `Applications without application server` is hidden [#333](https://github.com/liimaorg/liima/issues/333)
* Angular permission GUI: users and roles with spaces in name can be added [#359](https://github.com/liimaorg/liima/issues/359)
* Dash in YAML editor difficult to see [#358](https://github.com/liimaorg/liima/issues/358)
* editTemplateView.xhtml: "Compare with history" not updated after save [#363](https://github.com/liimaorg/liima/issues/363)
* editTemplateView.xhtml: compare doesn't work on Wildfly 12.0 [#362](https://github.com/liimaorg/liima/issues/362)
* Login prompt appears twice [#349](https://github.com/liimaorg/liima/issues/349)
* Templates: accessing not rendered relation templates now mostly works:
 [#378](https://github.com/liimaorg/liima/issues/378) and [#231](https://github.com/liimaorg/liima/issues/231)
 * Disable logging the run script output to the server log. Can be re-enabled via `LOG_RUNSCRIPT_OUTPUT_TO_SERVER_LOG` ConfigKey [#380](https://github.com/liimaorg/liima/issues/380)
* REST: copyFromResource no longer needs the content-type header to be set [#351](https://github.com/liimaorg/liima/issues/351)
* REST: get /deployments NullPointer with deleted AppServer/App [#328](https://github.com/liimaorg/liima/issues/328)
* properties REST endpoint returns successfully for non-existing resources [#325](https://github.com/liimaorg/liima/issues/325)

# v1.17.0
This release contains two mayor features: the resource audit view and the multi permission GUI.

## New features
* Audit view: [#332](https://github.com/liimaorg/liima/issues/332)
![image](https://user-images.githubusercontent.com/15231595/38356216-6c319af4-38bf-11e8-9b3c-bcc38c115b54.png)
  * Reachable via editResourceView -> Go to -> Audit view
  * Shows all changes made to a resource like Property Descriptors, Properties, Templates etc., the date and the user who made the change.
  * Current limitations of the implementation:
    * Only changes are shown that are made after v1.17 is deployed. Older changes can only be viewed in the Liima Database.
    * Changes to ResourceTypes and Functions are not shown.
    * Passwords can't be viewed and are always masked.
    * Relation of relation template isn't shown.
* Multi permission GUI: [#259](https://github.com/liimaorg/liima/issues/259)
![image](https://user-images.githubusercontent.com/15231595/38356482-26455944-38c0-11e8-8f0d-1ba7a7186be6.png)
  * The new GUI allows to select multiple environments, permissions, resources and so on. This dramatically reduced the amount of clicks needed to create multiple permissions.
* Applist search is now case insensitive: [#307](https://github.com/liimaorg/liima/issues/307)
* Auto-Refresh on Deploy page: [#293](https://github.com/liimaorg/liima/issues/293)
* Minor enhancements to the config overview from v1.16.1: [#295](https://github.com/liimaorg/liima/issues/295)

## Bug fixes
* Add test for force property descriptor deletion: [#310](https://github.com/liimaorg/liima/issues/310)
* Update JQuery: [#327](https://github.com/liimaorg/liima/issues/327)
* Update Moment.js: [#337](https://github.com/liimaorg/liima/issues/337)

# v1.16.1
This release contains bug fixes and one new feature.

## New features
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
