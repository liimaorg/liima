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

package ch.puzzle.itc.mobiliar.business.security.control;

import java.security.Principal;
import java.util.*;

import javax.ejb.SessionContext;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import ch.puzzle.itc.mobiliar.builders.ContextEntityBuilder;
import ch.puzzle.itc.mobiliar.builders.ResourceEntityBuilder;
import ch.puzzle.itc.mobiliar.builders.RestrictionDTOBuilder;
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.integration.entity.util.ResourceTypeEntityBuilder;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.*;
import ch.puzzle.itc.mobiliar.business.security.entity.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import ch.puzzle.itc.mobiliar.common.util.DefaultResourceTypeDefinition;

import static java.util.Collections.EMPTY_LIST;
import static org.mockito.Mockito.*;


@SuppressWarnings("static-access")
public class PermissionServiceTest {

	private static final String APP_DEVELOPER = "app_developer";
	private static final String CONFIG_ADMIN = "config_admin";
	private static final String VIEWER = "viewer";
	private static final String SERVER_ADMIN = "server_admin";
	private static final String SHAKEDOWN_ADMIN = "shakedown_admin";
	private static final String TEST_DEPLOYER = "test_deployer";
	private static final String ROLE_NOT_DEPLOY = "role_not_deploy";
	
	private PermissionService permissionService;
	private SessionContext sessionContext;
	private PermissionRepository permissionRepository;
	private Map<String, List<RestrictionDTO>> myRoles;
    private ResourceEntityBuilder resourceEntityBuilder = new ResourceEntityBuilder();

	private ContextEntity global;
	private ContextEntity test;
	private ContextEntity envC;
	private ContextEntity envZ;
	private ResourceGroupEntity asResourceGroup;
	private ResourceGroupEntity appResourceGroup;

	private Principal principal;

    @Before
	public void setUp(){
		permissionService = new PermissionService();
		sessionContext = Mockito.mock(SessionContext.class);
		permissionService.sessionContext = sessionContext;
		permissionRepository = Mockito.mock(PermissionRepository.class);
		permissionService.permissionRepository = permissionRepository;
		// reset the static caches to avoid side effects
		permissionService.deployableRolesWithRestrictions = null;
		permissionService.rolesWithRestrictions = null;
		permissionService.userRestrictions = null;

		global = new ContextEntityBuilder().id(1).buildContextEntity("GLOBAL", null, new HashSet<ContextEntity>(), false);
		test = new ContextEntityBuilder().id(5).buildContextEntity("TEST", global, new HashSet<ContextEntity>(), false);
		envC = new ContextEntityBuilder().id(10).buildContextEntity("C", test, new HashSet<ContextEntity>(), false);
		envZ = new ContextEntityBuilder().id(11).buildContextEntity("Z", test, new HashSet<ContextEntity>(), false);

		principal = new Principal() {
			@Override
			public String getName() {
				return "tester";
			}

		};

		asResourceGroup = new ResourceGroupEntity();
		asResourceGroup.setResourceType(ResourceTypeEntityBuilder.APPLICATION_SERVER_TYPE);

		appResourceGroup = new ResourceGroupEntity();
		appResourceGroup.setId(321);
		appResourceGroup.setResourceType(ResourceTypeEntityBuilder.APPLICATION_TYPE);
	}

	@Test
	public void shouldNotAllowToRemoveDefaultInstanceOfResTypeIfHasPermissionForResourcesOnly(){
		// given
		ResourceTypeEntity applicationResTypeEntity = new ResourceTypeEntity();
		applicationResTypeEntity.setName(DefaultResourceTypeDefinition.APPLICATION.name());

		when(sessionContext.isCallerInRole(CONFIG_ADMIN)).thenReturn(true);
		when(sessionContext.getCallerPrincipal()).thenReturn(principal);
		myRoles = new HashMap<>();
		PermissionEntity permission = new PermissionEntity();
		permission.setValue(Permission.RESOURCE.name());
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.DELETE);
		res.setResourceTypePermission(ResourceTypePermission.NON_DEFAULT_ONLY);
		res.setPermission(permission);
		myRoles.put(CONFIG_ADMIN, Arrays.asList(new RestrictionDTO(res)));
		permissionService.rolesWithRestrictions = myRoles;
		// when
		boolean result = permissionService.hasPermissionToRemoveInstanceOfResType(applicationResTypeEntity);
		// then
		Assert.assertFalse(result);
	}
	
	@Test
	public void shouldNotAllowToRemoveInstanceOfNonDefaultResTypeIfHasPermissionToDeleteInstancesOfDefaultResourceTypeOnly(){
		// given
		ResourceTypeEntity nonDefaultResType = new ResourceTypeEntity();

		when(sessionContext.isCallerInRole(SERVER_ADMIN)).thenReturn(true);
		when(sessionContext.getCallerPrincipal()).thenReturn(principal);
		myRoles = new HashMap<>();
		PermissionEntity permission = new PermissionEntity();
		permission.setValue(Permission.RESOURCE.name());
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.DELETE);
		res.setResourceTypePermission(ResourceTypePermission.DEFAULT_ONLY);
		res.setPermission(permission);
		myRoles.put(SERVER_ADMIN, Arrays.asList(new RestrictionDTO(res)));
		permissionService.rolesWithRestrictions = myRoles;
		// when
		boolean result = permissionService.hasPermissionToRemoveInstanceOfResType(nonDefaultResType);
		// then
		Assert.assertFalse(result);
	}
	
	@Test
	public void shouldAllowToRemoveDefaultInstanceOfResTypeIfHasPermissionToDeleteInstancesOfDefaultResourceType(){
		// given
		ResourceTypeEntity applicationResTypeEntity = new ResourceTypeEntity();
		applicationResTypeEntity.setName(DefaultResourceTypeDefinition.APPLICATION.name());

		when(sessionContext.isCallerInRole(SERVER_ADMIN)).thenReturn(true);
		when(sessionContext.getCallerPrincipal()).thenReturn(principal);
		myRoles = new HashMap<>();
		PermissionEntity permission = new PermissionEntity();
		permission.setValue(Permission.RESOURCE.name());
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.ALL);
		res.setResourceTypePermission(ResourceTypePermission.DEFAULT_ONLY);
		res.setPermission(permission);
		myRoles.put(SERVER_ADMIN, Arrays.asList(new RestrictionDTO(res)));
		permissionService.rolesWithRestrictions = myRoles;
		// when
		boolean result = permissionService.hasPermissionToRemoveInstanceOfResType(applicationResTypeEntity);
		// then
		Assert.assertTrue(result);
	}

    @Test
	public void hasPermissionToDeleteResourceRelationWhenUserHasResourceUpdatePermissionAndResourceIsInstanceOfDefaultResourceType(){
		// given
        ResourceEntity app = resourceEntityBuilder.mockApplicationEntity("app", appResourceGroup, null);
		
		when(sessionContext.isCallerInRole(CONFIG_ADMIN)).thenReturn(true);
		when(sessionContext.getCallerPrincipal()).thenReturn(principal);
		myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.ALL);
		myRoles.put(CONFIG_ADMIN, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.RESOURCE, res)));
		permissionService.rolesWithRestrictions = myRoles;

		// when
		boolean result = permissionService.hasPermissionToDeleteRelation(app, new ContextEntity());

		// then
		Assert.assertTrue(result);
	}
	
	@Test
	public void hasPermissionToDeleteResourceRelationWhenUserHasResourceUpdatePermissionAndResourceIsInstanceOfNotDefaultResourceType(){
		// given
        ResourceEntity ws = resourceEntityBuilder.mockResourceEntity("ws", null, "webservice", null);

		when(sessionContext.isCallerInRole(CONFIG_ADMIN)).thenReturn(true);
		when(sessionContext.getCallerPrincipal()).thenReturn(principal);
		myRoles = new HashMap<>();
		RestrictionEntity upd = new RestrictionEntity();
		upd.setAction(Action.UPDATE);
		myRoles.put(CONFIG_ADMIN, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.RESOURCE, upd)));
		permissionService.rolesWithRestrictions = myRoles;

		// when
		boolean result = permissionService.hasPermissionToDeleteRelation(ws, new ContextEntity());

		// then
		Assert.assertTrue(result);
	}

    @Test
    public void hasPermissionToDeleteResourceRelationWhenUserHasResourceUpdatePermissionAndResourceTypeIsNull() {
        // given
        ResourceEntity resourceWithoutResourceType = ResourceFactory.createNewResource("Orphan");

        when(sessionContext.isCallerInRole(CONFIG_ADMIN)).thenReturn(true);
		when(sessionContext.getCallerPrincipal()).thenReturn(principal);
        myRoles = new HashMap<>();
		RestrictionEntity upd = new RestrictionEntity();
		upd.setAction(Action.UPDATE);
		myRoles.put(CONFIG_ADMIN, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.RESOURCE, upd)));
        permissionService.rolesWithRestrictions = myRoles;

        // when
        boolean result = permissionService.hasPermissionToDeleteRelation(resourceWithoutResourceType, new ContextEntity());

        // then
        Assert.assertFalse(result);
    }

	/**
	 * Screen AppServer: remove node relation
	 */
	@Test
	public void hasPermissionToDeleteResourceRelationWhenUserHasResourceUpdatePermissionAndResourceIsInstanceOfAppServerResType(){
		// given
        ResourceEntity as = resourceEntityBuilder.mockAppServerEntity("as", null, null, null);

		when(sessionContext.isCallerInRole(SERVER_ADMIN)).thenReturn(true);
		when(sessionContext.getCallerPrincipal()).thenReturn(principal);
		myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.ALL);
		myRoles.put(SERVER_ADMIN, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.RESOURCE, res)));
		permissionService.rolesWithRestrictions = myRoles;

		// when
		boolean result = permissionService.hasPermissionToDeleteRelation(as, new ContextEntity());

		// then
		Assert.assertTrue(result);
	}
	
	@Test
	public void hasPermissionToDeleteResourceRelationWhenUserHasResourceUpdatePermissionAndResourceIsNotInstanceOfNodeResType(){
		// given
		ResourceEntity node = resourceEntityBuilder.mockNodeEntity("node", null,null);

		when(sessionContext.isCallerInRole(SERVER_ADMIN)).thenReturn(true);
		when(sessionContext.getCallerPrincipal()).thenReturn(principal);
		myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.UPDATE);
		myRoles.put(SERVER_ADMIN, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.RESOURCE, res)));
		permissionService.rolesWithRestrictions = myRoles;

		// when
		boolean result = permissionService.hasPermissionToDeleteRelation(node, new ContextEntity());

		// then
		Assert.assertTrue(result);
	}
	
	@Test
	public void hasPermissionToDeleteResourceRelationWhenUserIsAppDeveloperAndResourceIsInstanceOfApplicationResType(){
		// given
        ResourceEntity app = resourceEntityBuilder.mockApplicationEntity("app", appResourceGroup, null);

		when(sessionContext.isCallerInRole(APP_DEVELOPER)).thenReturn(true);
		when(sessionContext.getCallerPrincipal()).thenReturn(principal);
		myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.ALL);
		myRoles.put(APP_DEVELOPER, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.RESOURCE, res)));
		permissionService.rolesWithRestrictions = myRoles;

		// when
		boolean result = permissionService.hasPermissionToDeleteRelation(app, new ContextEntity());

		// then
		Assert.assertTrue(result);
	}

	@Test
	public void hasPermissionToDeleteResourceRelationWhenUserHasPermissionToEditAllResourcesOnSpecificEnvironment(){
		// given
		ResourceEntity app = resourceEntityBuilder.mockApplicationEntity("app", appResourceGroup, null);

		when(sessionContext.isCallerInRole(APP_DEVELOPER)).thenReturn(true);
		when(sessionContext.getCallerPrincipal()).thenReturn(principal);
		myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.UPDATE);
		res.setContext(envC);
		myRoles.put(APP_DEVELOPER, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.RESOURCE, res)));
		permissionService.rolesWithRestrictions = myRoles;

		// when
		boolean result = permissionService.hasPermissionToDeleteRelation(app, envC);

		// then
		Assert.assertTrue(result);
	}

	@Test
	public void hasPermissionToDeleteResourceRelationWhenUserHasPermissionToEditSpecificResourceGroupOnAllEnvironments(){
		// given
		ResourceEntity app = resourceEntityBuilder.mockApplicationEntity("app", appResourceGroup, null);

		when(sessionContext.isCallerInRole(APP_DEVELOPER)).thenReturn(true);
		when(sessionContext.getCallerPrincipal()).thenReturn(principal);
		myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.UPDATE);
		res.setResourceGroup(appResourceGroup);
		myRoles.put(APP_DEVELOPER, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.RESOURCE, res)));
		permissionService.rolesWithRestrictions = myRoles;

		// when
		boolean result = permissionService.hasPermissionToDeleteRelation(app, envC) &&
				permissionService.hasPermissionToDeleteRelation(app, envZ);

		// then
		Assert.assertTrue(result);
	}

	@Test
	public void hasNoPermissionToDeleteResourceRelationWhenUserHasPermissionToEditAllResourcesOnAnotherEnvironment(){
		// given
		ResourceEntity app = resourceEntityBuilder.mockApplicationEntity("app", appResourceGroup, null);

		when(sessionContext.isCallerInRole(APP_DEVELOPER)).thenReturn(true);
		when(sessionContext.getCallerPrincipal()).thenReturn(principal);
		myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.UPDATE);
		res.setContext(envC);
		myRoles.put(APP_DEVELOPER, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.RESOURCE, res)));
		permissionService.rolesWithRestrictions = myRoles;

		// when
		boolean result = permissionService.hasPermissionToDeleteRelation(app, envZ);

		// then
		Assert.assertFalse(result);
	}

	@Test
	public void hasNoPermissionToDeleteResourceRelationWhenUserHasPermissionToEditSpecificResourceGroupOnAllEnvironments(){
		// given
		ResourceEntity app = resourceEntityBuilder.mockApplicationEntity("app", appResourceGroup, null);

		ResourceGroupEntity allowedApResourceGroup = new ResourceGroupEntity();
		allowedApResourceGroup.setId(4321);
		allowedApResourceGroup.setResourceType(new ResourceTypeEntityBuilder().id(1).parentResourceType(null).build());

		when(sessionContext.isCallerInRole(APP_DEVELOPER)).thenReturn(true);
		when(sessionContext.getCallerPrincipal()).thenReturn(principal);
		myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.UPDATE);
		res.setResourceGroup(allowedApResourceGroup);
		myRoles.put(APP_DEVELOPER, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.RESOURCE, res)));
		permissionService.rolesWithRestrictions = myRoles;

		// when
		boolean result = permissionService.hasPermissionToDeleteRelation(app, envC);

		// then
		Assert.assertFalse(result);
	}
	
	@Test
	public void hasNoPermissionToDeleteResourceRelationWhenUserHasNoReourceUpdatePermissionAndResourceIsNotInstanceOfApplicationResType(){
		// given
        ResourceEntity as = resourceEntityBuilder.mockAppServerEntity("as", null, null, null);

		when(sessionContext.isCallerInRole(APP_DEVELOPER)).thenReturn(true);
		when(sessionContext.getCallerPrincipal()).thenReturn(principal);
		myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.ALL);
		myRoles.put(APP_DEVELOPER, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.RESOURCETYPE, res)));
		permissionService.rolesWithRestrictions = myRoles;

		// when
		boolean result = permissionService.hasPermissionToDeleteRelation(as, new ContextEntity());

		// then
		Assert.assertFalse(result);
	}

    @Test
    public void hasPermissionToDeleteResourceRelationWhenUserHasPermissionToUpdateResourceAndResourceIsRuntimeType(){
        // given
        ResourceEntity runtime = resourceEntityBuilder.mockRuntimeEntity("EAP6", null, null);

        when(sessionContext.isCallerInRole(SERVER_ADMIN)).thenReturn(true);
		when(sessionContext.getCallerPrincipal()).thenReturn(principal);
        myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.ALL);
		myRoles.put(SERVER_ADMIN, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.RESOURCE, res)));
        permissionService.rolesWithRestrictions = myRoles;

        // when
        boolean result = permissionService.hasPermissionToDeleteRelation(runtime, new ContextEntity());

        // then
        Assert.assertTrue(result);
    }

    @Test
    public void hasPermissionToAddResourceRelationWhenUserHasPermissionToUpdateResource() {
        // given
        ResourceEntity as = resourceEntityBuilder.mockAppServerEntity("as", null, null, null);

        when(sessionContext.isCallerInRole(CONFIG_ADMIN)).thenReturn(true);
		when(sessionContext.getCallerPrincipal()).thenReturn(principal);
        myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.ALL);
		myRoles.put(CONFIG_ADMIN, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.RESOURCE, res)));
        permissionService.rolesWithRestrictions = myRoles;

        // when
        boolean canAdd = permissionService.hasPermissionToAddRelation(as, new ContextEntity());

        // then
        Assert.assertTrue(canAdd);
    }

    @Test
    public void hasNoPermissionToAddResourceRelationWhenResourceTypeIsNull() {
        // given
        //Create resource without resourceType
        ResourceEntity resourceWithoutResourceType = ResourceFactory.createNewResource("Orphan");
        //end Create resource without resourceType

        when(sessionContext.isCallerInRole(CONFIG_ADMIN)).thenReturn(true);
		when(sessionContext.getCallerPrincipal()).thenReturn(principal);
        myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.ALL);
		myRoles.put(CONFIG_ADMIN, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.RESOURCE, res)));
        permissionService.rolesWithRestrictions = myRoles;

        // when
        boolean canAdd = permissionService.hasPermissionToAddRelation(resourceWithoutResourceType, new ContextEntity());

        // then
        Assert.assertFalse(canAdd);
    }

    @Test
    public void hasPermissionToAddResourceRelationWhenUserHasResourceUpdatePermissionForApplicationServerResTypeAndResourceIsInstanceOfApplicationServerResType() {
        // given
        ResourceEntity as = resourceEntityBuilder.mockAppServerEntity("as", asResourceGroup, null, null);

        when(sessionContext.isCallerInRole(SERVER_ADMIN)).thenReturn(true);
		when(sessionContext.getCallerPrincipal()).thenReturn(principal);
        myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.ALL);
		res.setResourceType(ResourceTypeEntityBuilder.APPLICATION_SERVER_TYPE);
		myRoles.put(SERVER_ADMIN, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.RESOURCE, res)));
        permissionService.rolesWithRestrictions = myRoles;

        // when
        boolean canAdd= permissionService.hasPermissionToAddRelation(as, new ContextEntity());

        // then
        Assert.assertTrue(canAdd);
    }

    @Test
    public void hasNoPermissionToAddResourceRelationWhenUserUserHasResourceUpdatePermissionForApplicationServerResTypeAndResourceIsInstanceOfApplicationResType() {
        // given
        ResourceEntity app = resourceEntityBuilder.mockApplicationEntity("app", appResourceGroup, null);

        when(sessionContext.isCallerInRole(SERVER_ADMIN)).thenReturn(true);
		when(sessionContext.getCallerPrincipal()).thenReturn(principal);
        myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.ALL);
		res.setResourceType(ResourceTypeEntityBuilder.APPLICATION_SERVER_TYPE);
		myRoles.put(SERVER_ADMIN, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.RESOURCE, res)));
        permissionService.rolesWithRestrictions = myRoles;

        // when
        boolean canAdd = permissionService.hasPermissionToAddRelation(app, new ContextEntity());

        // then
        Assert.assertFalse(canAdd);
    }

    @Test
	public void hasPermissionToAddResourceRelationWhenUserHasPermissionToUpdateResourceAndResourceIsInstanceOfApplicationResType(){
		// given
        ResourceEntity app = resourceEntityBuilder.mockApplicationEntity("app", appResourceGroup, null);

		when(sessionContext.isCallerInRole(APP_DEVELOPER)).thenReturn(true);
		when(sessionContext.getCallerPrincipal()).thenReturn(principal);
		myRoles = new HashMap<>();
		RestrictionEntity upd = new RestrictionEntity();
		upd.setAction(Action.UPDATE);
		myRoles.put(APP_DEVELOPER, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.RESOURCE, upd)));
		permissionService.rolesWithRestrictions = myRoles;

		// when
        boolean canAdd = permissionService.hasPermissionToAddRelation(app, new ContextEntity());

		// then
		Assert.assertTrue(canAdd);
	}

    @Test
    public void hasNoPermissionToAddResourceRelationWhenUserUserHasNoPermissionToUpdateResourceAndResourceIsNotInstanceOfApplicationResType() {
        // given
        ResourceEntity as = resourceEntityBuilder.mockAppServerEntity("as", null, null, null);

        when(sessionContext.isCallerInRole(APP_DEVELOPER)).thenReturn(true);
		when(sessionContext.getCallerPrincipal()).thenReturn(principal);
        myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.READ);
		myRoles.put(APP_DEVELOPER, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.RESOURCE, res)));
        permissionService.rolesWithRestrictions = myRoles;

        // when
        boolean canAdd = permissionService.hasPermissionToAddRelation(as, new ContextEntity());

        // then
        Assert.assertFalse(canAdd);
    }

    @Test
    public void hasPermissionToTemplateModifyWhenUserIsShakedownAdminAndIsTestingMode() {
        // given
        ResourceEntity as = resourceEntityBuilder.mockAppServerEntity("as", null, null, null);

        when(sessionContext.isCallerInRole(SHAKEDOWN_ADMIN)).thenReturn(true);
		when(sessionContext.getCallerPrincipal()).thenReturn(principal);
        myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.ALL);
		myRoles.put(SHAKEDOWN_ADMIN, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.SHAKEDOWN_TEST_MODE, res)));
        permissionService.rolesWithRestrictions = myRoles;

        // when
        boolean result = permissionService.hasPermissionToAddResourceTemplate(as, true);

        // then
        Assert.assertTrue(result);
    }

    @Test
    public void hasPermissionToTemplateModifyWhenUserIsNotShakedownAdminAndIsTestingMode() {
        // given
        ResourceEntity as = resourceEntityBuilder.mockAppServerEntity("as", null, null, null);

        when(sessionContext.isCallerInRole(VIEWER)).thenReturn(true);
		when(sessionContext.getCallerPrincipal()).thenReturn(principal);
        myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.ALL);
		myRoles.put(VIEWER, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.DEFAULT, res)));
        permissionService.rolesWithRestrictions = myRoles;

        // when
        boolean result = permissionService.hasPermissionToAddResourceTemplate(as, true);

        // then
        Assert.assertFalse(result);
    }

    @Test
    public void hasNoPermissionToAddTemplateWhenUserHasPermissionToUpdateResourceAndIsResourceIsNotTestingMode() {
        // given
        ResourceEntity as = resourceEntityBuilder.mockAppServerEntity("as", null, null, null);

        when(sessionContext.isCallerInRole(CONFIG_ADMIN)).thenReturn(true);
		when(sessionContext.getCallerPrincipal()).thenReturn(principal);
        myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.ALL);
		myRoles.put(CONFIG_ADMIN, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.RESOURCE, res)));
        permissionService.rolesWithRestrictions = myRoles;

        // when
        boolean result = permissionService.hasPermissionToAddResourceTemplate(as, false);

        // then
        Assert.assertFalse(result);
    }

	@Test
	public void hasPermissionToAddTemplateWhenUserHasPermissionToCreateTemplateResourceAndIsResourceIsNotTestingMode() {
		// given
		ResourceEntity as = resourceEntityBuilder.mockAppServerEntity("as", null, null, null);

		when(sessionContext.isCallerInRole(CONFIG_ADMIN)).thenReturn(true);
		when(sessionContext.getCallerPrincipal()).thenReturn(principal);
		myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.CREATE);
		myRoles.put(CONFIG_ADMIN, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.RESOURCE_TEMPLATE, res)));
		permissionService.rolesWithRestrictions = myRoles;

		// when
		boolean result = permissionService.hasPermissionToAddResourceTemplate(as, false);

		// then
		Assert.assertTrue(result);
	}

    @Test
    public void hasNoPermissionToAddResourceTypeTemplateWhenUserHasPermissionToUpdateResourceTypeAndIsApplicationResTypeAndIsNotTestingMode() {
        // given
		ResourceTypeEntity app = ResourceTypeEntityBuilder.APPLICATION_TYPE;

        when(sessionContext.isCallerInRole(APP_DEVELOPER)).thenReturn(true);
		when(sessionContext.getCallerPrincipal()).thenReturn(principal);
        myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.ALL);
		res.setResourceTypePermission(ResourceTypePermission.ANY);
		myRoles.put(APP_DEVELOPER, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.RESOURCETYPE, res)));
        permissionService.rolesWithRestrictions = myRoles;

        // when
        boolean result = permissionService.hasPermissionToAddResourceTypeTemplate(app, false);

        // then
        Assert.assertFalse(result);
    }

	@Test
	public void hasPermissionToAddResourceTypeTemplateWhenUserHasPermissionToCreateTemplateResourceTypeAndIsApplicationResTypeAndIsNotTestingMode() {
		// given
		ResourceTypeEntity app = ResourceTypeEntityBuilder.APPLICATION_TYPE;

		when(sessionContext.isCallerInRole(APP_DEVELOPER)).thenReturn(true);
		when(sessionContext.getCallerPrincipal()).thenReturn(principal);
		myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.CREATE);
		res.setResourceTypePermission(ResourceTypePermission.ANY);
		myRoles.put(APP_DEVELOPER, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.RESOURCETYPE_TEMPLATE, res)));
		permissionService.rolesWithRestrictions = myRoles;

		// when
		boolean result = permissionService.hasPermissionToAddResourceTypeTemplate(app, false);

		// then
		Assert.assertTrue(result);
	}

    @Test
    public void hasPermissionToTemplateModifyWhenUserIsShakedownAdminAndIsNotTestingMode() {
        // given
        ResourceEntity as = resourceEntityBuilder.mockAppServerEntity("as", null, null, null);

        when(sessionContext.isCallerInRole(SHAKEDOWN_ADMIN)).thenReturn(true);
		when(sessionContext.getCallerPrincipal()).thenReturn(principal);
        myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.ALL);
		myRoles.put(SHAKEDOWN_ADMIN, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.SHAKEDOWN_TEST_MODE, res)));
        permissionService.rolesWithRestrictions = myRoles;

        // when
        boolean result = permissionService.hasPermissionToAddResourceTemplate(as, false);

        // then
        Assert.assertFalse(result);
    }
	
	@Test
	public void hasPermissionToDeployWhenEmptyList(){
		// given
		List<RoleEntity>deployableRoles = new ArrayList<>();
		EntityManager entityManager = Mockito.mock(EntityManager.class);
		Query value = Mockito.mock(Query.class);
		when(entityManager.createQuery("from RoleEntity r where r.deployable=1")).thenReturn(value);
		when(value.getResultList()).thenReturn(deployableRoles);
		when(sessionContext.isCallerInRole(ROLE_NOT_DEPLOY)).thenReturn(true);
		when(sessionContext.getCallerPrincipal()).thenReturn(principal);
		// when
		boolean result = permissionService.hasPermissionToSeeDeployment();
		// then
		Assert.assertFalse(result);
	}

	@Test
	public void hasPermissionToDeployOnEnvironmentCTest(){
		// given
		RoleEntity roleTestDeployer = new RoleEntity();
		roleTestDeployer.setName(TEST_DEPLOYER);
		PermissionEntity permissionToDeploy = new PermissionEntity();
		permissionToDeploy.setValue(Permission.DEPLOYMENT.name());
		when(sessionContext.isCallerInRole(TEST_DEPLOYER)).thenReturn(true);
		when(sessionContext.getCallerPrincipal()).thenReturn(principal);
		myRoles = new HashMap<>();
		//assign restriction allowing all on environment "c"
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.ALL);
		res.setContext(envC);
		res.setPermission(permissionToDeploy);
		myRoles.put(TEST_DEPLOYER, Arrays.asList(new RestrictionDTO(res)));
		permissionService.deployableRolesWithRestrictions = myRoles;
		when(permissionService.permissionRepository.isReloadDeployableRoleList()).thenReturn(false);

		// when
		boolean result = permissionService.hasPermissionForDeploymentOnContext(envC, new ResourceGroupEntity());

		// then
		Assert.assertTrue(result);
	}

	@Test
	public void hasNoPermissionToDeployOnEnvironmentZTest(){
		// given
		RoleEntity roleTestDeployer = new RoleEntity();
		roleTestDeployer.setName(TEST_DEPLOYER);
		PermissionEntity permissionToDeploy = new PermissionEntity();
		permissionToDeploy.setValue(Permission.DEPLOYMENT.name());
		when(sessionContext.isCallerInRole(TEST_DEPLOYER)).thenReturn(true);
		when(sessionContext.getCallerPrincipal()).thenReturn(principal);
		myRoles = new HashMap<>();
		//assign restriction allowing all on environment "c"
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.ALL);
		res.setContext(envC);
		res.setPermission(permissionToDeploy);
		myRoles.put(TEST_DEPLOYER, Arrays.asList(new RestrictionDTO(res)));
		permissionService.deployableRolesWithRestrictions = myRoles;
		when(permissionService.permissionRepository.isReloadDeployableRoleList()).thenReturn(false);

		// when
		boolean resC = permissionService.hasPermissionForDeploymentOnContext(envC, new ResourceGroupEntity());
		boolean resZ = permissionService.hasPermissionForDeploymentOnContext(envZ, new ResourceGroupEntity());

		// then
		Assert.assertTrue(resC);
		Assert.assertFalse(resZ);
	}

	@Test
	public void hasPermissionToDeployOnlySpecificResourceOnEnvironmentCTest(){
		// given
		ResourceGroupEntity allowedResourceGroup = new ResourceGroupEntity();
		allowedResourceGroup.setId(42);
		allowedResourceGroup.setResourceType(new ResourceTypeEntityBuilder().id(2).build());
		RoleEntity roleTestDeployer = new RoleEntity();
		roleTestDeployer.setName(TEST_DEPLOYER);
		PermissionEntity permissionToDeploy = new PermissionEntity();
		permissionToDeploy.setValue(Permission.DEPLOYMENT.name());
		when(sessionContext.isCallerInRole(TEST_DEPLOYER)).thenReturn(true);
		when(sessionContext.getCallerPrincipal()).thenReturn(principal);
		myRoles = new HashMap<>();
		//assign restriction allowing specific resourceGroup on environment "c"
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.ALL);
		res.setResourceTypePermission(ResourceTypePermission.ANY);
		res.setContext(envC);
		res.setResourceGroup(allowedResourceGroup);
		res.setPermission(permissionToDeploy);
		myRoles.put(TEST_DEPLOYER, Arrays.asList(new RestrictionDTO(res)));
		permissionService.deployableRolesWithRestrictions = myRoles;
		when(permissionService.permissionRepository.isReloadDeployableRoleList()).thenReturn(false);

		// when
		boolean resC = permissionService.hasPermissionForDeploymentOnContext(envC, appResourceGroup);
		boolean resCAllowed = permissionService.hasPermissionForDeploymentOnContext(envC, allowedResourceGroup);
		boolean resZ = permissionService.hasPermissionForDeploymentOnContext(envZ, allowedResourceGroup);

		// then
		Assert.assertFalse(resC);
		Assert.assertTrue(resCAllowed);
		Assert.assertFalse(resZ);
	}

	@Test
	public void hasPermissionToDeployOnlySpecificResourceOnEnvironmentCTestGrantedByUserRestriction(){
		// given
		ResourceGroupEntity allowedResourceGroup = new ResourceGroupEntity();
		allowedResourceGroup.setId(42);
		allowedResourceGroup.setResourceType(new ResourceTypeEntityBuilder().id(2).build());
		RoleEntity roleTestDeployer = new RoleEntity();
		roleTestDeployer.setName(TEST_DEPLOYER);
		PermissionEntity permissionToDeploy = new PermissionEntity();
		permissionToDeploy.setValue(Permission.DEPLOYMENT.name());
		when(sessionContext.isCallerInRole(TEST_DEPLOYER)).thenReturn(true);
		when(sessionContext.getCallerPrincipal()).thenReturn(principal);
		myRoles = new HashMap<>();

		//assign user a restriction allowing specific resourceGroup on environment "c"
		UserRestrictionEntity userRestriction = new UserRestrictionEntity(principal.getName());
		userRestriction.setId(123);
		PermissionEntity deploymentPermission =  new PermissionEntity();
		deploymentPermission.setValue(Permission.DEPLOYMENT.name());
		RestrictionEntity restriction = new RestrictionEntity();
		restriction.setUser(userRestriction);
		restriction.setPermission(deploymentPermission);
		restriction.setResourceTypePermission(ResourceTypePermission.ANY);
		restriction.setContext(envC);
		restriction.setResourceGroup(allowedResourceGroup);
		when(permissionRepository.getUserWithRestrictions(anyString())).thenReturn(Arrays.asList(restriction));

		myRoles.put(TEST_DEPLOYER, Collections.EMPTY_LIST);
		permissionService.deployableRolesWithRestrictions = myRoles;
		when(permissionService.permissionRepository.isReloadDeployableRoleList()).thenReturn(false);

		// when
		boolean resC = permissionService.hasPermissionForDeploymentOnContext(envC, appResourceGroup);
		boolean resCAllowed = permissionService.hasPermissionForDeploymentOnContext(envC, allowedResourceGroup);
		boolean resZ = permissionService.hasPermissionForDeploymentOnContext(envZ, allowedResourceGroup);

		// then
		Assert.assertFalse(resC);
		Assert.assertTrue(resCAllowed);
		Assert.assertFalse(resZ);
	}

	@Test
	public void hasPermissionToDeployOnlySpecificResourceTypeOnEnvironmentZTest(){
		// given
		ResourceTypeEntity allowedResourceType = new ResourceTypeEntityBuilder().id(7).build();
		ResourceGroupEntity resourceGroup = new ResourceGroupEntity();
		resourceGroup.setId(42);
		resourceGroup.setResourceType(allowedResourceType);
		RoleEntity roleTestDeployer = new RoleEntity();
		roleTestDeployer.setName(TEST_DEPLOYER);
		PermissionEntity permissionToDeploy = new PermissionEntity();
		permissionToDeploy.setValue(Permission.DEPLOYMENT.name());
		when(sessionContext.isCallerInRole(TEST_DEPLOYER)).thenReturn(true);
		when(sessionContext.getCallerPrincipal()).thenReturn(principal);
		myRoles = new HashMap<>();
		//assign restriction allowing specific resourcetype on environment "z"
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.ALL);
		res.setResourceTypePermission(ResourceTypePermission.ANY);
		res.setContext(envZ);
		res.setResourceType(allowedResourceType);
		res.setPermission(permissionToDeploy);
		myRoles.put(TEST_DEPLOYER, Arrays.asList(new RestrictionDTO(res)));
		permissionService.deployableRolesWithRestrictions = myRoles;
		when(permissionService.permissionRepository.isReloadDeployableRoleList()).thenReturn(false);

		// when
		boolean resZ = permissionService.hasPermissionForDeploymentOnContext(envZ, appResourceGroup);
		boolean resZAllowed = permissionService.hasPermissionForDeploymentOnContext(envZ, resourceGroup);
		boolean resC = permissionService.hasPermissionForDeploymentOnContext(envC, resourceGroup);

		// then
		Assert.assertFalse(resZ);
		Assert.assertTrue(resZAllowed);
		Assert.assertFalse(resC);
	}

	@Test
	public void hasPermissionToDeployOnlySpecificResourceTypeOnAllEnvironmentsTest(){
		// given
		ResourceTypeEntity allowedResourceType = new ResourceTypeEntityBuilder().id(7).build();
		ResourceGroupEntity resourceGroup = new ResourceGroupEntity();
		resourceGroup.setId(42);
		resourceGroup.setResourceType(allowedResourceType);
		RoleEntity roleTestDeployer = new RoleEntity();
		roleTestDeployer.setName(TEST_DEPLOYER);
		PermissionEntity permissionToDeploy = new PermissionEntity();
		permissionToDeploy.setValue(Permission.DEPLOYMENT.name());
		when(sessionContext.isCallerInRole(TEST_DEPLOYER)).thenReturn(true);
		when(sessionContext.getCallerPrincipal()).thenReturn(principal);
		myRoles = new HashMap<>();
		//assign restriction allowing specific resourcetype on all environments
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.ALL);
		res.setResourceTypePermission(ResourceTypePermission.ANY);
		res.setResourceType(allowedResourceType);
		res.setPermission(permissionToDeploy);
		myRoles.put(TEST_DEPLOYER, Arrays.asList(new RestrictionDTO(res)));
		permissionService.deployableRolesWithRestrictions = myRoles;
		when(permissionService.permissionRepository.isReloadDeployableRoleList()).thenReturn(false);

		// when
		boolean resZ = permissionService.hasPermissionForDeploymentOnContext(envZ, appResourceGroup);
		boolean resZAllowed = permissionService.hasPermissionForDeploymentOnContext(envZ, resourceGroup);
		boolean resCAllowed = permissionService.hasPermissionForDeploymentOnContext(envC, resourceGroup);

		// then
		Assert.assertFalse(resZ);
		Assert.assertTrue(resZAllowed);
		Assert.assertTrue(resCAllowed);
	}

	@Test
	public void hasPermissionToDeployOnChildsIfHasPermissionForParentTest(){
		// given
		RoleEntity roleTestDeployer = new RoleEntity();
		roleTestDeployer.setName(TEST_DEPLOYER);
		PermissionEntity permissionToDeploy = new PermissionEntity();
		permissionToDeploy.setValue(Permission.DEPLOYMENT.name());
		when(sessionContext.isCallerInRole(TEST_DEPLOYER)).thenReturn(true);
		when(sessionContext.getCallerPrincipal()).thenReturn(principal);
		myRoles = new HashMap<>();
		//assign restriction allowing all on test environment
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.ALL);
		res.setResourceTypePermission(ResourceTypePermission.ANY);
		res.setContext(test);
		res.setPermission(permissionToDeploy);
		myRoles.put(TEST_DEPLOYER, Arrays.asList(new RestrictionDTO(res)));
		permissionService.deployableRolesWithRestrictions = myRoles;
		when(permissionService.permissionRepository.isReloadDeployableRoleList()).thenReturn(false);

		// when
		boolean resC = permissionService.hasPermissionForDeploymentOnContext(envC, appResourceGroup);
		boolean resZ = permissionService.hasPermissionForDeploymentOnContext(envZ, appResourceGroup);

		// then
		Assert.assertTrue(resC);
		Assert.assertTrue(resZ);
	}

	@Test
	public void hasPermissionToDeployOnAllEnvironmentsIfHasPermissionForGlobalTest(){
		// given
		RoleEntity roleTestDeployer = new RoleEntity();
		roleTestDeployer.setName(TEST_DEPLOYER);
		PermissionEntity permissionToDeploy = new PermissionEntity();
		permissionToDeploy.setValue(Permission.DEPLOYMENT.name());
		when(sessionContext.isCallerInRole(TEST_DEPLOYER)).thenReturn(true);
		when(sessionContext.getCallerPrincipal()).thenReturn(principal);
		myRoles = new HashMap<>();
		//assign restriction allowing all on global context
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.ALL);
		res.setContext(global);
		res.setResourceTypePermission(ResourceTypePermission.ANY);
		res.setPermission(permissionToDeploy);
		myRoles.put(TEST_DEPLOYER, Arrays.asList(new RestrictionDTO(res)));
		permissionService.deployableRolesWithRestrictions = myRoles;
		when(permissionService.permissionRepository.isReloadDeployableRoleList()).thenReturn(false);

		// when
		boolean resGlobal = permissionService.hasPermissionForDeploymentOnContext(global, appResourceGroup);
		boolean resParent = permissionService.hasPermissionForDeploymentOnContext(test, appResourceGroup);
		boolean resC = permissionService.hasPermissionForDeploymentOnContext(envC, appResourceGroup);
		boolean resZ = permissionService.hasPermissionForDeploymentOnContext(envZ, appResourceGroup);

		// then
		Assert.assertTrue(resGlobal);
		Assert.assertTrue(resParent);
		Assert.assertTrue(resC);
		Assert.assertTrue(resZ);
	}

	@Test
	public void multipleDeployUserRestriction(){
		// given
		List<RestrictionEntity> userRestriction = new LinkedList<>();

		ResourceGroupEntity resGroup1 = new ResourceGroupEntity();
		resGroup1.setId(111);
		resGroup1.setResourceType(new ResourceTypeEntityBuilder().id(1).parentResourceType(null).build());
		ResourceGroupEntity resGroup2 = new ResourceGroupEntity();
		resGroup2.setId(222);
		resGroup2.setResourceType(new ResourceTypeEntityBuilder().id(1).parentResourceType(null).build());
		ResourceGroupEntity forbiddenResGroup3 = new ResourceGroupEntity();
		forbiddenResGroup3.setId(333);
		forbiddenResGroup3.setResourceType(new ResourceTypeEntityBuilder().id(1).parentResourceType(null).build());

		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.ALL);
		res.setResourceGroup(resGroup1);
		PermissionEntity permission = new PermissionEntity();
		permission.setValue(Permission.DEPLOYMENT.name());
		res.setPermission(permission);
		userRestriction.add(res);
		RestrictionEntity res2 = new RestrictionEntity();
		res2.setAction(Action.ALL);
		res2.setResourceGroup(resGroup2);
		PermissionEntity permission2 = new PermissionEntity();
		permission2.setValue(Permission.DEPLOYMENT.name());
		res2.setPermission(permission2);
		userRestriction.add(res2);

		when(permissionRepository.getUserWithRestrictions(principal.getName())).thenReturn(userRestriction);
		when(sessionContext.getCallerPrincipal()).thenReturn(principal);

		// when
		boolean result1 = permissionService.hasPermission(Permission.DEPLOYMENT, envC, Action.CREATE, resGroup1, null);
		boolean result2 = permissionService.hasPermission(Permission.DEPLOYMENT, envC, Action.CREATE, resGroup2, null);
		boolean result3 = permissionService.hasPermission(Permission.DEPLOYMENT, envC, Action.CREATE, forbiddenResGroup3, null);

		// then
		Assert.assertTrue(result1);
		Assert.assertTrue(result2);
		Assert.assertFalse(result3);
	}
	
	@Test
	public void hasPermissionToAddResourceTypeTemplateWhenUserIsShakedownAdminAndIsTestingMode() {
		// given
		ResourceTypeEntity as = ResourceTypeEntityBuilder.APPLICATION_SERVER_TYPE;

		when(sessionContext.isCallerInRole(SHAKEDOWN_ADMIN)).thenReturn(true);
		when(sessionContext.getCallerPrincipal()).thenReturn(principal);
		myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.ALL);
		myRoles.put(SHAKEDOWN_ADMIN, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.SHAKEDOWN_TEST_MODE, res)));
		permissionService.rolesWithRestrictions = myRoles;

		// when
		boolean result = permissionService.hasPermissionToAddResourceTypeTemplate(as, true);

		// then
		Assert.assertTrue(result);
	}

	@Test
	public void hasPermissionToAddResourceTypeTemplateWhenUserIsNotShakedownAdminAndIsTestingMode() {
		// given
		ResourceTypeEntity as = ResourceTypeEntityBuilder.APPLICATION_SERVER_TYPE;

		when(sessionContext.isCallerInRole(VIEWER)).thenReturn(true);
		when(sessionContext.getCallerPrincipal()).thenReturn(principal);
		myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.ALL);
		myRoles.put(VIEWER, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.DEFAULT, res)));
		permissionService.rolesWithRestrictions = myRoles;

		// when
		boolean result = permissionService.hasPermissionToAddResourceTypeTemplate(as, true);

		// then
		Assert.assertFalse(result);
	}

	@Test
	public void hasPermissionToAddResourceTypeTemplateWhenUserHasResourceTypePermissionAndIsNotTestingMode() {
		// given
		ResourceTypeEntity as = ResourceTypeEntityBuilder.APPLICATION_SERVER_TYPE;

		when(sessionContext.isCallerInRole(CONFIG_ADMIN)).thenReturn(true);
		when(sessionContext.getCallerPrincipal()).thenReturn(principal);
		myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.ALL);
		res.setResourceTypePermission(ResourceTypePermission.ANY);
		myRoles.put(CONFIG_ADMIN, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.RESOURCETYPE, res),
				new RestrictionDTOBuilder().mockRestrictionDTO(Permission.RESOURCETYPE_TEMPLATE, res)));
		permissionService.rolesWithRestrictions = myRoles;

		// when
		boolean result = permissionService.hasPermissionToAddResourceTypeTemplate(as, false);

		// then
		Assert.assertTrue(result);
	}

	@Test
	public void hasNoPermissionToAddResourceTypeTemplateWhenUserHasOnlyResourceTypePermissionAndIsApplicationResTypeAndIsNotTestingMode() {
		// given
		ResourceTypeEntity app = ResourceTypeEntityBuilder.APPLICATION_TYPE;

		when(sessionContext.isCallerInRole(APP_DEVELOPER)).thenReturn(true);
		when(sessionContext.getCallerPrincipal()).thenReturn(principal);
		myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.ALL);
		res.setResourceTypePermission(ResourceTypePermission.ANY);
		myRoles.put(APP_DEVELOPER, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.RESOURCETYPE, res)));
		permissionService.rolesWithRestrictions = myRoles;

		// when
		boolean result = permissionService.hasPermissionToAddResourceTypeTemplate(app, false);

		// then
		Assert.assertFalse(result);
	}

	@Test
	public void hasNoPermissionToAddResourceTypeTemplateWhenUserIsShakedownAdminAndIsNotTestingMode() {
		// given
		ResourceTypeEntity as = ResourceTypeEntityBuilder.APPLICATION_SERVER_TYPE;

		when(sessionContext.isCallerInRole(SHAKEDOWN_ADMIN)).thenReturn(true);
		when(sessionContext.getCallerPrincipal()).thenReturn(principal);
		myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.ALL);
		myRoles.put(SHAKEDOWN_ADMIN, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.SHAKEDOWN_TEST_MODE, res)));
		permissionService.rolesWithRestrictions = myRoles;

		// when
		boolean result = permissionService.hasPermissionToAddResourceTypeTemplate(as, false);

		// then
		Assert.assertFalse(result);
	}
	
	@Test
	public void permissionInTwoRoles(){
		// given
		when(sessionContext.isCallerInRole(CONFIG_ADMIN)).thenReturn(false);
		when(sessionContext.isCallerInRole(SERVER_ADMIN)).thenReturn(true);
		when(sessionContext.getCallerPrincipal()).thenReturn(principal);
		myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.ALL);
		myRoles.put(CONFIG_ADMIN, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.RELEASE, res)));
		myRoles.put(SERVER_ADMIN, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.RELEASE, res)));
		permissionService.rolesWithRestrictions = myRoles;
			
		// when
		boolean result = permissionService.hasPermission(Permission.RELEASE);
		
		// then
		Assert.assertTrue(result);
	}
	
	@Test
	public void permissionInTwoRolesFail(){
		// given
		when(sessionContext.isCallerInRole(CONFIG_ADMIN)).thenReturn(false);
		when(sessionContext.isCallerInRole(SERVER_ADMIN)).thenReturn(false);
		when(sessionContext.getCallerPrincipal()).thenReturn(principal);
		myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.ALL);
		myRoles.put(CONFIG_ADMIN, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.RELEASE, res)));
		myRoles.put(SERVER_ADMIN, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.RELEASE, res)));
		permissionService.rolesWithRestrictions = myRoles;
			
		// when
		boolean result = permissionService.hasPermission(Permission.RELEASE);
		
		// then
		Assert.assertFalse(result);
	}

	@Test
	public void shouldOnlyReloadWhenNeeded() {
		// given
		when(permissionService.permissionRepository.isReloadRolesAndPermissionsList()).thenReturn(false);
		permissionService.rolesWithRestrictions = new HashMap<>();

		// when
		permissionService.getPermissions();

		// then
		verify(permissionService.permissionRepository, never()).getRolesWithRestrictions();
	}

	@Test
	public void shouldObtainRolesWithRestrictions() {
		// given
		when(permissionService.permissionRepository.isReloadRolesAndPermissionsList()).thenReturn(true);
		when(permissionService.permissionRepository.getRolesWithRestrictions()).thenReturn(null);

		// when
		permissionService.getPermissions();

		// then
		verify(permissionService.permissionRepository, times(1)).getRolesWithRestrictions();
	}

	@Test
	public void shouldObtainDeployableRolesOnGetDeployableRolesWhenNeeded() {
		// given
		when(permissionService.permissionRepository.isReloadDeployableRoleList()).thenReturn(true);
		when(permissionService.permissionRepository.getDeployableRoles()).thenReturn(EMPTY_LIST);

		// when
		permissionService.getDeployableRoles();

		// then
		verify(permissionService.permissionRepository, times(1)).getDeployableRoles();
	}

	@Test
	public void shouldSucceedIfAPermissionCheckIsDoneWithoutContextAndPermissionIsGrantedToRoleOnSpecificContext() {
		// given
		ResourceTypeEntity resourceType = new ResourceTypeEntityBuilder().id(7).build();
		ResourceGroupEntity resourceGroup = new ResourceGroupEntity();
		resourceGroup.setId(23);
		resourceGroup.setResourceType(resourceType);
		when(sessionContext.isCallerInRole(CONFIG_ADMIN)).thenReturn(true);
		when(sessionContext.getCallerPrincipal()).thenReturn(principal);
		myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.UPDATE);
		res.setContext(envC);
		myRoles.put(CONFIG_ADMIN, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.RESOURCE, res)));
		permissionService.rolesWithRestrictions = myRoles;

		// when
		boolean result = permissionService.hasPermission(Permission.RESOURCE, null, Action.UPDATE, resourceGroup, null);

		// then
		Assert.assertTrue(result);
	}

	@Test
	public void shouldSucceedIfAPermissionCheckIsDoneWithoutContextAndPermissionIsGrantedToUserOnSpecificContext() {
		// given
		ResourceTypeEntity resourceType = new ResourceTypeEntityBuilder().id(7).build();
		ResourceGroupEntity resourceGroup = new ResourceGroupEntity();
		resourceGroup.setId(23);
		resourceGroup.setResourceType(resourceType);
		when(sessionContext.isCallerInRole(CONFIG_ADMIN)).thenReturn(true);
		when(sessionContext.getCallerPrincipal()).thenReturn(principal);
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.UPDATE);
		res.setContext(envC);
		PermissionEntity perm = new PermissionEntity();
		perm.setValue(Permission.RESOURCE.name());
		res.setPermission(perm);
		myRoles = new HashMap<>();
		permissionService.rolesWithRestrictions = myRoles;
		when(permissionRepository.getUserWithRestrictions(anyString())).thenReturn(Arrays.asList(res));

		// when
		boolean result = permissionService.hasPermission(Permission.RESOURCE, null, Action.UPDATE, resourceGroup, null);

		// then
		Assert.assertTrue(result);
	}


	@Test
	public void shouldFailIfAPermissionIsRequiredOnAllContextsButOnlyGrantedToGroupOnASpecificContext() {
		// given
		ResourceTypeEntity resourceType = new ResourceTypeEntityBuilder().id(7).build();
		ResourceGroupEntity resourceGroup = new ResourceGroupEntity();
		resourceGroup.setId(23);
		resourceGroup.setResourceType(resourceType);
		when(sessionContext.isCallerInRole(CONFIG_ADMIN)).thenReturn(true);
		when(sessionContext.getCallerPrincipal()).thenReturn(principal);
		myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.ALL);
		res.setContext(envC);
		myRoles.put(CONFIG_ADMIN, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.RESOURCE_PROPERTY_DECRYPT, res)));
		permissionService.rolesWithRestrictions = myRoles;

		// when
		boolean result = permissionService.hasPermissionOnAllContext(Permission.RESOURCE_PROPERTY_DECRYPT, Action.ALL, resourceGroup, null);

		// then
		Assert.assertFalse(result);
	}

	@Test
	public void shouldFailIfAPermissionIsRequiredOnAllContextsButOnlyGrantedToUserOnASpecificContext() {
		// given
		ResourceTypeEntity resourceType = new ResourceTypeEntityBuilder().id(7).build();
		ResourceGroupEntity resourceGroup = new ResourceGroupEntity();
		resourceGroup.setId(23);
		resourceGroup.setResourceType(resourceType);
		when(sessionContext.isCallerInRole(CONFIG_ADMIN)).thenReturn(true);
		when(sessionContext.getCallerPrincipal()).thenReturn(principal);
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.ALL);
		res.setContext(envC);
		PermissionEntity perm = new PermissionEntity();
		perm.setValue(Permission.RESOURCE_PROPERTY_DECRYPT.name());
		res.setPermission(perm);
		myRoles = new HashMap<>();
		permissionService.rolesWithRestrictions = myRoles;
		when(permissionRepository.getUserWithRestrictions(anyString())).thenReturn(Arrays.asList(res));

		// when
		boolean result = permissionService.hasPermissionOnAllContext(Permission.RESOURCE_PROPERTY_DECRYPT, Action.ALL, resourceGroup, null);

		// then
		Assert.assertFalse(result);
	}

	@Test
	public void shouldSucceedIfAPermissionIsRequiredOnAllContextsAndGrantedToRoleOnAllContexts() {
		// given
		ResourceTypeEntity resourceType = new ResourceTypeEntityBuilder().id(7).build();
		ResourceGroupEntity resourceGroup = new ResourceGroupEntity();
		resourceGroup.setId(23);
		resourceGroup.setResourceType(resourceType);
		when(sessionContext.isCallerInRole(CONFIG_ADMIN)).thenReturn(true);
		when(sessionContext.getCallerPrincipal()).thenReturn(principal);
		myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.ALL);
		myRoles.put(CONFIG_ADMIN, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.RESOURCE_PROPERTY_DECRYPT, res)));
		permissionService.rolesWithRestrictions = myRoles;

		// when
		boolean result = permissionService.hasPermissionOnAllContext(Permission.RESOURCE_PROPERTY_DECRYPT, Action.ALL, resourceGroup, null);

		// then
		Assert.assertTrue(result);
	}

	@Test
	public void shouldSucceedIfAPermissionIsRequiredOnAllContextsAndGrantedToUserOnAllContext() {
		// given
		ResourceTypeEntity resourceType = new ResourceTypeEntityBuilder().id(7).build();
		ResourceGroupEntity resourceGroup = new ResourceGroupEntity();
		resourceGroup.setId(23);
		resourceGroup.setResourceType(resourceType);
		when(sessionContext.isCallerInRole(CONFIG_ADMIN)).thenReturn(true);
		when(sessionContext.getCallerPrincipal()).thenReturn(principal);
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.ALL);
		PermissionEntity perm = new PermissionEntity();
		perm.setValue(Permission.RESOURCE_PROPERTY_DECRYPT.name());
		res.setPermission(perm);
		myRoles = new HashMap<>();
		permissionService.rolesWithRestrictions = myRoles;
		when(permissionRepository.getUserWithRestrictions(anyString())).thenReturn(Arrays.asList(res));

		// when
		boolean result = permissionService.hasPermissionOnAllContext(Permission.RESOURCE_PROPERTY_DECRYPT, Action.ALL, resourceGroup, null);

		// then
		Assert.assertTrue(result);
	}

	@Test
	public void shouldReturnFalseIfCallerHasNoDelegationPermission() {
		// given
		when(sessionContext.isCallerInRole(CONFIG_ADMIN)).thenReturn(true);
		when(sessionContext.getCallerPrincipal()).thenReturn(principal);
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.ALL);
		PermissionEntity perm = new PermissionEntity();
		perm.setValue(Permission.RESOURCE_PROPERTY_DECRYPT.name());
		res.setPermission(perm);
		myRoles = new HashMap<>();
		permissionService.rolesWithRestrictions = myRoles;
		when(permissionRepository.getUserWithRestrictions(anyString())).thenReturn(Arrays.asList(res));

		// when
		boolean result = permissionService.hasPermissionToDelegatePermission(Permission.RESOURCE_PROPERTY_DECRYPT, null, null, null, Action.CREATE);

		// then
		Assert.assertFalse(result);
	}

	@Test
	public void shouldReturnFalseIfCallerHasDelegationPermissionButNotSimilarRestriction() {
		// given
		when(sessionContext.isCallerInRole(CONFIG_ADMIN)).thenReturn(true);
		when(sessionContext.getCallerPrincipal()).thenReturn(principal);
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.ALL);
		PermissionEntity perm = new PermissionEntity();
		perm.setValue(Permission.PERMISSION_DELEGATION.name());
		res.setPermission(perm);
		myRoles = new HashMap<>();
		permissionService.rolesWithRestrictions = myRoles;
		when(permissionRepository.getUserWithRestrictions(anyString())).thenReturn(Arrays.asList(res));

		// when
		boolean result = permissionService.hasPermissionToDelegatePermission(Permission.RESOURCE_PROPERTY_DECRYPT, null, null, null, Action.CREATE);

		// then
		Assert.assertFalse(result);
	}

	@Test
	public void shouldReturnFalseIfCallerHasDelegationPermissionButHisSimilarRestrictionIsRestrictedToAnExplicitResourceGroupAndTheOneHeWantsToDelegateIsNot() {
		// given
		ResourceTypeEntity resourceType = new ResourceTypeEntityBuilder().id(7).build();
		ResourceGroupEntity resourceGroup = new ResourceGroupEntity();
		resourceGroup.setId(23);
		resourceGroup.setResourceType(resourceType);
		when(sessionContext.isCallerInRole(CONFIG_ADMIN)).thenReturn(true);
		when(sessionContext.getCallerPrincipal()).thenReturn(principal);
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.ALL);
		PermissionEntity perm = new PermissionEntity();
		perm.setValue(Permission.PERMISSION_DELEGATION.name());
		res.setPermission(perm);
		RestrictionEntity res2 = new RestrictionEntity();
		res2.setResourceGroup(resourceGroup);
		res2.setAction(Action.ALL);
		PermissionEntity perm2 = new PermissionEntity();
		perm2.setValue(Permission.RESOURCE_PROPERTY_DECRYPT.name());
		res2.setPermission(perm2);
		myRoles = new HashMap<>();
		permissionService.rolesWithRestrictions = myRoles;
		when(permissionRepository.getUserWithRestrictions(anyString())).thenReturn(Arrays.asList(res, res2));

		// when
		boolean result = permissionService.hasPermissionToDelegatePermission(Permission.RESOURCE_PROPERTY_DECRYPT, null, null, null, Action.CREATE);

		// then
		Assert.assertFalse(result);
	}

	@Test
	public void shouldReturnFalseIfCallerHasDelegationPermissionButHisSimilarRestrictionIsRestrictedToAnExplicitResourceTypeAndTheOneHeWantsToDelegateIsFromAnother() {
		// given
		ResourceTypeEntity resourceType = new ResourceTypeEntityBuilder().id(7).build();
		ResourceGroupEntity resourceGroup = new ResourceGroupEntity();
		resourceGroup.setId(23);
		resourceGroup.setResourceType(resourceType);
		ResourceTypeEntity anotherResourceType = new ResourceTypeEntityBuilder().id(9).build();
		when(sessionContext.isCallerInRole(CONFIG_ADMIN)).thenReturn(true);
		when(sessionContext.getCallerPrincipal()).thenReturn(principal);
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.ALL);
		PermissionEntity perm = new PermissionEntity();
		perm.setValue(Permission.PERMISSION_DELEGATION.name());
		res.setPermission(perm);
		RestrictionEntity res2 = new RestrictionEntity();
		res2.setResourceType(anotherResourceType);
		res2.setAction(Action.ALL);
		PermissionEntity perm2 = new PermissionEntity();
		perm2.setValue(Permission.RESOURCE_PROPERTY_DECRYPT.name());
		res2.setPermission(perm2);
		myRoles = new HashMap<>();
		permissionService.rolesWithRestrictions = myRoles;
		when(permissionRepository.getUserWithRestrictions(anyString())).thenReturn(Arrays.asList(res, res2));

		// when
		boolean result = permissionService.hasPermissionToDelegatePermission(Permission.RESOURCE_PROPERTY_DECRYPT, resourceGroup, null, null, Action.CREATE);

		// then
		Assert.assertFalse(result);
	}

	@Test
	public void shouldReturnFalseIfCallerHasDelegationPermissionButHisSimilarRestrictionIsRestrictedToAnExplicitContextAndTheOneHeWantsToDelegateIsNot() {
		// given
		ResourceTypeEntity resourceType = new ResourceTypeEntityBuilder().id(7).build();
		ResourceGroupEntity resourceGroup = new ResourceGroupEntity();
		resourceGroup.setId(23);
		resourceGroup.setResourceType(resourceType);
		when(sessionContext.isCallerInRole(CONFIG_ADMIN)).thenReturn(true);
		when(sessionContext.getCallerPrincipal()).thenReturn(principal);
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.ALL);
		PermissionEntity perm = new PermissionEntity();
		perm.setValue(Permission.PERMISSION_DELEGATION.name());
		res.setPermission(perm);
		RestrictionEntity res2 = new RestrictionEntity();
		res2.setResourceGroup(resourceGroup);
		res2.setAction(Action.ALL);
		res2.setContext(envC);
		PermissionEntity perm2 = new PermissionEntity();
		perm2.setValue(Permission.RESOURCE_PROPERTY_DECRYPT.name());
		res2.setPermission(perm2);
		myRoles = new HashMap<>();
		permissionService.rolesWithRestrictions = myRoles;
		when(permissionRepository.getUserWithRestrictions(anyString())).thenReturn(Arrays.asList(res, res2));

		// when
		boolean result = permissionService.hasPermissionToDelegatePermission(Permission.RESOURCE_PROPERTY_DECRYPT, resourceGroup, null, null, Action.CREATE);

		// then
		Assert.assertFalse(result);
	}

	@Test
	public void shouldReturnFalseIfCallerHasDelegationPermissionButHisSimilarRestrictionIsRestrictedToAnExplicitResourceTypeAndTheOneHeWantsToDelegateIsNot() {
		// given
		ResourceTypeEntity resourceType = new ResourceTypeEntityBuilder().id(7).build();
		when(sessionContext.isCallerInRole(CONFIG_ADMIN)).thenReturn(true);
		when(sessionContext.getCallerPrincipal()).thenReturn(principal);
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.ALL);
		PermissionEntity perm = new PermissionEntity();
		perm.setValue(Permission.PERMISSION_DELEGATION.name());
		res.setPermission(perm);
		RestrictionEntity res2 = new RestrictionEntity();
		res2.setResourceType(resourceType);
		res2.setAction(Action.ALL);
		res2.setContext(envC);
		PermissionEntity perm2 = new PermissionEntity();
		perm2.setValue(Permission.RESOURCE_PROPERTY_DECRYPT.name());
		res2.setPermission(perm2);
		myRoles = new HashMap<>();
		permissionService.rolesWithRestrictions = myRoles;
		when(permissionRepository.getUserWithRestrictions(anyString())).thenReturn(Arrays.asList(res, res2));

		// when
		boolean result = permissionService.hasPermissionToDelegatePermission(Permission.RESOURCE_PROPERTY_DECRYPT, null, null, null, Action.CREATE);

		// then
		Assert.assertFalse(result);
	}

	@Test
	public void shouldReturnTrueIfCallerHasDelegationPermissionAndHisSimilarRestrictionIsRestrictedToAnExplicitResourceGroupAndAnExplicitContextWhichIsTheParentOfTheOneHeWantsToDelegate() {
		// given
		ResourceTypeEntity resourceType = new ResourceTypeEntityBuilder().id(7).build();
		ResourceGroupEntity resourceGroup = new ResourceGroupEntity();
		resourceGroup.setId(23);
		resourceGroup.setResourceType(resourceType);
		when(sessionContext.isCallerInRole(CONFIG_ADMIN)).thenReturn(true);
		when(sessionContext.getCallerPrincipal()).thenReturn(principal);
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.ALL);
		PermissionEntity perm = new PermissionEntity();
		perm.setValue(Permission.PERMISSION_DELEGATION.name());
		res.setPermission(perm);
		RestrictionEntity res2 = new RestrictionEntity();
		res2.setResourceGroup(resourceGroup);
		res2.setAction(Action.ALL);
		res2.setContext(test);
		PermissionEntity perm2 = new PermissionEntity();
		perm2.setValue(Permission.RESOURCE_PROPERTY_DECRYPT.name());
		res2.setPermission(perm2);
		myRoles = new HashMap<>();
		permissionService.rolesWithRestrictions = myRoles;
		when(permissionRepository.getUserWithRestrictions(anyString())).thenReturn(Arrays.asList(res, res2));

		// when
		boolean result = permissionService.hasPermissionToDelegatePermission(Permission.RESOURCE_PROPERTY_DECRYPT, resourceGroup, null, envC, Action.CREATE);

		// then
		Assert.assertTrue(result);
	}

	@Test
	public void shouldReturnTrueIfCallerHasDelegationPermissionAndHisSimilarRestrictionIsRestrictedToAnExplicitContextWhichIsTheParentOfTheOneHeWantsToDelegate() {
		// given
		ResourceTypeEntity resourceType = new ResourceTypeEntityBuilder().id(7).build();
		ResourceGroupEntity resourceGroup = new ResourceGroupEntity();
		resourceGroup.setId(23);
		resourceGroup.setResourceType(resourceType);
		when(sessionContext.isCallerInRole(CONFIG_ADMIN)).thenReturn(true);
		when(sessionContext.getCallerPrincipal()).thenReturn(principal);
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.ALL);
		PermissionEntity perm = new PermissionEntity();
		perm.setValue(Permission.PERMISSION_DELEGATION.name());
		res.setPermission(perm);
		RestrictionEntity res2 = new RestrictionEntity();
		res2.setAction(Action.ALL);
		res2.setContext(test);
		PermissionEntity perm2 = new PermissionEntity();
		perm2.setValue(Permission.RESOURCE_PROPERTY_DECRYPT.name());
		res2.setPermission(perm2);
		myRoles = new HashMap<>();
		permissionService.rolesWithRestrictions = myRoles;
		when(permissionRepository.getUserWithRestrictions(anyString())).thenReturn(Arrays.asList(res, res2));

		// when
		boolean result = permissionService.hasPermissionToDelegatePermission(Permission.RESOURCE_PROPERTY_DECRYPT, null, null, envC, Action.CREATE);

		// then
		Assert.assertTrue(result);
	}

	@Test
	public void shouldReturnTrueIfCallerHasDelegationPermissionAndSimilarRestriction() {
		// given
		when(sessionContext.isCallerInRole(CONFIG_ADMIN)).thenReturn(true);
		when(sessionContext.getCallerPrincipal()).thenReturn(principal);
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.ALL);
		PermissionEntity perm = new PermissionEntity();
		perm.setValue(Permission.PERMISSION_DELEGATION.name());
		res.setPermission(perm);
		RestrictionEntity res2 = new RestrictionEntity();
		res2.setAction(Action.ALL);
		PermissionEntity perm2 = new PermissionEntity();
		perm2.setValue(Permission.RESOURCE_PROPERTY_DECRYPT.name());
		res2.setPermission(perm2);
		myRoles = new HashMap<>();
		permissionService.rolesWithRestrictions = myRoles;
		when(permissionRepository.getUserWithRestrictions(anyString())).thenReturn(Arrays.asList(res, res2));

		// when
		boolean result = permissionService.hasPermissionToDelegatePermission(Permission.RESOURCE_PROPERTY_DECRYPT, null, null, null, Action.CREATE);

		// then
		Assert.assertTrue(result);
	}

	@Test
	public void shouldReturnTrueIfASameRoleRestrictionAlreadyExists() {
		// given
		RoleEntity role = new RoleEntity();
		role.setName(CONFIG_ADMIN);
		PermissionEntity permission = new PermissionEntity();
		permission.setValue(Permission.RESOURCE.name());

		RestrictionEntity restriction = new RestrictionEntity();
		restriction.setRole(role);
		restriction.setAction(Action.UPDATE);
		restriction.setContext(envC);
		restriction.setPermission(permission);
		myRoles = new HashMap<>();
		myRoles.put(role.getName(), Arrays.asList(new RestrictionDTOBuilder().buildRestrictionDTO(Permission.RESOURCE, restriction)));
		permissionService.rolesWithRestrictions = myRoles;

		// when
		boolean exists = permissionService.identicalOrMoreGeneralRestrictionExists(restriction);

		// then
		Assert.assertTrue(exists);
	}

	@Test
	public void shouldReturnTrueIfASameUserRestrictionAlreadyExists() {
		// given
		UserRestrictionEntity userRestrictionEntity = new UserRestrictionEntity();
		userRestrictionEntity.setName("tester");
		PermissionEntity permission = new PermissionEntity();
		permission.setValue(Permission.RESOURCE.name());

		RestrictionEntity restriction = new RestrictionEntity();
		restriction.setUser(userRestrictionEntity);
		restriction.setAction(Action.UPDATE);
		restriction.setContext(envC);
		restriction.setPermission(permission);

		myRoles = new HashMap<>();
		permissionService.rolesWithRestrictions = myRoles;
		when(permissionRepository.getUserWithRestrictions("tester")).thenReturn(Arrays.asList(restriction));

		// when
		boolean exists = permissionService.identicalOrMoreGeneralRestrictionExists(restriction);

		// then
		Assert.assertTrue(exists);
	}

	@Test
	public void shouldReturnTrueIfASimilarRoleRestrictionAlreadyExists() {
		// given
		RoleEntity role = new RoleEntity();
		role.setName(CONFIG_ADMIN);
		PermissionEntity permission = new PermissionEntity();
		permission.setValue(Permission.RESOURCE.name());

		ResourceTypeEntity resourceType = new ResourceTypeEntityBuilder().id(7).build();
		ResourceGroupEntity resourceGroup = new ResourceGroupEntity();
		resourceGroup.setId(23);
		resourceGroup.setResourceType(resourceType);

		RestrictionEntity existingRestriction = new RestrictionEntity();
		existingRestriction.setRole(role);
		existingRestriction.setAction(Action.UPDATE);
		existingRestriction.setContext(envC);
		existingRestriction.setPermission(permission);

		RestrictionEntity newRestriction = new RestrictionEntity();
		newRestriction.setRole(role);
		newRestriction.setAction(Action.UPDATE);
		newRestriction.setContext(envC);
		newRestriction.setPermission(permission);
		newRestriction.setResourceGroup(resourceGroup);

		myRoles = new HashMap<>();
		myRoles.put(role.getName(), Arrays.asList(new RestrictionDTOBuilder().buildRestrictionDTO(Permission.RESOURCE, existingRestriction)));
		permissionService.rolesWithRestrictions = myRoles;

		// when
		boolean exists = permissionService.identicalOrMoreGeneralRestrictionExists(newRestriction);

		// then
		Assert.assertTrue(exists);
	}

	@Test
	public void shouldReturnFalseIfASimilarButMoreResourceGroupRestrictedRoleRestrictionExists() {
		// given
		RoleEntity role = new RoleEntity();
		role.setName(CONFIG_ADMIN);
		PermissionEntity permission = new PermissionEntity();
		permission.setValue(Permission.RESOURCE.name());

		ResourceTypeEntity resourceType = new ResourceTypeEntityBuilder().id(7).build();
		ResourceGroupEntity resourceGroup = new ResourceGroupEntity();
		resourceGroup.setId(23);
		resourceGroup.setResourceType(resourceType);

		RestrictionEntity existingRestriction = new RestrictionEntity();
		existingRestriction.setRole(role);
		existingRestriction.setAction(Action.UPDATE);
		existingRestriction.setContext(envC);
		existingRestriction.setPermission(permission);
		existingRestriction.setResourceGroup(resourceGroup);

		RestrictionEntity newRestriction = new RestrictionEntity();
		newRestriction.setRole(role);
		newRestriction.setAction(Action.UPDATE);
		newRestriction.setContext(envC);
		newRestriction.setPermission(permission);

		myRoles = new HashMap<>();
		myRoles.put(role.getName(), Arrays.asList(new RestrictionDTOBuilder().buildRestrictionDTO(Permission.RESOURCE, existingRestriction)));
		permissionService.rolesWithRestrictions = myRoles;

		// when
		boolean exists = permissionService.identicalOrMoreGeneralRestrictionExists(newRestriction);

		// then
		Assert.assertFalse(exists);
	}

	@Test
	public void shouldReturnFalseIfUpdatingExistingRoleRestriction() {
		// given
		RoleEntity role = new RoleEntity();
		role.setName(CONFIG_ADMIN);
		PermissionEntity permission = new PermissionEntity();
		permission.setValue(Permission.RESOURCE.name());

		ResourceTypeEntity resourceType = new ResourceTypeEntityBuilder().id(7).build();
		ResourceGroupEntity resourceGroup = new ResourceGroupEntity();
		resourceGroup.setId(23);
		resourceGroup.setResourceType(resourceType);

		RestrictionEntity existingRestriction = new RestrictionEntity();
		existingRestriction.setRole(role);
		existingRestriction.setAction(Action.ALL);
		existingRestriction.setContext(test);
		existingRestriction.setPermission(permission);
		existingRestriction.setId(1);

		RestrictionEntity newRestriction = new RestrictionEntity();
		newRestriction.setRole(role);
		newRestriction.setAction(Action.UPDATE);
		newRestriction.setContext(envC);
		newRestriction.setPermission(permission);
		newRestriction.setResourceGroup(resourceGroup);
		newRestriction.setId(1);

		myRoles = new HashMap<>();
		myRoles.put(role.getName(), Arrays.asList(new RestrictionDTOBuilder().buildRestrictionDTO(Permission.RESOURCE, existingRestriction)));
		permissionService.rolesWithRestrictions = myRoles;

		// when
		boolean exists = permissionService.identicalOrMoreGeneralRestrictionExists(newRestriction);

		// then
		Assert.assertFalse(exists);
	}

	@Test
	public void shouldReturnTrueIfUpdatingExistingRoleRestrictionButAnotherMoreGeneralRestrictionExists() {
		// given
		RoleEntity role = new RoleEntity();
		role.setName(CONFIG_ADMIN);
		PermissionEntity permission = new PermissionEntity();
		permission.setValue(Permission.RESOURCE.name());

		ResourceTypeEntity resourceType = new ResourceTypeEntityBuilder().id(7).build();
		ResourceGroupEntity resourceGroup = new ResourceGroupEntity();
		resourceGroup.setId(23);
		resourceGroup.setResourceType(resourceType);

		RestrictionEntity existingRestriction = new RestrictionEntity();
		existingRestriction.setRole(role);
		existingRestriction.setAction(Action.ALL);
		existingRestriction.setContext(test);
		existingRestriction.setPermission(permission);
		existingRestriction.setId(1);

		RestrictionEntity anotherExistingRestriction = new RestrictionEntity();
		anotherExistingRestriction.setRole(role);
		anotherExistingRestriction.setAction(Action.ALL);
		anotherExistingRestriction.setContext(envC);
		anotherExistingRestriction.setPermission(permission);
		anotherExistingRestriction.setId(2);

		RestrictionEntity newRestriction = new RestrictionEntity();
		newRestriction.setRole(role);
		newRestriction.setAction(Action.UPDATE);
		newRestriction.setContext(envC);
		newRestriction.setPermission(permission);
		newRestriction.setResourceGroup(resourceGroup);
		newRestriction.setId(1);

		myRoles = new HashMap<>();
		myRoles.put(role.getName(), Arrays.asList(new RestrictionDTOBuilder().buildRestrictionDTO(Permission.RESOURCE, existingRestriction),
				new RestrictionDTOBuilder().buildRestrictionDTO(Permission.RESOURCE, anotherExistingRestriction)));
		permissionService.rolesWithRestrictions = myRoles;

		// when
		boolean exists = permissionService.identicalOrMoreGeneralRestrictionExists(newRestriction);

		// then
		Assert.assertTrue(exists);
	}

	@Test
	public void shouldReturnFalseIfASimilarButMoreResourceGroupRestrictedUserRestrictionExists() {
		// given
		UserRestrictionEntity userRestrictionEntity = new UserRestrictionEntity();
		userRestrictionEntity.setName("tester");
		PermissionEntity permission = new PermissionEntity();
		permission.setValue(Permission.RESOURCE.name());

		ResourceTypeEntity resourceType = new ResourceTypeEntityBuilder().id(7).build();
		ResourceGroupEntity resourceGroup = new ResourceGroupEntity();
		resourceGroup.setId(23);
		resourceGroup.setResourceType(resourceType);

		RestrictionEntity existingRestriction = new RestrictionEntity();
		existingRestriction.setUser(userRestrictionEntity);
		existingRestriction.setAction(Action.UPDATE);
		existingRestriction.setContext(envC);
		existingRestriction.setPermission(permission);
		existingRestriction.setResourceGroup(resourceGroup);

		RestrictionEntity newRestriction = new RestrictionEntity();
		newRestriction.setUser(userRestrictionEntity);
		newRestriction.setAction(Action.UPDATE);
		newRestriction.setContext(envC);
		newRestriction.setPermission(permission);

		myRoles = new HashMap<>();
		permissionService.rolesWithRestrictions = myRoles;
		when(permissionRepository.getUserWithRestrictions("tester")).thenReturn(Arrays.asList(existingRestriction));

		// when
		boolean exists = permissionService.identicalOrMoreGeneralRestrictionExists(newRestriction);

		// then
		Assert.assertFalse(exists);
	}

	@Test
	public void shouldReturnFalseIfASimilarButMoreResourceTypeRestrictedUserRestrictionExists() {
		// given
		UserRestrictionEntity userRestrictionEntity = new UserRestrictionEntity();
		userRestrictionEntity.setName("tester");
		PermissionEntity permission = new PermissionEntity();
		permission.setValue(Permission.RESOURCE.name());

		ResourceTypeEntity resourceType = new ResourceTypeEntityBuilder().id(7).build();

		RestrictionEntity existingRestriction = new RestrictionEntity();
		existingRestriction.setUser(userRestrictionEntity);
		existingRestriction.setAction(Action.UPDATE);
		existingRestriction.setContext(envC);
		existingRestriction.setPermission(permission);
		existingRestriction.setResourceType(resourceType);

		RestrictionEntity newRestriction = new RestrictionEntity();
		newRestriction.setUser(userRestrictionEntity);
		newRestriction.setAction(Action.UPDATE);
		newRestriction.setContext(envC);
		newRestriction.setPermission(permission);

		myRoles = new HashMap<>();
		permissionService.rolesWithRestrictions = myRoles;
		when(permissionRepository.getUserWithRestrictions("tester")).thenReturn(Arrays.asList(existingRestriction));

		// when
		boolean exists = permissionService.identicalOrMoreGeneralRestrictionExists(newRestriction);

		// then
		Assert.assertFalse(exists);
	}

	@Test
	public void shouldReturnTrueIfASimilarButNotResourceTypeRestrictedUserRestrictionExists() {
		// given
		UserRestrictionEntity userRestrictionEntity = new UserRestrictionEntity();
		userRestrictionEntity.setName("tester");
		PermissionEntity permission = new PermissionEntity();
		permission.setValue(Permission.RESOURCE.name());

		ResourceTypeEntity resourceType = new ResourceTypeEntityBuilder().id(7).build();

		RestrictionEntity existingRestriction = new RestrictionEntity();
		existingRestriction.setUser(userRestrictionEntity);
		existingRestriction.setAction(Action.UPDATE);
		existingRestriction.setContext(envC);
		existingRestriction.setPermission(permission);

		RestrictionEntity newRestriction = new RestrictionEntity();
		newRestriction.setUser(userRestrictionEntity);
		newRestriction.setAction(Action.UPDATE);
		newRestriction.setContext(envC);
		newRestriction.setPermission(permission);
		newRestriction.setResourceType(resourceType);

		myRoles = new HashMap<>();
		permissionService.rolesWithRestrictions = myRoles;
		when(permissionRepository.getUserWithRestrictions("tester")).thenReturn(Arrays.asList(existingRestriction));

		// when
		boolean exists = permissionService.identicalOrMoreGeneralRestrictionExists(newRestriction);

		// then
		Assert.assertTrue(exists);
	}

	@Test
	public void shouldReturnFalseIfASimilarButMoreContextRestrictedUserRestrictionExists() {
		// given
		UserRestrictionEntity userRestrictionEntity = new UserRestrictionEntity();
		userRestrictionEntity.setName("tester");
		PermissionEntity permission = new PermissionEntity();
		permission.setValue(Permission.RESOURCE.name());

		RestrictionEntity existingRestriction = new RestrictionEntity();
		existingRestriction.setUser(userRestrictionEntity);
		existingRestriction.setAction(Action.UPDATE);
		existingRestriction.setContext(envC);
		existingRestriction.setPermission(permission);

		RestrictionEntity newRestriction = new RestrictionEntity();
		newRestriction.setUser(userRestrictionEntity);
		newRestriction.setAction(Action.UPDATE);
		newRestriction.setContext(test);
		newRestriction.setPermission(permission);

		myRoles = new HashMap<>();
		permissionService.rolesWithRestrictions = myRoles;
		when(permissionRepository.getUserWithRestrictions("tester")).thenReturn(Arrays.asList(existingRestriction));

		// when
		boolean exists = permissionService.identicalOrMoreGeneralRestrictionExists(newRestriction);

		// then
		Assert.assertFalse(exists);
	}

	@Test
	public void shouldReturnTrueIfASimilarButLessContextRestrictedUserRestrictionExists() {
		// given
		UserRestrictionEntity userRestrictionEntity = new UserRestrictionEntity();
		userRestrictionEntity.setName("tester");
		PermissionEntity permission = new PermissionEntity();
		permission.setValue(Permission.RESOURCE.name());

		RestrictionEntity existingRestriction = new RestrictionEntity();
		existingRestriction.setUser(userRestrictionEntity);
		existingRestriction.setAction(Action.UPDATE);
		existingRestriction.setContext(test);
		existingRestriction.setPermission(permission);

		RestrictionEntity newRestriction = new RestrictionEntity();
		newRestriction.setUser(userRestrictionEntity);
		newRestriction.setAction(Action.UPDATE);
		newRestriction.setContext(envC);
		newRestriction.setPermission(permission);

		myRoles = new HashMap<>();
		permissionService.rolesWithRestrictions = myRoles;
		when(permissionRepository.getUserWithRestrictions("tester")).thenReturn(Arrays.asList(existingRestriction));

		// when
		boolean exists = permissionService.identicalOrMoreGeneralRestrictionExists(newRestriction);

		// then
		Assert.assertTrue(exists);
	}

	@Test
	public void shouldReturnTrueIfASimilarButNotContextRestrictedUserRestrictionExists() {
		// given
		UserRestrictionEntity userRestrictionEntity = new UserRestrictionEntity();
		userRestrictionEntity.setName("tester");
		PermissionEntity permission = new PermissionEntity();
		permission.setValue(Permission.RESOURCE.name());

		RestrictionEntity existingRestriction = new RestrictionEntity();
		existingRestriction.setUser(userRestrictionEntity);
		existingRestriction.setAction(Action.UPDATE);
		existingRestriction.setPermission(permission);

		RestrictionEntity newRestriction = new RestrictionEntity();
		newRestriction.setUser(userRestrictionEntity);
		newRestriction.setAction(Action.UPDATE);
		newRestriction.setPermission(permission);
		newRestriction.setContext(envC);

		myRoles = new HashMap<>();
		permissionService.rolesWithRestrictions = myRoles;
		when(permissionRepository.getUserWithRestrictions("tester")).thenReturn(Arrays.asList(existingRestriction));

		// when
		boolean exists = permissionService.identicalOrMoreGeneralRestrictionExists(newRestriction);

		// then
		Assert.assertTrue(exists);
	}

	@Test
	public void shouldReturnTrueIfASimilarButNotActionRestrictedUserRestrictionExists() {
		// given
		UserRestrictionEntity userRestrictionEntity = new UserRestrictionEntity();
		userRestrictionEntity.setName("tester");
		PermissionEntity permission = new PermissionEntity();
		permission.setValue(Permission.RESOURCE.name());

		RestrictionEntity existingRestriction = new RestrictionEntity();
		existingRestriction.setUser(userRestrictionEntity);
		existingRestriction.setAction(Action.ALL);
		existingRestriction.setPermission(permission);

		RestrictionEntity newRestriction = new RestrictionEntity();
		newRestriction.setUser(userRestrictionEntity);
		newRestriction.setAction(Action.READ);
		newRestriction.setPermission(permission);

		myRoles = new HashMap<>();
		permissionService.rolesWithRestrictions = myRoles;
		when(permissionRepository.getUserWithRestrictions("tester")).thenReturn(Arrays.asList(existingRestriction));

		// when
		boolean exists = permissionService.identicalOrMoreGeneralRestrictionExists(newRestriction);

		// then
		Assert.assertTrue(exists);
	}

	@Test
	public void shouldReturnFalseIfASimilarButActionRestrictedUserRestrictionExists() {
		// given
		UserRestrictionEntity userRestrictionEntity = new UserRestrictionEntity();
		userRestrictionEntity.setName("tester");
		PermissionEntity permission = new PermissionEntity();
		permission.setValue(Permission.RESOURCE.name());

		RestrictionEntity existingRestriction = new RestrictionEntity();
		existingRestriction.setUser(userRestrictionEntity);
		existingRestriction.setAction(Action.UPDATE);
		existingRestriction.setPermission(permission);

		RestrictionEntity newRestriction = new RestrictionEntity();
		newRestriction.setUser(userRestrictionEntity);
		newRestriction.setAction(Action.ALL);
		newRestriction.setPermission(permission);

		myRoles = new HashMap<>();
		permissionService.rolesWithRestrictions = myRoles;
		when(permissionRepository.getUserWithRestrictions("tester")).thenReturn(Arrays.asList(existingRestriction));

		// when
		boolean exists = permissionService.identicalOrMoreGeneralRestrictionExists(newRestriction);

		// then
		Assert.assertFalse(exists);
	}

	@Test
	public void shouldReturnFalseIfASimilarButResourceTypePermissionRestrictedUserRestrictionExists() {
		// given
		UserRestrictionEntity userRestrictionEntity = new UserRestrictionEntity();
		userRestrictionEntity.setName("tester");
		PermissionEntity permission = new PermissionEntity();
		permission.setValue(Permission.RESOURCE.name());

		RestrictionEntity existingRestriction = new RestrictionEntity();
		existingRestriction.setUser(userRestrictionEntity);
		existingRestriction.setAction(Action.CREATE);
		existingRestriction.setResourceTypePermission(ResourceTypePermission.NON_DEFAULT_ONLY);
		existingRestriction.setPermission(permission);

		RestrictionEntity anotherExistingRestriction = new RestrictionEntity();
		anotherExistingRestriction.setUser(userRestrictionEntity);
		anotherExistingRestriction.setAction(Action.ALL);
		anotherExistingRestriction.setResourceTypePermission(ResourceTypePermission.DEFAULT_ONLY);
		anotherExistingRestriction.setPermission(permission);

		RestrictionEntity newRestriction = new RestrictionEntity();
		newRestriction.setUser(userRestrictionEntity);
		newRestriction.setAction(Action.CREATE);
		newRestriction.setResourceTypePermission(ResourceTypePermission.ANY);
		newRestriction.setPermission(permission);

		myRoles = new HashMap<>();
		permissionService.rolesWithRestrictions = myRoles;
		when(permissionRepository.getUserWithRestrictions("tester")).thenReturn(Arrays.asList(existingRestriction, anotherExistingRestriction));

		// when
		boolean exists = permissionService.identicalOrMoreGeneralRestrictionExists(newRestriction);

		// then
		Assert.assertFalse(exists);
	}

	@Test
	public void shouldReturnTrueIfASimilarButNotResourceTypePermissionRestrictedUserRestrictionExists() {
		// given
		UserRestrictionEntity userRestrictionEntity = new UserRestrictionEntity();
		userRestrictionEntity.setName("tester");
		PermissionEntity permission = new PermissionEntity();
		permission.setValue(Permission.RESOURCE.name());

		RestrictionEntity existingRestriction = new RestrictionEntity();
		existingRestriction.setUser(userRestrictionEntity);
		existingRestriction.setAction(Action.ALL);
		existingRestriction.setResourceTypePermission(ResourceTypePermission.ANY);
		existingRestriction.setPermission(permission);

		RestrictionEntity anotherExistingRestriction = new RestrictionEntity();
		anotherExistingRestriction.setUser(userRestrictionEntity);
		anotherExistingRestriction.setAction(Action.ALL);
		anotherExistingRestriction.setResourceTypePermission(ResourceTypePermission.DEFAULT_ONLY);
		anotherExistingRestriction.setPermission(permission);

		RestrictionEntity newRestriction = new RestrictionEntity();
		newRestriction.setUser(userRestrictionEntity);
		newRestriction.setAction(Action.ALL);
		newRestriction.setResourceTypePermission(ResourceTypePermission.NON_DEFAULT_ONLY);
		newRestriction.setPermission(permission);

		myRoles = new HashMap<>();
		permissionService.rolesWithRestrictions = myRoles;
		when(permissionRepository.getUserWithRestrictions("tester")).thenReturn(Arrays.asList(existingRestriction, anotherExistingRestriction));

		// when
		boolean exists = permissionService.identicalOrMoreGeneralRestrictionExists(newRestriction);

		// then
		Assert.assertTrue(exists);
	}

	@Test
	public void shouldReturnTrueIfCallerHasAnIdenticalPermissionGrantedByItsRole() {
		// given
		RoleEntity role = new RoleEntity();
		role.setName(CONFIG_ADMIN);

		UserRestrictionEntity userRestrictionEntity = new UserRestrictionEntity();
		userRestrictionEntity.setName("tester");

		PermissionEntity permission = new PermissionEntity();
		permission.setValue(Permission.RESOURCE.name());

		ResourceTypeEntity resourceType = new ResourceTypeEntityBuilder().id(7).build();
		ResourceGroupEntity resourceGroup = new ResourceGroupEntity();
		resourceGroup.setId(23);
		resourceGroup.setResourceType(resourceType);

		RestrictionEntity existingUserRestriction = new RestrictionEntity();
		existingUserRestriction.setUser(userRestrictionEntity);
		existingUserRestriction.setAction(Action.DELETE);
		existingUserRestriction.setContext(test);
		existingUserRestriction.setPermission(permission);
		existingUserRestriction.setId(1);

		RestrictionEntity existingRoleRestriction = new RestrictionEntity();
		existingRoleRestriction.setRole(role);
		existingRoleRestriction.setAction(Action.UPDATE);
		existingRoleRestriction.setContext(envC);
		existingRoleRestriction.setPermission(permission);
		existingRoleRestriction.setId(2);

		RestrictionEntity newRestriction = new RestrictionEntity();
		newRestriction.setUser(userRestrictionEntity);
		newRestriction.setAction(Action.UPDATE);
		newRestriction.setContext(envC);
		newRestriction.setPermission(permission);
		newRestriction.setResourceGroup(resourceGroup);

		when(permissionService.sessionContext.isCallerInRole("config_admin")).thenReturn(true);

		myRoles = new HashMap<>();
		myRoles.put(role.getName(), Arrays.asList(new RestrictionDTOBuilder().buildRestrictionDTO(Permission.RESOURCE, existingRoleRestriction)));
		permissionService.rolesWithRestrictions = myRoles;

		when(permissionRepository.getUserWithRestrictions(principal.getName())).thenReturn(Collections.singletonList(existingUserRestriction));
		when(sessionContext.getCallerPrincipal()).thenReturn(principal);

		// when
		boolean exists = permissionService.callerHasIdenticalOrMoreGeneralRestriction(newRestriction);

		// then
		Assert.assertTrue(exists);
	}

	@Test
	public void shouldReturnTrueIfCallerHasAMorePowerfulPermissionGrantedByItsRole() {
		// given
		RoleEntity role = new RoleEntity();
		role.setName(CONFIG_ADMIN);

		UserRestrictionEntity userRestrictionEntity = new UserRestrictionEntity();
		userRestrictionEntity.setName("tester");

		PermissionEntity permission = new PermissionEntity();
		permission.setValue(Permission.RESOURCE.name());

		ResourceTypeEntity resourceType = new ResourceTypeEntityBuilder().id(7).build();
		ResourceGroupEntity resourceGroup = new ResourceGroupEntity();
		resourceGroup.setId(23);
		resourceGroup.setResourceType(resourceType);

		RestrictionEntity existingUserRestriction = new RestrictionEntity();
		existingUserRestriction.setUser(userRestrictionEntity);
		existingUserRestriction.setAction(Action.DELETE);
		existingUserRestriction.setContext(test);
		existingUserRestriction.setPermission(permission);
		existingUserRestriction.setId(1);

		RestrictionEntity existingRoleRestriction = new RestrictionEntity();
		existingRoleRestriction.setRole(role);
		existingRoleRestriction.setAction(Action.ALL);
		existingRoleRestriction.setContext(test);
		existingRoleRestriction.setPermission(permission);
		existingRoleRestriction.setId(2);

		RestrictionEntity newRestriction = new RestrictionEntity();
		newRestriction.setUser(userRestrictionEntity);
		newRestriction.setAction(Action.UPDATE);
		newRestriction.setContext(envC);
		newRestriction.setPermission(permission);
		newRestriction.setResourceGroup(resourceGroup);

		when(permissionService.sessionContext.isCallerInRole("config_admin")).thenReturn(true);

		myRoles = new HashMap<>();
		myRoles.put(role.getName(), Arrays.asList(new RestrictionDTOBuilder().buildRestrictionDTO(Permission.RESOURCE, existingRoleRestriction)));
		permissionService.rolesWithRestrictions = myRoles;

		when(permissionRepository.getUserWithRestrictions(principal.getName())).thenReturn(Collections.singletonList(existingUserRestriction));
		when(sessionContext.getCallerPrincipal()).thenReturn(principal);

		// when
		boolean exists = permissionService.callerHasIdenticalOrMoreGeneralRestriction(newRestriction);

		// then
		Assert.assertTrue(exists);
	}

	@Test
	public void shouldReturnFalseIfCallerHasALessPowerfulPermissionGrantedByItsRole() {
		// given
		RoleEntity role = new RoleEntity();
		role.setName(CONFIG_ADMIN);

		UserRestrictionEntity userRestrictionEntity = new UserRestrictionEntity();
		userRestrictionEntity.setName("tester");

		PermissionEntity permission = new PermissionEntity();
		permission.setValue(Permission.RESOURCE.name());

		ResourceTypeEntity resourceType = new ResourceTypeEntityBuilder().id(7).build();
		ResourceGroupEntity resourceGroup = new ResourceGroupEntity();
		resourceGroup.setId(23);
		resourceGroup.setResourceType(resourceType);

		RestrictionEntity existingUserRestriction = new RestrictionEntity();
		existingUserRestriction.setUser(userRestrictionEntity);
		existingUserRestriction.setAction(Action.DELETE);
		existingUserRestriction.setContext(test);
		existingUserRestriction.setPermission(permission);
		existingUserRestriction.setId(1);

		RestrictionEntity existingRoleRestriction = new RestrictionEntity();
		existingRoleRestriction.setRole(role);
		existingRoleRestriction.setAction(Action.ALL);
		existingRoleRestriction.setContext(envC);
		existingRoleRestriction.setPermission(permission);
		existingRoleRestriction.setId(2);

		RestrictionEntity newRestriction = new RestrictionEntity();
		newRestriction.setUser(userRestrictionEntity);
		newRestriction.setAction(Action.ALL);
		newRestriction.setContext(test);
		newRestriction.setPermission(permission);
		newRestriction.setResourceGroup(resourceGroup);

		when(permissionService.sessionContext.isCallerInRole("config_admin")).thenReturn(true);

		myRoles = new HashMap<>();
		myRoles.put(role.getName(), Arrays.asList(new RestrictionDTOBuilder().buildRestrictionDTO(Permission.RESOURCE, existingRoleRestriction)));
		permissionService.rolesWithRestrictions = myRoles;

		when(permissionRepository.getUserWithRestrictions(principal.getName())).thenReturn(Collections.singletonList(existingUserRestriction));
		when(sessionContext.getCallerPrincipal()).thenReturn(principal);

		// when
		boolean exists = permissionService.callerHasIdenticalOrMoreGeneralRestriction(newRestriction);

		// then
		Assert.assertFalse(exists);
	}

}