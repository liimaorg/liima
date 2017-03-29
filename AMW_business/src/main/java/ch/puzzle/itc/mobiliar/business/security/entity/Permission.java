/*
 * AMW - Automated Middleware allows you to manage the configurations of
 * your Java EE applications on an unlimited number of different environments
 * with various versions, including the automated deployment of those apps.
 * Copyright (C) 2013-2016 by Puzzle ITC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ch.puzzle.itc.mobiliar.business.security.entity;

public enum Permission {

    DEFAULT(""),

    //RESOURCE PERMISSIONS
    ADD_NEW_RES_OF_DEFAULT_RESTYPE("New application or/and application server. Path: Resources -> select APPLICATION tab"),
    DELETE_RES_INSTANCE_OF_DEFAULT_RESTYPE("WARNING: DON'T CHANGE THIS PERMISSION"),
    EDIT_NOT_DEFAULT_RES_OF_RESTYPE("With this permission the config admin can rename the NOT DEFAULT RESOURCE AND RESOURCE TYPE. The DEAFULT RESOURCE TYPE are: APPLICATION, APPLICATION SERVER and NODE"),
    RENAME_INSTANCE_DEFAULT_RESOURCE("With this permission it is possible to rename all instances of default resource types. This permission has only effect if it is not combined with EDIT_RES_OR_RESTYPE_NAME since it is more restrictive. WARNING: DON'T CHANGE THIS PERMISSION"),

    INSTANCE_TEMP_LIST("Instance template list. You can find this list in all Resource and Resource Type screen."),
    DELETE_RES_TEMPLATE("Delete instance template. You can find this link in all Templates/Relationship Templates panel in all Resource/Resource screen."),
    EDIT_RES_TEMP("Permission to edit the template of a resource"),
    SAVE_RES_TEMPLATE("WARNING: DON'T CHANGE THIS PERMISSION"),
    TAG_CURRENT_STATE("Tag current state. You can find this select one menu in application instance screen and in all not default resource screen. "),

    EXCLUDE_APP_FROM_NODE("Exclude application from node. You find can this checkbox in all instances of applications server"),

    CHANGE_RESOURCE_RELEASE("With this permission the viewer can view releases"),

    TEST_GENERATION("With this permission the user can test the generation of an Applicationserver"),

    //RESOURCE RELATION PERMISSIONS
    CONSUMED_RES_LIST("Allows to see consumed relations in the edit screen"),
    PROVIDED_RES_LIST("Allows to see provided relations in the edit screen"),
    ADD_RELATED_RESOURCE("Add a relation to an application. Path_1: Apps -> Edit(application) -> Add Relation; Path_2 Resources -> APPLICATION -> Add Relation"),
    ADD_AS_PROVIDED_RESOURCE("Add as provided Resource. Path: all resource instance screen -> press button 'Add relation'."),
    ADD_AS_CONSUMED_RESOURCE("Add as consumed Resource. Path: all resource instance screen-> press button 'Add relation'."),
    ADD_EVERY_RELATED_RESOURCE("WARNING: DON'T CHANGE THIS PERMISSION"),
    DELETE_EVERY_RELATED_RESOURCE("WARNING: DON'T CHANGE THIS PERMISSION"),
    DELETE_CONS_OR_PROVIDED_RELATION("Delete Consumed or Provided relation. You can find this button in all resource instance except in Applicationserver instance and node instance"),
    SELECT_RUNTIME("Select runtime. You can find this button in all instances of Application Server"),
    ADD_NODE_RELATION("Add Node to Application Server. Path: Resources -> select one instance of Applicaiton Server "),
    DELETE_NODE_RELATION("Delete Node Relation button. Path: Resources -> select APPLICATIONSERVER tab -> select one instance of application server list "),
    ADD_APP_TO_APP_SERVER("Add an application to an application server. Path_1: Apps -> Edit(application server) -> Add application ; Path_2 Resources -> APPLICATIONSERVER -> Edit -> Add application"),
    DELETE_APP_TO_APP_SERVER("Delete application to application server. You can find this list in all instances of application server"),
    APP_LIST_ADDED_APPSERVER("The Applications added to applicationserver. Path: Resource -> APPLICATIONSERVER -> Edit"),

    //MODIFY_APPSERVER_RELATION("With this permission the config admin can make modifications in the appServerRelations screen."),

    //RESOURCE TYPE PERMISSIONS
    NEW_RESTYPE("New resource type button. Path: Resources"),
    DELETE_RESTYPE("Delete Resource Type. In all 'NOT DEFAULT RESOURCE TYPE'. Path -> Resources -> select a 'NOT DEFAULT RESOURCE TYPE'"),
    EDIT_RES_TYPE("Edit resource type. You can find this panel in Resources screen. All Resource Type have this button. Path -> Resources"),

    ADD_RELATED_RESOURCETYPE("Add related resource type. Path: all Resource Type instance screen -> press button 'Add related resource Type.'"),

    REL_RESTYPE_PANEL_LIST("Allows to show the related resource types"),

    SAVE_RESTYPE_TEMPLATE("Permission to persist a template for a resource type. WARNING: DON'T CHANGE THIS PERMISSION"),
    EDIT_RESTYPE_TEMPLATE("Edit resource type tempalte. This is the List of Resource Type Templates in Templates panel. You can find this panel in all Resource/ResourceType screen"),
    DELETE_RESTYPE_TEMPLATE("Delete resource type template. You can find this link in all Templates/Relationship Templates panel in all Resource/ResourceType screen"),

    //RESOURCE AND RESOURCE TYPE PERMISSIONS
    EDIT_PROPERTIES_COMMENT_POPUP("Edit comments of property."),
    EDIT_RES_OR_RESTYPE_NAME("Edit all resource and resource type names irrespective of their type. You can find this fiel in all resource/resource type instance screen."),
    RESET_PROP("Reset properties. You can find this list in all Resource/Resource Type screen."),
    EDIT_PROP_LIST_OF_INST_APP("Edit properties in resource / resource type screen."),
    PROP_LIST("This permission allows to list the properties in the edit screen."),
    CREATE_TEMPLATE("With this permission it can create a new template"),
    RES_RESTYPE_TEMPLATE_LIST("Allows to list the templates of resources and resource types"),
    DECRYPT_PROPERTIES("When the properties is encrypt without this permission you can not see the not encrypt value"),
    EDIT_ALL_PROPERTIES("WARNING: DON'T CHANGE THIS PERMISSION"),
    SAVE_PROPERTY("With this permission it can modify the property"),
    SAVE_ALL_PROPERTIES("Permission to persist all properties of a resource or resource type excluding its name. WARNING: DON'T CHANGE THIS PERMISSION"),
    SAVE_ALL_CHANGES("Permission to persist all properties and the name of any resource or resource type. WARNING: DON'T CHANGE THIS PERMISSION"),

    SET_SOFTLINK_ID_OR_REF("Allows to manually set a softlink id or a softlink reference respective"),

    REMOVE_RELATED_RESOURCETYPE("Remove related resource type"),

    // FOREIGNABLES PERMISSIONS
    IGNORE_FOREIGNABLE_OWNER("Ignore the owner of a foreignable object. Chuck Norris can edit/delete objects of any owner!"),

    //DEPLOYMENT
    EXPORT_CSV_DEPLOYMENTS("You can find this button in Deploy page."),

    //SHAKEDOWN TEST
    SHAKEDOWN_TEST_PAGE("Shakedown test page."),
    ADD_SHAKEDOWN_TEST("Add Shakedown test. Path: Shakedown Test."),
    SHAKEDOWN_TEST_MODE(""),
    EXECUTE_SHAKE_TEST_CHECKBOX(""),
    TEST_NEIGHBOURHOOD_CHECKBOX(""),
    EXECUTE_SHAKE_TEST_ORDER(""),

    // FUNCTIONS
    MANAGE_AMW_FUNCTIONS("Create, edit and delete all AmwFunctions on Resource Types and Resource Instances"),
    MANAGE_AMW_APP_INSTANCE_FUNCTIONS("Create, edit and delete all AmwFunctions on App Instances"),
    VIEW_AMW_FUNCTIONS("Can see AmwFunctions"),
    MANAGE_GLOBAL_FUNCTIONS("Create, edit and delete GlobalFunctions"),
    VIEW_GLOBAL_FUNCTIONS("Can see GlobalFunctions"),

    // Global Tags
    MANAGE_GLOBAL_TAGS("Create, edit and delete GlobalTags"),

    // Deployment parameter
    MANAGE_DEPLOYMENT_PARAMETER("Create, edit and delete deployment parameter"),

    //**** SETTINGS *****//


    //CONTEXT PERMISSIONS
    ADD_NEW_ENV_OR_DOM("Add a new environment or domain. Path: Settings -> Add new Domain/Add new Environment"),
    REMOVE_ENV_OR_DOM("Remove environment or domain. Path: -> Settings -> select Environment(tab) -> select Environment or Domain"),
    EDIT_ENV_OR_DOM_NAME("Path: Setting -> select 'Enviroments'(tab)"),
    SAVE_SETTINGS_ENV("Save environments button. Path: Settings -> select Property Types tab"),

    //PROPERTY TYPES
    ADD_PROPTYPE("Add Property Type. Path: Settins -> Property Types(tab)"),
    DELETE_PROPTYPE("Delete Property Type. Path: Settins -> Property Types(tab)"),
    PROP_TYPE_NAME_VALUE("Property Type panel. Path: Settings -> Property Types(tab)"),
    EDIT_PROP_TYPE_NAME("Path: Settings -> select 'Property Types'(tab)"),
    EDIT_PROP_TYPE_VALIDATION("Path: Settings -> select 'Property Types'(tab)"),
    SAVE_SETTINGS_PROPTYPE("Save property type button. Path: Settings -> select Property Types tab"),

    //NAVIGATION
    APP_TAB("Applications tab. Path: Apps"),
    RES_TYPE_LIST_TAB("Resource Type Panel. Path: Resources(screen)"),
    ENV_PANEL_LIST("Enviroment list. You can find this panel in all Resource/ResourceType screen and in Environments screen in Setting page."),
    SETTING_PANEL_LIST("Setting panel. Path: Settings(screen)"),
    RESOURCE_LIST("Resource list. You can find this list in Resources screen. All the Resources Type has a resource list."),
    APP_AND_APPSERVER_LIST("The Applications and Applicatioservers list. Path-> Apps"),
    ROLES_AND_PERMISSIONS_TAB("Roles and Permissions tab. Path: Settings"),
    BACK_TO_RES_LIST("Back to resource list. You can find this button in all resource and resource type-instance screen"),

    //ROLES
    DELETE_ROLE("Delete Role. Path: Settings -> select Roles and Permissions tab"),
    CREATE_ROLE("Create Role. Path: Settings -> select Roles and Permissions tab"),
    ASSIGN_REMOVE_PERMISSION("Assign/Remove/Add Permission. WARNING: if you remove this permission to config_admin role you can't modify role and permission."),


    //NEW PERMISSIONS
    DEPLOYMENT("The right to deploy."),
    COPY_FROM_RESOURCE("The right to copy the configuration from one resource into an other."),
    // TODO ist: ALLE soll: ALLE WELCHE NICHT DEFAULT TYPEN
    // COPY_FROM_RESOURCE("With this permission the config admin can copy the configuration from one non default resource into an other."),
    // COPY_FROM_RESOURCE_APP("With this permission the config admin can copy the configuration from one app into an other."),
    // COPY_FROM_RESOURCE_APPSERVER("With this permission the config admin can copy the configuration from one appserver into an other."),
    // COPY_FROM_RESOURCE_NODE("With this permission the config admin can copy the configuration from one node into an other."),
    RENAME_RESOURCE("The right to rename resources"),
    // RENAME_APPSERVER("With this permission it is possible to rename all AppServer instances."),
    // RENAME_APP("With this permission it is possible to rename all APP instances."),
    // RENAME_NODE("With this permission it is possible to rename all NODE instances."),
    // TODO ist: ALLE soll: ALLE WELCHE NICHT DEFAULT TYPEN
    // RENAME_RES("With this permission it is possible to rename everthing else."),
    RELEASE("The right to create, read, update or delete releases"),
    // CREATE_RELEASE("With this permission the config admin can create releases."),
    // EDIT_RELEASE("With this permission the config admin can edit releases."),
    // DELETE_RELEASE("With this permission the config admin can delete releases."),
    // VIEW_RELEASE("With this permission the viewer can view releases"),
    SHAKEDOWNTEST("The right to create, read, update or delete shakedown tests"),
    // STP_MANAGEMENT_PAGE("Path: Settings -> select STP Management"),
    // EDIT_STP("Path: Settings -> select STP Management -> Edit"),
    // DELETE_STP("Path: Settings -> select STP Management -> Edit"),
    // ADD_STP("Path: Settings -> select STP Management -> Add new STP"),
    RESOURCE("The right to create, read, update or delete resources");
    // NEW_RES("New resource button. You can find this button in APPLICATIONSERVER/NODE/NOT DEFAULT RESOURCETYPE."),
    // ADD_APPSERVER("Add application server. Path: Apps"),
    // ADD_APP("Add new application and/or application server. Path: Apps"),
    // ADD_NODE("Add a new node to an application server. Path_1: Apps -> Edit(application server) -> Add Node; Path_2: Resources -> APPLICATIONSERVER -> Edit -> Add Node"),
    // DELETE_RES("Delete Resource. In all resource type screen. Path -> Resources -> select one Resource Type."),
    // DELETE_APPSERVER("Delete application server. Path: Apps"),
    // DELETE_APP("Delete application. Path: Apps"),
    // EDIT_RES("With this permission you can see the edit link"), > READ
    // EDIT_APP("Edit application. Path: Apps"), > READ
    // EDIT_APP_SERVER("Edit application server. Path: Apps"), > READ

    // CANDIDATES (assigned only to config_admin)
    // SAVE_ALL_CHANGES("Permission to persist all properties and the name of any resource or resource type. WARNING: DON'T CHANGE THIS PERMISSION"),
    // EDIT_RES_OR_RESTYPE_NAME("Edit all resource and resource type names irrespective of their type. You can find this fiel in all resource/resource type instance screen."),

    private String info;

    Permission(String info) {
        this.info = info;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

}
