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
	private ContextEntity parent;
	private ContextEntity envC;
	private ContextEntity envZ;
	private ResourceGroupEntity anotherResourceGroup;

	private Principal principal;

    @Before
	public void setUp(){
		permissionService = new PermissionService();
		sessionContext = Mockito.mock(SessionContext.class);
		permissionService.sessionContext = sessionContext;
		permissionRepository = Mockito.mock(PermissionRepository.class);
		permissionService.permissionRepository = permissionRepository;

		global = new ContextEntityBuilder().id(1).buildContextEntity("GLOBAL", null, new HashSet<ContextEntity>(), false);
		parent = new ContextEntityBuilder().id(5).buildContextEntity("TEST", global, new HashSet<ContextEntity>(), false);
		envC = new ContextEntityBuilder().id(10).buildContextEntity("C", parent, new HashSet<ContextEntity>(), false);
		envZ = new ContextEntityBuilder().id(11).buildContextEntity("Z", parent, new HashSet<ContextEntity>(), false);

		principal = new Principal() {
			@Override
			public String getName() {
				return "tester";
			}
		};

		// APPLICATION
		anotherResourceGroup = new ResourceGroupEntity();
		anotherResourceGroup.setId(321);
		anotherResourceGroup.setResourceType(new ResourceTypeEntityBuilder().id(1).parentResourceType(null).build());
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
        ResourceEntity app = resourceEntityBuilder.mockApplicationEntity("app", null, null);
		
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
        ResourceEntity app = resourceEntityBuilder.mockApplicationEntity("app", null, null);

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
		ResourceEntity app = resourceEntityBuilder.mockApplicationEntity("app", null, null);

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
		ResourceEntity app = resourceEntityBuilder.mockApplicationEntity("app", anotherResourceGroup, null);

		when(sessionContext.isCallerInRole(APP_DEVELOPER)).thenReturn(true);
		when(sessionContext.getCallerPrincipal()).thenReturn(principal);
		myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.UPDATE);
		res.setResourceGroup(anotherResourceGroup);
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
		ResourceEntity app = resourceEntityBuilder.mockApplicationEntity("app", null, null);

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
		ResourceEntity app = resourceEntityBuilder.mockApplicationEntity("app", null, null);

		when(sessionContext.isCallerInRole(APP_DEVELOPER)).thenReturn(true);
		when(sessionContext.getCallerPrincipal()).thenReturn(principal);
		myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.UPDATE);
		res.setResourceGroup(anotherResourceGroup);
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
    public void hasPermissionToDeleteResourceRelationWhenResourceIsRuntimeType(){
        // given
        ResourceEntity runtime = resourceEntityBuilder.mockRuntimeEntity("EAP6", null, null);

        when(sessionContext.isCallerInRole(SERVER_ADMIN)).thenReturn(true);
		when(sessionContext.getCallerPrincipal()).thenReturn(principal);
        myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.ALL);
		myRoles.put(SERVER_ADMIN, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.SELECT_RUNTIME, res)));
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
        boolean consumed = permissionService.hasPermissionToAddRelation(as, false, new ContextEntity());
        boolean provided = permissionService.hasPermissionToAddRelation(as, true, new ContextEntity());

        // then
        Assert.assertTrue(consumed);
        Assert.assertTrue(provided);
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
        boolean consumed = permissionService.hasPermissionToAddRelation(resourceWithoutResourceType, false, new ContextEntity());
        boolean provided = permissionService.hasPermissionToAddRelation(resourceWithoutResourceType, true, new ContextEntity());

        // then
        Assert.assertFalse(consumed);
        Assert.assertFalse(provided);
    }

    @Test
    public void hasPermissionToAddResourceRelationWhenUserIsServeAdminAndResourceIsInstanceOfApplicationServerResType() {
        // given
        ResourceEntity as = resourceEntityBuilder.mockAppServerEntity("as", null, null, null);

        when(sessionContext.isCallerInRole(SERVER_ADMIN)).thenReturn(true);
		when(sessionContext.getCallerPrincipal()).thenReturn(principal);
        myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.ALL);
		myRoles.put(SERVER_ADMIN, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.ADD_NODE_RELATION, res)));
        permissionService.rolesWithRestrictions = myRoles;

        // when
        boolean consumed = permissionService.hasPermissionToAddRelation(as, false, new ContextEntity());
        boolean provided = permissionService.hasPermissionToAddRelation(as, true, new ContextEntity());

        // then
        Assert.assertTrue(consumed);
        Assert.assertTrue(provided);
    }

    @Test
    public void hasNoPermissionToAddResourceRelationWhenUserIsServeAdminAndResourceIsInstanceOfApplicationResType() {
        // given
        ResourceEntity app = resourceEntityBuilder.mockApplicationEntity("app", null, null);

        when(sessionContext.isCallerInRole(SERVER_ADMIN)).thenReturn(true);
		when(sessionContext.getCallerPrincipal()).thenReturn(principal);
        myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.ALL);
		myRoles.put(SERVER_ADMIN, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.ADD_NODE_RELATION, res)));
        permissionService.rolesWithRestrictions = myRoles;

        // when
        boolean consumed = permissionService.hasPermissionToAddRelation(app, false, new ContextEntity());
        boolean provided = permissionService.hasPermissionToAddRelation(app, true, new ContextEntity());

        // then
        Assert.assertFalse(consumed);
        Assert.assertFalse(provided);
    }

    @Test
	public void hasPermissionToAddResourceRelationWhenUserHasPermissionToUpdateResourceAndResourceIsInstanceOfApplicationResType(){
		// given
        ResourceEntity app = resourceEntityBuilder.mockApplicationEntity("app", null, null);

		when(sessionContext.isCallerInRole(APP_DEVELOPER)).thenReturn(true);
		when(sessionContext.getCallerPrincipal()).thenReturn(principal);
		myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.ALL);
		RestrictionEntity upd = new RestrictionEntity();
		upd.setAction(Action.UPDATE);
		myRoles.put(APP_DEVELOPER, Arrays.asList(
				new RestrictionDTOBuilder().mockRestrictionDTO(Permission.ADD_AS_CONSUMED_RESOURCE, res),
				new RestrictionDTOBuilder().mockRestrictionDTO(Permission.RESOURCE, upd)));
		permissionService.rolesWithRestrictions = myRoles;

		// when
        boolean consumed = permissionService.hasPermissionToAddRelation(app, false, new ContextEntity());
        boolean provided = permissionService.hasPermissionToAddRelation(app, true, new ContextEntity());

		// then
		Assert.assertTrue(consumed);
        Assert.assertTrue(provided);
	}

    @Test
    public void hasNoPermissionToAddResourceRelationWhenUserUserHasNoPermissionToUpdateResourceAndResourceIsNotInstanceOfApplicationResType() {
        // given
        ResourceEntity as = resourceEntityBuilder.mockAppServerEntity("as", null, null, null);

        when(sessionContext.isCallerInRole(APP_DEVELOPER)).thenReturn(true);
		when(sessionContext.getCallerPrincipal()).thenReturn(principal);
        myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.ALL);
		myRoles.put(APP_DEVELOPER, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.ADD_AS_CONSUMED_RESOURCE, res)));
        permissionService.rolesWithRestrictions = myRoles;

        // when
        boolean consumed = permissionService.hasPermissionToAddRelation(as, false, new ContextEntity());
        boolean provided = permissionService.hasPermissionToAddRelation(as, true, new ContextEntity());

        // then
        Assert.assertFalse(consumed);
        Assert.assertFalse(provided);
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
        boolean result = permissionService.hasPermissionToModifyResourceTemplate(as, true);

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
        boolean result = permissionService.hasPermissionToModifyResourceTemplate(as, true);

        // then
        Assert.assertFalse(result);
    }

    @Test
    public void hasPermissionToTemplateModifyWhenUserHasPermissionToUpdateResourceAndIsResourceIsNotTestingMode() {
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
        boolean result = permissionService.hasPermissionToModifyResourceTemplate(as, false);

        // then
        Assert.assertTrue(result);
    }

	@Test
	public void hasPermissionToTemplateModifyWhenUserHasPermissionToUpdateTemplateResourceAndIsResourceIsNotTestingMode() {
		// given
		ResourceEntity as = resourceEntityBuilder.mockAppServerEntity("as", null, null, null);

		when(sessionContext.isCallerInRole(CONFIG_ADMIN)).thenReturn(true);
		when(sessionContext.getCallerPrincipal()).thenReturn(principal);
		myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.UPDATE);
		myRoles.put(CONFIG_ADMIN, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.TEMPLATE_RESOURCE, res)));
		permissionService.rolesWithRestrictions = myRoles;

		// when
		boolean result = permissionService.hasPermissionToModifyResourceTemplate(as, false);

		// then
		Assert.assertTrue(result);
	}

    @Test
    public void hasPermissionToTemplateModifyWhenUserHasPermissionToUpdateResourceTypeAndIsApplicationResTypeAndIsNotTestingMode() {
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
        boolean result = permissionService.hasPermissionToModifyResourceTypeTemplate(app, false);

        // then
        Assert.assertTrue(result);
    }

	@Test
	public void hasPermissionToTemplateModifyWhenUserHasPermissionToUpdateTemplateResourceTypeAndIsApplicationResTypeAndIsNotTestingMode() {
		// given
		ResourceTypeEntity app = ResourceTypeEntityBuilder.APPLICATION_TYPE;

		when(sessionContext.isCallerInRole(APP_DEVELOPER)).thenReturn(true);
		when(sessionContext.getCallerPrincipal()).thenReturn(principal);
		myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.UPDATE);
		res.setResourceTypePermission(ResourceTypePermission.ANY);
		myRoles.put(APP_DEVELOPER, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.TEMPLATE_RESOURCETYPE, res)));
		permissionService.rolesWithRestrictions = myRoles;

		// when
		boolean result = permissionService.hasPermissionToModifyResourceTypeTemplate(app, false);

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
        boolean result = permissionService.hasPermissionToModifyResourceTemplate(as, false);

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
		boolean result = permissionService.hasPermissionToDeploy();
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
		roleTestDeployer.getPermissions().add(permissionToDeploy);
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
		roleTestDeployer.getPermissions().add(permissionToDeploy);
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
		roleTestDeployer.getPermissions().add(permissionToDeploy);
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
		boolean resC = permissionService.hasPermissionForDeploymentOnContext(envC, anotherResourceGroup);
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
		roleTestDeployer.getPermissions().add(permissionToDeploy);
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
		boolean resC = permissionService.hasPermissionForDeploymentOnContext(envC, anotherResourceGroup);
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
		roleTestDeployer.getPermissions().add(permissionToDeploy);
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
		boolean resZ = permissionService.hasPermissionForDeploymentOnContext(envZ, anotherResourceGroup);
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
		roleTestDeployer.getPermissions().add(permissionToDeploy);
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
		boolean resZ = permissionService.hasPermissionForDeploymentOnContext(envZ, anotherResourceGroup);
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
		roleTestDeployer.getPermissions().add(permissionToDeploy);
		when(sessionContext.isCallerInRole(TEST_DEPLOYER)).thenReturn(true);
		when(sessionContext.getCallerPrincipal()).thenReturn(principal);
		myRoles = new HashMap<>();
		//assign restriction allowing all on parent environment
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.ALL);
		res.setResourceTypePermission(ResourceTypePermission.ANY);
		res.setContext(parent);
		res.setPermission(permissionToDeploy);
		myRoles.put(TEST_DEPLOYER, Arrays.asList(new RestrictionDTO(res)));
		permissionService.deployableRolesWithRestrictions = myRoles;
		when(permissionService.permissionRepository.isReloadDeployableRoleList()).thenReturn(false);

		// when
		boolean resC = permissionService.hasPermissionForDeploymentOnContext(envC, anotherResourceGroup);
		boolean resZ = permissionService.hasPermissionForDeploymentOnContext(envZ, anotherResourceGroup);

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
		roleTestDeployer.getPermissions().add(permissionToDeploy);
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
		boolean resGlobal = permissionService.hasPermissionForDeploymentOnContext(global, anotherResourceGroup);
		boolean resParent = permissionService.hasPermissionForDeploymentOnContext(parent, anotherResourceGroup);
		boolean resC = permissionService.hasPermissionForDeploymentOnContext(envC, anotherResourceGroup);
		boolean resZ = permissionService.hasPermissionForDeploymentOnContext(envZ, anotherResourceGroup);

		// then
		Assert.assertTrue(resGlobal);
		Assert.assertTrue(resParent);
		Assert.assertTrue(resC);
		Assert.assertTrue(resZ);
	}

	@Test
	public void hasPermissionToTemplateModifyResourceTypeWhenUserIsShakedownAdminAndIsTestingMode() {
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
		boolean result = permissionService.hasPermissionToModifyResourceTypeTemplate(as, true);

		// then
		Assert.assertTrue(result);
	}

	@Test
	public void hasPermissionToTemplateModifyResourceTypeWhenUserIsNotShakedownAdminAndIsTestingMode() {
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
		boolean result = permissionService.hasPermissionToModifyResourceTypeTemplate(as, true);

		// then
		Assert.assertFalse(result);
	}

	@Test
	public void hasPermissionToTemplateModifyResourceTypeWhenUserHasResourceTypePermissionAndIsNotTestingMode() {
		// given
		ResourceTypeEntity as = ResourceTypeEntityBuilder.APPLICATION_SERVER_TYPE;

		when(sessionContext.isCallerInRole(CONFIG_ADMIN)).thenReturn(true);
		when(sessionContext.getCallerPrincipal()).thenReturn(principal);
		myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.ALL);
		res.setResourceTypePermission(ResourceTypePermission.ANY);
		myRoles.put(CONFIG_ADMIN, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.RESOURCETYPE, res),
				new RestrictionDTOBuilder().mockRestrictionDTO(Permission.TEMPLATE_RESOURCETYPE, res)));
		permissionService.rolesWithRestrictions = myRoles;

		// when
		boolean result = permissionService.hasPermissionToModifyResourceTypeTemplate(as, false);

		// then
		Assert.assertTrue(result);
	}

	@Test
	public void hasPermissionToTemplateModifyResourceTypeWhenUserHasResourceTypePermissionAndIsApplicationResTypeAndIsNotTestingMode() {
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
		boolean result = permissionService.hasPermissionToModifyResourceTypeTemplate(app, false);

		// then
		Assert.assertTrue(result);
	}

	@Test
	public void hasPermissionToTemplateModifyResourceTypeWhenUserIsShakedownAdminAndIsNotTestingMode() {
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
		boolean result = permissionService.hasPermissionToModifyResourceTypeTemplate(as, false);

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
		myRoles.put(CONFIG_ADMIN, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.EDIT_RES_OR_RESTYPE_NAME, res)));
		myRoles.put(SERVER_ADMIN, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.EDIT_RES_OR_RESTYPE_NAME, res)));
		permissionService.rolesWithRestrictions = myRoles;
			
		// when
		boolean result = permissionService.hasPermission(Permission.EDIT_RES_OR_RESTYPE_NAME);
		
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
		myRoles.put(CONFIG_ADMIN, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.EDIT_RES_OR_RESTYPE_NAME, res)));
		myRoles.put(SERVER_ADMIN, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.EDIT_RES_OR_RESTYPE_NAME, res)));
		permissionService.rolesWithRestrictions = myRoles;
			
		// when
		boolean result = permissionService.hasPermission(Permission.EDIT_RES_OR_RESTYPE_NAME);
		
		// then
		Assert.assertFalse(result);
	}

	@Test
	public void shouldOnlyReloadWhenNeeded() {
		// given
		when(permissionService.permissionRepository.isReloadRolesAndPermissionsList()).thenReturn(false);

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
	
}