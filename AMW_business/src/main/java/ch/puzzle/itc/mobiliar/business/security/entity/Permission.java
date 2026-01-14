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
    ANGULAR_EDIT_RESOURCE("Display links to the Angular edit resource screen", true), // TODO: remove after migration

    // ROLES
    DELETE_ROLE("Delete Role. Path: Settings -> select Roles and Permissions tab", true),
    CREATE_ROLE("Create Role. Path: Settings -> select Roles and Permissions tab", true),
    ASSIGN_REMOVE_PERMISSION("Assign/Remove/Add Permission. WARNING: if you remove this permission to config_admin role you can't modify role and permission.", true),

    // NEW PERMISSIONS
    DEPLOYMENT("The right to deploy.", false),
    RESOURCE_RELEASE_COPY_FROM_RESOURCE ("The right to copy the configuration from one Resources into an other.", false),
    RELEASE("The right to create, read, update or delete Releases", false),
    RESOURCE("The right to create, read, update or delete Resources", false),
    RESOURCETYPE("The right to create, read, update or delete ResourceTypes", false),
    RESOURCE_TEMPLATE("The right to create, read, update or delete Templates of Resources", false),
    RESOURCETYPE_TEMPLATE("The right to create, read, update or delete Templates of ResourceTypes", false),
    RESOURCE_AMWFUNCTION("The right to create, read, update or delete AmwFunctions of Resources", false),
    RESOURCETYPE_AMWFUNCTION("The right to create, read, update or delete AmwFunctions of ResourceTypes", false),
    RESOURCE_TEST_GENERATION("The right to start test generation", false),
    RESOURCE_TEST_GENERATION_RESULT("The right to see Templates generated from test generation", false),
    RESOURCE_PROPERTY_DECRYPT("The right to decrypt and edit crypted Resource Properties", false),
    RESOURCETYPE_PROPERTY_DECRYPT("The right to decrypt and edit crypted ResourceType Properties", false),
    PERMISSION_DELEGATION("The right to delegate own Permissions to other users", true),
    ADD_ADMIN_PERMISSIONS_ON_CREATED_RESOURCE("The right automatically receive admin Permissions for self created Resources", true);

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
