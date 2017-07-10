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

    DEFAULT("",true),

    // RESOURCE PERMISSIONS
    RENAME_INSTANCE_DEFAULT_RESOURCE("With this permission it is possible to rename all instances of default resource types. This permission has only effect if it is not combined with EDIT_RES_OR_RESTYPE_NAME since it is more restrictive. WARNING: DON'T CHANGE THIS PERMISSION", true),
    TAG_CURRENT_STATE("Tag current state. You can find this select one menu in application instance screen and in all not default resource screen. ", true),
    EXCLUDE_APP_FROM_NODE("Exclude application from node. You find can this checkbox in all instances of applications server", true),
    CHANGE_RESOURCE_RELEASE("With this permission the viewer can view releases", true),
    TEST_GENERATION("With this permission the user can test the generation of an Applicationserver", true),

    // RESOURCE RELATION PERMISSIONS
    CONSUMED_RES_LIST("Allows to see consumed relations in the edit screen", true),
    PROVIDED_RES_LIST("Allows to see provided relations in the edit screen", true),
    ADD_RELATED_RESOURCE("Add a relation to an application. Path_1: Apps -> Edit(application) -> Add Relation; Path_2 Resources -> APPLICATION -> Add Relation", true),
    ADD_AS_PROVIDED_RESOURCE("Add as provided Resource. Path: all resource instance screen -> press button 'Add relation'.", true),
    ADD_AS_CONSUMED_RESOURCE("Add as consumed Resource. Path: all resource instance screen-> press button 'Add relation'.", true),
    ADD_EVERY_RELATED_RESOURCE("WARNING: DON'T CHANGE THIS PERMISSION", true),
    DELETE_EVERY_RELATED_RESOURCE("WARNING: DON'T CHANGE THIS PERMISSION", true),
    DELETE_CONS_OR_PROVIDED_RELATION("Delete Consumed or Provided relation. You can find this button in all resource instance except in Applicationserver instance and node instance", true),
    SELECT_RUNTIME("Select runtime. You can find this button in all instances of Application Server", true),
    ADD_NODE_RELATION("Add Node to Application Server. Path: Resources -> select one instance of Applicaiton Server ", true),
    DELETE_NODE_RELATION("Delete Node Relation button. Path: Resources -> select APPLICATIONSERVER tab -> select one instance of application server list ", true),
    DELETE_APP_TO_APP_SERVER("Delete application to application server. You can find this list in all instances of application server", true),
    APP_LIST_ADDED_APPSERVER("The Applications added to applicationserver. Path: Resource -> APPLICATIONSERVER -> Edit", true),

    // RESOURCE TYPE PERMISSIONS
    ADD_RELATED_RESOURCETYPE("Add related resource type. Path: all Resource Type instance screen -> press button 'Add related resource Type.'", true),
    REL_RESTYPE_PANEL_LIST("Allows to show the related resource types", true),

    // RESOURCE AND RESOURCE TYPE PERMISSIONS
    EDIT_PROPERTIES_COMMENT_POPUP("Edit comments of property.", true),
    EDIT_RES_OR_RESTYPE_NAME("Edit all resource and resource type names irrespective of their type. You can find this fiel in all resource/resource type instance screen.", true),
    RESET_PROP("Reset properties. You can find this list in all Resource/Resource Type screen.", true),
    EDIT_PROP_LIST_OF_INST_APP("Edit properties in resource / resource type screen.", true),
    PROP_LIST("This permission allows to list the properties in the edit screen.", true),
    DECRYPT_PROPERTIES("When the properties is encrypt without this permission you can not see the not encrypt value", true),
    SAVE_PROPERTY("With this permission it can modify a property descriptor", true),
    SET_SOFTLINK_ID_OR_REF("Allows to manually set a softlink id or a softlink reference respective", true),
    REMOVE_RELATED_RESOURCETYPE("Remove related resource type", true),

    // FOREIGNABLES PERMISSIONS
    IGNORE_FOREIGNABLE_OWNER("Ignore the owner of a foreignable object. Chuck Norris can edit/delete objects of any owner!", true),

    // DEPLOYMENT
    EXPORT_CSV_DEPLOYMENTS("You can find this button in Deploy page.", true),

    // SHAKEDOWN TEST
    SHAKEDOWN_TEST_PAGE("Shakedown test page.", true),
    ADD_SHAKEDOWN_TEST("Add Shakedown test. Path: Shakedown Test.", true),
    SHAKEDOWN_TEST_MODE("", true),
    EXECUTE_SHAKE_TEST_CHECKBOX("", true),
    TEST_NEIGHBOURHOOD_CHECKBOX("", true),
    EXECUTE_SHAKE_TEST_ORDER("", true),

    // FUNCTIONS
    MANAGE_GLOBAL_FUNCTIONS("Create, edit and delete GlobalFunctions", true),
    VIEW_GLOBAL_FUNCTIONS("Can see GlobalFunctions", true),

    // Global Tags
    MANAGE_GLOBAL_TAGS("Create, edit and delete GlobalTags", true),

    // Deployment parameter
    MANAGE_DEPLOYMENT_PARAMETER("Create, edit and delete deployment parameter", true),

    //**** SETTINGS *****//

    // CONTEXT PERMISSIONS
    ADD_NEW_ENV_OR_DOM("Add a new environment or domain. Path: Settings -> Add new Domain/Add new Environment", true),
    REMOVE_ENV_OR_DOM("Remove environment or domain. Path: -> Settings -> select Environment(tab) -> select Environment or Domain", true),
    EDIT_ENV_OR_DOM_NAME("Path: Setting -> select 'Enviroments'(tab)", true),
    SAVE_SETTINGS_ENV("Save environments button. Path: Settings -> select Property Types tab", true),

    // PROPERTY TYPES
    ADD_PROPTYPE("Add Property Type. Path: Settins -> Property Types(tab)", true),
    DELETE_PROPTYPE("Delete Property Type. Path: Settins -> Property Types(tab)", true),
    PROP_TYPE_NAME_VALUE("Property Type panel. Path: Settings -> Property Types(tab)", true),
    EDIT_PROP_TYPE_NAME("Path: Settings -> select 'Property Types'(tab)", true),
    EDIT_PROP_TYPE_VALIDATION("Path: Settings -> select 'Property Types'(tab)", true),
    SAVE_SETTINGS_PROPTYPE("Save property type button. Path: Settings -> select Property Types tab", true),

    // NAVIGATION
    APP_TAB("Applications tab. Path: Apps", true),
    RES_TYPE_LIST_TAB("Resource Type Panel. Path: Resources(screen)", true),
    ENV_PANEL_LIST("Enviroment list. You can find this panel in all Resource/ResourceType screen and in Environments screen in Setting page.", true),
    SETTING_PANEL_LIST("Setting panel. Path: Settings(screen)", true),
    RESOURCE_LIST("Resource list. You can find this list in Resources screen. All the Resources Type has a resource list.", true),
    APP_AND_APPSERVER_LIST("The Applications and Applicatioservers list. Path-> Apps", true),
    ROLES_AND_PERMISSIONS_TAB("Roles and Permissions tab. Path: Settings", true),
    BACK_TO_RES_LIST("Back to resource list. You can find this button in all resource and resource type-instance screen", true),

    // ROLES
    DELETE_ROLE("Delete Role. Path: Settings -> select Roles and Permissions tab", true),
    CREATE_ROLE("Create Role. Path: Settings -> select Roles and Permissions tab", true),
    ASSIGN_REMOVE_PERMISSION("Assign/Remove/Add Permission. WARNING: if you remove this permission to config_admin role you can't modify role and permission.", true),

    // NEW PERMISSIONS
    DEPLOYMENT("The right to deploy.", false),
    COPY_FROM_RESOURCE("The right to copy the configuration from one resource into an other.", false),
    RELEASE("The right to create, read, update or delete releases", false),
    SHAKEDOWNTEST("The right to create, read, update or delete shakedown tests", false),
    RESOURCE("The right to create, read, update or delete resources", false),
    RESOURCETYPE("The right to create, read, update or delete resourcetypes", false),
    TEMPLATE_RESOURCE("The right to create, read, update or delete templates of resources", false),
    TEMPLATE_RESOURCETYPE("The right to create, read, update or delete templates of resourcestypes", false),
    AMWFUNCTION("The right to create, read, update or delete AmwFunctions", false);

    private String info;
    private boolean old;

    Permission(String info, boolean old) {
        this.info = info;
        this.old = old;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public boolean isOld() {
        return old;
    }

}
