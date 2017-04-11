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
import org.hamcrest.CoreMatchers;
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

		// APPLICATION
		anotherResourceGroup = new ResourceGroupEntity();
		anotherResourceGroup.setId(321);
		anotherResourceGroup.setResourceType(new ResourceTypeEntityBuilder().id(1).parentResourceType(null).build());
	}

	@Test
	public void shouldNotAllowToRemoveDefaultInstanceOfResTypeIfHasPermissionForResourcesOnly(){
		//Given
		ResourceTypeEntity applicationResTypeEntity = new ResourceTypeEntity();
		applicationResTypeEntity.setName(DefaultResourceTypeDefinition.APPLICATION.name());
		when(sessionContext.isCallerInRole(CONFIG_ADMIN)).thenReturn(true);
		myRoles = new HashMap<>();
		PermissionEntity permission = new PermissionEntity();
		permission.setValue(Permission.RESOURCE.name());
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.DELETE);
		res.setResourceTypePermission(ResourceTypePermission.NON_DEFAULT_ONLY);
		res.setPermission(permission);
		myRoles.put(CONFIG_ADMIN, Arrays.asList(new RestrictionDTO(res)));
		permissionService.rolesWithRestrictions = myRoles;
		//When
		boolean result = permissionService.hasPermissionToRemoveInstanceOfResType(applicationResTypeEntity);
		//Then
		Assert.assertFalse(result);
	}
	
	@Test
	public void shouldNotAllowToRemoveInstanceOfNonDefaultResTypeIfHasPermissionToDeleteInstancesOfDefaultResourceTypeOnly(){
		//Given
		ResourceTypeEntity nonDefaultResType = new ResourceTypeEntity();
		when(sessionContext.isCallerInRole(SERVER_ADMIN)).thenReturn(true);
		myRoles = new HashMap<>();
		PermissionEntity permission = new PermissionEntity();
		permission.setValue(Permission.RESOURCE.name());
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.DELETE);
		res.setResourceTypePermission(ResourceTypePermission.DEFAULT_ONLY);
		res.setPermission(permission);
		myRoles.put(SERVER_ADMIN, Arrays.asList(new RestrictionDTO(res)));
		permissionService.rolesWithRestrictions = myRoles;
		//When
		boolean result = permissionService.hasPermissionToRemoveInstanceOfResType(nonDefaultResType);
		//Then
		Assert.assertFalse(result);
	}
	
	@Test
	public void shouldAllowToRemoveDefaultInstanceOfResTypeIfHasPermissionToDeleteInstancesOfDefaultResourceType(){
		//Given
		ResourceTypeEntity applicationResTypeEntity = new ResourceTypeEntity();
		applicationResTypeEntity.setName(DefaultResourceTypeDefinition.APPLICATION.name());
		when(sessionContext.isCallerInRole(SERVER_ADMIN)).thenReturn(true);
		myRoles = new HashMap<>();
		PermissionEntity permission = new PermissionEntity();
		permission.setValue(Permission.RESOURCE.name());
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.ALL);
		res.setResourceTypePermission(ResourceTypePermission.DEFAULT_ONLY);
		res.setPermission(permission);
		myRoles.put(SERVER_ADMIN, Arrays.asList(new RestrictionDTO(res)));
		permissionService.rolesWithRestrictions = myRoles;
		//When
		boolean result = permissionService.hasPermissionToRemoveInstanceOfResType(applicationResTypeEntity);
		//Then
		Assert.assertTrue(result);
	}

    @Test
	public void hasPermissionToDeleteResourceRelationWhenUserIsConfigAdminAndResourceIsInstanceOfDefaultResourceType(){
		//Given
        ResourceEntity app = resourceEntityBuilder.mockApplicationEntity("app", null, null);
		
		when(sessionContext.isCallerInRole(CONFIG_ADMIN)).thenReturn(true);
		myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.ALL);
		myRoles.put(CONFIG_ADMIN, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.DELETE_EVERY_RELATED_RESOURCE, res)));
		permissionService.rolesWithRestrictions = myRoles;

		//When
		boolean result = permissionService.hasPermissionToDeleteRelation(app);

		//Then
		Assert.assertTrue(result);
	}
	
	@Test
	public void hasPermissionToDeleteResourceRelationWhenUserIsConfigAdminAndResourceIsInstanceOfNotDefaultResourceType(){
		//Given
        ResourceEntity ws = resourceEntityBuilder.mockResourceEntity("ws", null, "webservice", null);

		when(sessionContext.isCallerInRole(CONFIG_ADMIN)).thenReturn(true);
		myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.ALL);
		myRoles.put(CONFIG_ADMIN, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.DELETE_EVERY_RELATED_RESOURCE, res)));
		permissionService.rolesWithRestrictions = myRoles;

		//When
		boolean result = permissionService.hasPermissionToDeleteRelation(ws);

		//Then
		Assert.assertTrue(result);
	}

    @Test
    public void hasPermissionToDeleteResourceRelationWhenResourceTypeIsNull() {
        //Given
        ResourceEntity resourceWithoutResourceType = ResourceFactory.createNewResource("Orphan");

        when(sessionContext.isCallerInRole(CONFIG_ADMIN)).thenReturn(true);
        myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.ALL);
		myRoles.put(CONFIG_ADMIN, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.DELETE_EVERY_RELATED_RESOURCE, res)));
        permissionService.rolesWithRestrictions = myRoles;

        //When
        boolean result = permissionService.hasPermissionToDeleteRelation(resourceWithoutResourceType);

        //Then
        Assert.assertFalse(result);
    }

	/**
	 * Screen AppServer: remove node relation
	 */
	@Test
	public void hasPermissionToDeleteResourceRelationWhenUserIsServerAdminAndResourceIsInstanceOfAppServerResType(){
		//Given
        ResourceEntity as = resourceEntityBuilder.mockAppServerEntity("as", null, null, null);

		when(sessionContext.isCallerInRole(SERVER_ADMIN)).thenReturn(true);
		myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.ALL);
		myRoles.put(SERVER_ADMIN, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.DELETE_NODE_RELATION, res)));
		permissionService.rolesWithRestrictions = myRoles;

		//When
		boolean result = permissionService.hasPermissionToDeleteRelation(as);

		//Then
		Assert.assertTrue(result);
	}
	
	@Test
	public void hasPermissionToDeleteResourceRelationWhenUserIsServerAdminAndResourceIsNotInstanceOfNodeResType(){
		//Given
		ResourceEntity node = resourceEntityBuilder.mockNodeEntity("node", null,null);

		when(sessionContext.isCallerInRole(SERVER_ADMIN)).thenReturn(true);
		myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.ALL);
		myRoles.put(SERVER_ADMIN, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.DELETE_NODE_RELATION, res)));
		permissionService.rolesWithRestrictions = myRoles;

		//When
		boolean result = permissionService.hasPermissionToDeleteRelation(node);

		//Then
		Assert.assertFalse(result);
	}
	
	@Test
	public void hasPermissionToDeleteResourceRelationWhenUserIsAppDeveloperAndResourceIsInstanceOfApplicationResType(){
		//Given
        ResourceEntity app = resourceEntityBuilder.mockApplicationEntity("app", null, null);

		when(sessionContext.isCallerInRole(APP_DEVELOPER)).thenReturn(true);
		myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.ALL);
		myRoles.put(APP_DEVELOPER, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.DELETE_CONS_OR_PROVIDED_RELATION, res)));
		permissionService.rolesWithRestrictions = myRoles;

		//When
		boolean result = permissionService.hasPermissionToDeleteRelation(app);

		//Then
		Assert.assertTrue(result);
	}
	
	@Test
	public void hasPermissionToDeleteResourceRelationWhenUserIsAppDeveloperAndResourceIsNotInstanceOfApplicationResType(){
		//Given
        ResourceEntity as = resourceEntityBuilder.mockAppServerEntity("as", null, null, null);

		when(sessionContext.isCallerInRole(APP_DEVELOPER)).thenReturn(true);
		myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.ALL);
		myRoles.put(APP_DEVELOPER, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.DELETE_CONS_OR_PROVIDED_RELATION, res)));
		permissionService.rolesWithRestrictions = myRoles;

		//When
		boolean result = permissionService.hasPermissionToDeleteRelation(as);

		//Then
		Assert.assertFalse(result);
	}

    @Test
    public void hasPermissionToDeleteResourceRelationWhenResourceIsRuntimeType(){
        //Given
        ResourceEntity runtime = resourceEntityBuilder.mockRuntimeEntity("EAP6", null, null);

        when(sessionContext.isCallerInRole(SERVER_ADMIN)).thenReturn(true);
        myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.ALL);
		myRoles.put(SERVER_ADMIN, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.SELECT_RUNTIME, res)));
        permissionService.rolesWithRestrictions = myRoles;

        //When
        boolean result = permissionService.hasPermissionToDeleteRelation(runtime);

        //Then
        Assert.assertTrue(result);
    }

    @Test
    public void hasPermissionToAddResourceRelationWhenUserIsConfigAdmin() {
        //Given
        ResourceEntity as = resourceEntityBuilder.mockAppServerEntity("as", null, null, null);

        when(sessionContext.isCallerInRole(CONFIG_ADMIN)).thenReturn(true);
        myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.ALL);
		myRoles.put(CONFIG_ADMIN, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.ADD_EVERY_RELATED_RESOURCE, res)));
        permissionService.rolesWithRestrictions = myRoles;

        //When
        boolean consumed = permissionService.hasPermissionToAddRelation(as, false);
        boolean provided = permissionService.hasPermissionToAddRelation(as, true);

        //Then
        Assert.assertTrue(consumed);
        Assert.assertTrue(provided);
    }

    @Test
    public void hasPermissionToAddResourceRelationWhenResourceTypeIsNull() {
        //Given
        //Create resource without resourceType
        ResourceEntity resourceWithoutResourceType = ResourceFactory.createNewResource("Orphan");
        //end Create resource without resourceType

        when(sessionContext.isCallerInRole(CONFIG_ADMIN)).thenReturn(true);
        myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.ALL);
		myRoles.put(CONFIG_ADMIN, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.ADD_EVERY_RELATED_RESOURCE, res)));
        permissionService.rolesWithRestrictions = myRoles;

        //When
        boolean consumed = permissionService.hasPermissionToAddRelation(resourceWithoutResourceType, false);
        boolean provided = permissionService.hasPermissionToAddRelation(resourceWithoutResourceType, true);

        //Then
        Assert.assertFalse(consumed);
        Assert.assertFalse(provided);
    }

    @Test
    public void hasPermissionToAddResourceRelationWhenUserIsServeAdminAndResourceIsInstanceOfApplicationServerResType() {
        //Given
        ResourceEntity as = resourceEntityBuilder.mockAppServerEntity("as", null, null, null);

        when(sessionContext.isCallerInRole(SERVER_ADMIN)).thenReturn(true);
        myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.ALL);
		myRoles.put(SERVER_ADMIN, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.ADD_NODE_RELATION, res)));
        permissionService.rolesWithRestrictions = myRoles;

        //When
        boolean consumed = permissionService.hasPermissionToAddRelation(as, false);
        boolean provided = permissionService.hasPermissionToAddRelation(as, true);

        //Then
        Assert.assertTrue(consumed);
        Assert.assertTrue(provided);
    }

    @Test
    public void hasPermissionToAddResourceRelationWhenUserIsServeAdminAndResourceIsInstanceOfApplicationResType() {
        //Given
        ResourceEntity app = resourceEntityBuilder.mockApplicationEntity("app", null, null);

        when(sessionContext.isCallerInRole(SERVER_ADMIN)).thenReturn(true);
        myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.ALL);
		myRoles.put(SERVER_ADMIN, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.ADD_NODE_RELATION, res)));
        permissionService.rolesWithRestrictions = myRoles;

        //When
        boolean consumed = permissionService.hasPermissionToAddRelation(app, false);
        boolean provided = permissionService.hasPermissionToAddRelation(app, true);

        //Then
        Assert.assertFalse(consumed);
        Assert.assertFalse(provided);
    }

    @Test
	public void hasPermissionToAddResourceRelationWhenUserIsAppDeveloperAndResourceIsInstanceOfApplicationResType(){
		//Given
        ResourceEntity app = resourceEntityBuilder.mockApplicationEntity("app", null, null);

		when(sessionContext.isCallerInRole(APP_DEVELOPER)).thenReturn(true);
		myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.ALL);
		myRoles.put(APP_DEVELOPER, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.ADD_RELATED_RESOURCE, res),
				new RestrictionDTOBuilder().mockRestrictionDTO(Permission.ADD_AS_CONSUMED_RESOURCE, res),
				new RestrictionDTOBuilder().mockRestrictionDTO(Permission.ADD_AS_PROVIDED_RESOURCE, res)));
		permissionService.rolesWithRestrictions = myRoles;

		//When
        boolean consumed = permissionService.hasPermissionToAddRelation(app, false);
        boolean provided = permissionService.hasPermissionToAddRelation(app, true);

		//Then
		Assert.assertTrue(consumed);
        Assert.assertTrue(provided);
	}

    @Test
    public void hasPermissionToAddResourceRelationWhenUserIsAppDeveloperAndResourceIsNotInstanceOfApplicationResType() {
        //Given
        ResourceEntity as = resourceEntityBuilder.mockAppServerEntity("as", null, null, null);

        when(sessionContext.isCallerInRole(APP_DEVELOPER)).thenReturn(true);
        myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.ALL);
		myRoles.put(APP_DEVELOPER, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.ADD_RELATED_RESOURCE, res)));
        permissionService.rolesWithRestrictions = myRoles;

        //When
        boolean consumed = permissionService.hasPermissionToAddRelation(as, false);
        boolean provided = permissionService.hasPermissionToAddRelation(as, true);

        //Then
        Assert.assertFalse(consumed);
        Assert.assertFalse(provided);
    }

    @Test
    public void hasPermissionToTemplateModifyWhenUserIsShakedownAdminAndIsTestingMode() {
        //Given
        ResourceEntity as = resourceEntityBuilder.mockAppServerEntity("as", null, null, null);

        when(sessionContext.isCallerInRole(SHAKEDOWN_ADMIN)).thenReturn(true);
        myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.ALL);
		myRoles.put(SHAKEDOWN_ADMIN, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.SHAKEDOWN_TEST_MODE, res)));
        permissionService.rolesWithRestrictions = myRoles;

        //When
        boolean result = permissionService.hasPermissionToModifyResourceTemplate(as, true);

        //Then
        Assert.assertTrue(result);
    }

    @Test
    public void hasPermissionToTemplateModifyWhenUserIsNotShakedownAdminAndIsTestingMode() {
        //Given
        ResourceEntity as = resourceEntityBuilder.mockAppServerEntity("as", null, null, null);

        when(sessionContext.isCallerInRole(VIEWER)).thenReturn(true);
        myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.ALL);
		myRoles.put(VIEWER, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.DEFAULT, res)));
        permissionService.rolesWithRestrictions = myRoles;

        //When
        boolean result = permissionService.hasPermissionToModifyResourceTemplate(as, true);

        //Then
        Assert.assertFalse(result);
    }

    @Test
    public void hasPermissionToTemplateModifyWhenUserHasPermissionToUpdateResourceAndIsResourceIsNotTestingMode() {
        //Given
        ResourceEntity as = resourceEntityBuilder.mockAppServerEntity("as", null, null, null);

        when(sessionContext.isCallerInRole(CONFIG_ADMIN)).thenReturn(true);
        myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.ALL);
		myRoles.put(CONFIG_ADMIN, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.RESOURCE, res)));
        permissionService.rolesWithRestrictions = myRoles;

        //When
        boolean result = permissionService.hasPermissionToModifyResourceTemplate(as, false);

        //Then
        Assert.assertTrue(result);
    }

	@Test
	public void hasPermissionToTemplateModifyWhenUserHasPermissionToUpdateTemplateResourceAndIsResourceIsNotTestingMode() {
		//Given
		ResourceEntity as = resourceEntityBuilder.mockAppServerEntity("as", null, null, null);

		when(sessionContext.isCallerInRole(CONFIG_ADMIN)).thenReturn(true);
		myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.UPDATE);
		myRoles.put(CONFIG_ADMIN, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.TEMPLATE_RESOURCE, res)));
		permissionService.rolesWithRestrictions = myRoles;

		//When
		boolean result = permissionService.hasPermissionToModifyResourceTemplate(as, false);

		//Then
		Assert.assertTrue(result);
	}

    @Test
    public void hasPermissionToTemplateModifyWhenUserHasPermissionToUpdateResourceTypeAndIsApplicationResTypeAndIsNotTestingMode() {
        //Given
		ResourceTypeEntity app = ResourceTypeEntityBuilder.APPLICATION_TYPE;

        when(sessionContext.isCallerInRole(APP_DEVELOPER)).thenReturn(true);
        myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.ALL);
		res.setResourceTypePermission(ResourceTypePermission.ANY);
		myRoles.put(APP_DEVELOPER, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.RESOURCETYPE, res)));
        permissionService.rolesWithRestrictions = myRoles;

        //When
        boolean result = permissionService.hasPermissionToModifyResourceTypeTemplate(app, false);

        //Then
        Assert.assertTrue(result);
    }

	@Test
	public void hasPermissionToTemplateModifyWhenUserHasPermissionToUpdateTemplateResourceTypeAndIsApplicationResTypeAndIsNotTestingMode() {
		//Given
		ResourceTypeEntity app = ResourceTypeEntityBuilder.APPLICATION_TYPE;

		when(sessionContext.isCallerInRole(APP_DEVELOPER)).thenReturn(true);
		myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.UPDATE);
		res.setResourceTypePermission(ResourceTypePermission.ANY);
		myRoles.put(APP_DEVELOPER, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.TEMPLATE_RESOURCETYPE, res)));
		permissionService.rolesWithRestrictions = myRoles;

		//When
		boolean result = permissionService.hasPermissionToModifyResourceTypeTemplate(app, false);

		//Then
		Assert.assertTrue(result);
	}

    @Test
    public void hasPermissionToTemplateModifyWhenUserIsShakedownAdminAndIsNotTestingMode() {
        //Given
        ResourceEntity as = resourceEntityBuilder.mockAppServerEntity("as", null, null, null);

        when(sessionContext.isCallerInRole(SHAKEDOWN_ADMIN)).thenReturn(true);
        myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.ALL);
		myRoles.put(SHAKEDOWN_ADMIN, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.SHAKEDOWN_TEST_MODE, res)));
        permissionService.rolesWithRestrictions = myRoles;

        //When
        boolean result = permissionService.hasPermissionToModifyResourceTemplate(as, false);

        //Then
        Assert.assertFalse(result);
    }

	@Test
	public void hasPermissionToDeployWhenRoleIsNotDeployable(){
		//given
		Map<String, List<RestrictionDTO>> deployableRolesWithRestrictions;
		RoleEntity roleToDeployEnvC = new RoleEntity();
		roleToDeployEnvC.setName(TEST_DEPLOYER);
		
		when(sessionContext.isCallerInRole(ROLE_NOT_DEPLOY)).thenReturn(true);

		PermissionEntity pe = new PermissionEntity();
		pe.setValue("aTestPermission");
		deployableRolesWithRestrictions = new HashMap<>();
		deployableRolesWithRestrictions.put(roleToDeployEnvC.getName(), Arrays.asList(new RestrictionDTO(pe, roleToDeployEnvC)));
		
		permissionService.deployableRolesWithRestrictions = deployableRolesWithRestrictions;
		
		//When
		boolean result = permissionService.hasPermissionToDeploy();

		//then
		Assert.assertFalse(result);
	}
	
	@Test
	public void hasPermissionToDeployWhenEmptyList(){
		//given
		List<RoleEntity>deployableRoles = new ArrayList<>();
		EntityManager entityManager = Mockito.mock(EntityManager.class);
		Query value = Mockito.mock(Query.class);
		when(entityManager.createQuery("from RoleEntity r where r.deployable=1")).thenReturn(value);
		when(value.getResultList()).thenReturn(deployableRoles);
		when(sessionContext.isCallerInRole(ROLE_NOT_DEPLOY)).thenReturn(true);
		//When
		boolean result = permissionService.hasPermissionToDeploy();
		//then
		Assert.assertFalse(result);
	}

	@Test
	public void hasPermissionToDeployOnEnvironmentCTest(){
		//given
		RoleEntity roleTestDeployer = new RoleEntity();
		roleTestDeployer.setName(TEST_DEPLOYER);
		PermissionEntity permissionToDeploy = new PermissionEntity();
		permissionToDeploy.setValue(Permission.DEPLOYMENT.name());
		roleTestDeployer.getPermissions().add(permissionToDeploy);
		when(sessionContext.isCallerInRole(TEST_DEPLOYER)).thenReturn(true);
		myRoles = new HashMap<>();
		//assign restriction allowing all on environment "c"
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.ALL);
		res.setContext(envC);
		res.setPermission(permissionToDeploy);
		myRoles.put(TEST_DEPLOYER, Arrays.asList(new RestrictionDTO(res)));
		permissionService.deployableRolesWithRestrictions = myRoles;
		when(permissionService.permissionRepository.isReloadDeployableRoleList()).thenReturn(false);

		//When
		boolean result = permissionService.hasPermissionForDeploymentOnContext(envC, new ResourceGroupEntity());

		//Then
		Assert.assertTrue(result);
	}

	@Test
	public void hasNoPermissionToDeployOnEnvironmentZTest(){
		//given
		RoleEntity roleTestDeployer = new RoleEntity();
		roleTestDeployer.setName(TEST_DEPLOYER);
		PermissionEntity permissionToDeploy = new PermissionEntity();
		permissionToDeploy.setValue(Permission.DEPLOYMENT.name());
		roleTestDeployer.getPermissions().add(permissionToDeploy);
		when(sessionContext.isCallerInRole(TEST_DEPLOYER)).thenReturn(true);
		myRoles = new HashMap<>();
		//assign restriction allowing all on environment "c"
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.ALL);
		res.setContext(envC);
		res.setPermission(permissionToDeploy);
		myRoles.put(TEST_DEPLOYER, Arrays.asList(new RestrictionDTO(res)));
		permissionService.deployableRolesWithRestrictions = myRoles;
		when(permissionService.permissionRepository.isReloadDeployableRoleList()).thenReturn(false);

		//When
		boolean resC = permissionService.hasPermissionForDeploymentOnContext(envC, new ResourceGroupEntity());
		boolean resZ = permissionService.hasPermissionForDeploymentOnContext(envZ, new ResourceGroupEntity());

		//Then
		Assert.assertTrue(resC);
		Assert.assertFalse(resZ);
	}

	@Test
	public void hasPermissionToDeployOnlySpecificResourceOnEnvironmentCTest(){
		//given
		ResourceGroupEntity allowedResourceGroup = new ResourceGroupEntity();
		allowedResourceGroup.setId(42);
		allowedResourceGroup.setResourceType(new ResourceTypeEntityBuilder().id(2).build());
		RoleEntity roleTestDeployer = new RoleEntity();
		roleTestDeployer.setName(TEST_DEPLOYER);
		PermissionEntity permissionToDeploy = new PermissionEntity();
		permissionToDeploy.setValue(Permission.DEPLOYMENT.name());
		roleTestDeployer.getPermissions().add(permissionToDeploy);
		when(sessionContext.isCallerInRole(TEST_DEPLOYER)).thenReturn(true);
		myRoles = new HashMap<>();
		//assign restriction allowing all on environment "c"
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.ALL);
		res.setResourceTypePermission(ResourceTypePermission.ANY);
		res.setContext(envC);
		res.setResourceGroup(allowedResourceGroup);
		res.setPermission(permissionToDeploy);
		myRoles.put(TEST_DEPLOYER, Arrays.asList(new RestrictionDTO(res)));
		permissionService.deployableRolesWithRestrictions = myRoles;
		when(permissionService.permissionRepository.isReloadDeployableRoleList()).thenReturn(false);

		//When
		boolean resC = permissionService.hasPermissionForDeploymentOnContext(envC, anotherResourceGroup);
		boolean resCAllowed = permissionService.hasPermissionForDeploymentOnContext(envC, allowedResourceGroup);
		boolean resZ = permissionService.hasPermissionForDeploymentOnContext(envZ, allowedResourceGroup);

		//Then
		Assert.assertFalse(resC);
		Assert.assertTrue(resCAllowed);
		Assert.assertFalse(resZ);
	}

	@Test
	public void hasPermissionToDeployOnlySpecificResourceTypeOnEnvironmentZTest(){
		//given
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
		myRoles = new HashMap<>();
		//assign restriction allowing all on environment "c"
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.ALL);
		res.setResourceTypePermission(ResourceTypePermission.ANY);
		res.setContext(envZ);
		res.setResourceType(allowedResourceType);
		res.setPermission(permissionToDeploy);
		myRoles.put(TEST_DEPLOYER, Arrays.asList(new RestrictionDTO(res)));
		permissionService.deployableRolesWithRestrictions = myRoles;
		when(permissionService.permissionRepository.isReloadDeployableRoleList()).thenReturn(false);

		//When
		boolean resZ = permissionService.hasPermissionForDeploymentOnContext(envZ, anotherResourceGroup);
		boolean resZAllowed = permissionService.hasPermissionForDeploymentOnContext(envZ, resourceGroup);
		boolean resC = permissionService.hasPermissionForDeploymentOnContext(envC, resourceGroup);

		//Then
		Assert.assertFalse(resZ);
		Assert.assertTrue(resZAllowed);
		Assert.assertFalse(resC);
	}

	@Test
	public void hasPermissionToDeployOnlySpecificResourceTypeOnAllEnvironmentsTest(){
		//given
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
		myRoles = new HashMap<>();
		//assign restriction allowing all on environment "c"
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.ALL);
		res.setResourceTypePermission(ResourceTypePermission.ANY);
		res.setResourceType(allowedResourceType);
		res.setPermission(permissionToDeploy);
		myRoles.put(TEST_DEPLOYER, Arrays.asList(new RestrictionDTO(res)));
		permissionService.deployableRolesWithRestrictions = myRoles;
		when(permissionService.permissionRepository.isReloadDeployableRoleList()).thenReturn(false);

		//When
		boolean resZ = permissionService.hasPermissionForDeploymentOnContext(envZ, anotherResourceGroup);
		boolean resZAllowed = permissionService.hasPermissionForDeploymentOnContext(envZ, resourceGroup);
		boolean resCAllowed = permissionService.hasPermissionForDeploymentOnContext(envC, resourceGroup);

		//Then
		Assert.assertFalse(resZ);
		Assert.assertTrue(resZAllowed);
		Assert.assertTrue(resCAllowed);
	}

	@Test
	public void hasPermissionToDeployOnChildsIfHasPermissionForParentTest(){
		//given
		RoleEntity roleTestDeployer = new RoleEntity();
		roleTestDeployer.setName(TEST_DEPLOYER);
		PermissionEntity permissionToDeploy = new PermissionEntity();
		permissionToDeploy.setValue(Permission.DEPLOYMENT.name());
		roleTestDeployer.getPermissions().add(permissionToDeploy);
		when(sessionContext.isCallerInRole(TEST_DEPLOYER)).thenReturn(true);
		myRoles = new HashMap<>();
		//assign restriction allowing all on environment "c"
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.ALL);
		res.setResourceTypePermission(ResourceTypePermission.ANY);
		res.setContext(parent);
		res.setPermission(permissionToDeploy);
		myRoles.put(TEST_DEPLOYER, Arrays.asList(new RestrictionDTO(res)));
		permissionService.deployableRolesWithRestrictions = myRoles;
		when(permissionService.permissionRepository.isReloadDeployableRoleList()).thenReturn(false);

		//When
		boolean resC = permissionService.hasPermissionForDeploymentOnContext(envC, anotherResourceGroup);
		boolean resZ = permissionService.hasPermissionForDeploymentOnContext(envZ, anotherResourceGroup);

		//Then
		Assert.assertTrue(resC);
		Assert.assertTrue(resZ);
	}

	@Test
	public void hasPermissionToDeployOnAllEnvironmentsIfHasPermissionForGlobalTest(){
		//given
		RoleEntity roleTestDeployer = new RoleEntity();
		roleTestDeployer.setName(TEST_DEPLOYER);
		PermissionEntity permissionToDeploy = new PermissionEntity();
		permissionToDeploy.setValue(Permission.DEPLOYMENT.name());
		roleTestDeployer.getPermissions().add(permissionToDeploy);
		when(sessionContext.isCallerInRole(TEST_DEPLOYER)).thenReturn(true);
		myRoles = new HashMap<>();
		//assign restriction allowing all on environment "c"
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.ALL);
		res.setContext(global);
		res.setResourceTypePermission(ResourceTypePermission.ANY);
		res.setPermission(permissionToDeploy);
		myRoles.put(TEST_DEPLOYER, Arrays.asList(new RestrictionDTO(res)));
		permissionService.deployableRolesWithRestrictions = myRoles;
		when(permissionService.permissionRepository.isReloadDeployableRoleList()).thenReturn(false);

		//When
		boolean resGlobal = permissionService.hasPermissionForDeploymentOnContext(global, anotherResourceGroup);
		boolean resParent = permissionService.hasPermissionForDeploymentOnContext(parent, anotherResourceGroup);
		boolean resC = permissionService.hasPermissionForDeploymentOnContext(envC, anotherResourceGroup);
		boolean resZ = permissionService.hasPermissionForDeploymentOnContext(envZ, anotherResourceGroup);

		//Then
		Assert.assertTrue(resGlobal);
		Assert.assertTrue(resParent);
		Assert.assertTrue(resC);
		Assert.assertTrue(resZ);
	}

	@Test
	public void hasPermissionToTemplateModifyResourceTypeWhenUserIsShakedownAdminAndIsTestingMode() {
		//Given
		ResourceTypeEntity as = ResourceTypeEntityBuilder.APPLICATION_SERVER_TYPE;

		when(sessionContext.isCallerInRole(SHAKEDOWN_ADMIN)).thenReturn(true);
		myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.ALL);
		myRoles.put(SHAKEDOWN_ADMIN, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.SHAKEDOWN_TEST_MODE, res)));
		permissionService.rolesWithRestrictions = myRoles;

		//When
		boolean result = permissionService.hasPermissionToModifyResourceTypeTemplate(as, true);

		//Then
		Assert.assertTrue(result);
	}

	@Test
	public void hasPermissionToTemplateModifyResourceTypeWhenUserIsNotShakedownAdminAndIsTestingMode() {
		//Given
		ResourceTypeEntity as = ResourceTypeEntityBuilder.APPLICATION_SERVER_TYPE;

		when(sessionContext.isCallerInRole(VIEWER)).thenReturn(true);
		myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.ALL);
		myRoles.put(VIEWER, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.DEFAULT, res)));
		permissionService.rolesWithRestrictions = myRoles;

		//When
		boolean result = permissionService.hasPermissionToModifyResourceTypeTemplate(as, true);

		//Then
		Assert.assertFalse(result);
	}

	@Test
	public void hasPermissionToTemplateModifyResourceTypeWhenUserHasResourceTypePermissionAndIsNotTestingMode() {
		//Given
		ResourceTypeEntity as = ResourceTypeEntityBuilder.APPLICATION_SERVER_TYPE;

		when(sessionContext.isCallerInRole(CONFIG_ADMIN)).thenReturn(true);
		myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.ALL);
		res.setResourceTypePermission(ResourceTypePermission.ANY);
		myRoles.put(CONFIG_ADMIN, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.RESOURCETYPE, res),
				new RestrictionDTOBuilder().mockRestrictionDTO(Permission.TEMPLATE_RESOURCETYPE, res)));
		permissionService.rolesWithRestrictions = myRoles;

		//When
		boolean result = permissionService.hasPermissionToModifyResourceTypeTemplate(as, false);

		//Then
		Assert.assertTrue(result);
	}

	@Test
	public void hasPermissionToTemplateModifyResourceTypeWhenUserHasResourceTypePermissionAndIsApplicationResTypeAndIsNotTestingMode() {
		//Given
		ResourceTypeEntity app = ResourceTypeEntityBuilder.APPLICATION_TYPE;

		when(sessionContext.isCallerInRole(APP_DEVELOPER)).thenReturn(true);
		myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.ALL);
		res.setResourceTypePermission(ResourceTypePermission.ANY);
		myRoles.put(APP_DEVELOPER, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.RESOURCETYPE, res)));
		permissionService.rolesWithRestrictions = myRoles;

		//When
		boolean result = permissionService.hasPermissionToModifyResourceTypeTemplate(app, false);

		//Then
		Assert.assertTrue(result);
	}

	@Test
	public void hasPermissionToTemplateModifyResourceTypeWhenUserIsShakedownAdminAndIsNotTestingMode() {
		//Given
		ResourceTypeEntity as = ResourceTypeEntityBuilder.APPLICATION_SERVER_TYPE;

		when(sessionContext.isCallerInRole(SHAKEDOWN_ADMIN)).thenReturn(true);
		myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.ALL);
		myRoles.put(SHAKEDOWN_ADMIN, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.SHAKEDOWN_TEST_MODE, res)));
		permissionService.rolesWithRestrictions = myRoles;

		//When
		boolean result = permissionService.hasPermissionToModifyResourceTypeTemplate(as, false);

		//Then
		Assert.assertFalse(result);
	}
	
	@Test
	public void permissionInTwoRoles(){
		//Given
		when(sessionContext.isCallerInRole(CONFIG_ADMIN)).thenReturn(false);
		when(sessionContext.isCallerInRole(SERVER_ADMIN)).thenReturn(true);
		myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.ALL);
		myRoles.put(CONFIG_ADMIN, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.EDIT_RES_OR_RESTYPE_NAME, res)));
		myRoles.put(SERVER_ADMIN, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.EDIT_RES_OR_RESTYPE_NAME, res)));
		permissionService.rolesWithRestrictions = myRoles;
			
		//When
		boolean result = permissionService.hasPermission(Permission.EDIT_RES_OR_RESTYPE_NAME);
		
		//Then
		Assert.assertTrue(result);
	}
	
	@Test
	public void permissionInTwoRolesFail(){
		//Given
		when(sessionContext.isCallerInRole(CONFIG_ADMIN)).thenReturn(false);
		when(sessionContext.isCallerInRole(SERVER_ADMIN)).thenReturn(false);
		myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.ALL);
		myRoles.put(CONFIG_ADMIN, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.EDIT_RES_OR_RESTYPE_NAME, res)));
		myRoles.put(SERVER_ADMIN, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.EDIT_RES_OR_RESTYPE_NAME, res)));
		permissionService.rolesWithRestrictions = myRoles;
			
		//When
		boolean result = permissionService.hasPermission(Permission.EDIT_RES_OR_RESTYPE_NAME);
		
		//Then
		Assert.assertFalse(result);
	}

	@Test
	public void shouldOnlyReloadWhenNeeded() {
		//Given
		when(permissionService.permissionRepository.isReloadRolesAndPermissionsList()).thenReturn(false);

		//When
		permissionService.getPermissions();

		//Then
		verify(permissionService.permissionRepository, never()).getRolesWithPermissions();
		verify(permissionService.permissionRepository, never()).getRolesWithRestrictions();
	}

	@Test
	public void shouldObtainLegacyRolesWithPermissionsAndRolesWithRestrictions() {
		//Given
		when(permissionService.permissionRepository.isReloadRolesAndPermissionsList()).thenReturn(true);
		when(permissionService.permissionRepository.getRolesWithPermissions()).thenReturn(null);
		when(permissionService.permissionRepository.getRolesWithRestrictions()).thenReturn(null);

		//When
		permissionService.getPermissions();

		//Then
		verify(permissionService.permissionRepository, times(1)).getRolesWithPermissions();
		verify(permissionService.permissionRepository, times(1)).getRolesWithRestrictions();
	}

	@Test
	public void shouldCombineLegacyRolesWithPermissionsAndRolesWithRestrictions() {
		//Given
		RoleEntity legacyRole = new RoleEntity();
		legacyRole.setName("aLegacyTestRole");
		PermissionEntity pe = new PermissionEntity();
		pe.setValue("aTestPermission");
		PermissionEntity pe2 = new PermissionEntity();
		pe2.setValue("anotherTestPermission");
		Set<PermissionEntity> permissions = new HashSet<>(Arrays.asList(pe,pe2));
		legacyRole.setPermissions(permissions);

		RoleEntity newRole = new RoleEntity();
		newRole.setName("aNewTestRole");
		RestrictionEntity re = new RestrictionEntity();
		re.setPermission(pe);
		Set<RestrictionEntity> restrictions = new HashSet<>(Arrays.asList(re));
		newRole.setRestrictions(restrictions);

		when(permissionService.permissionRepository.isReloadRolesAndPermissionsList()).thenReturn(true);
		when(permissionService.permissionRepository.getRolesWithPermissions()).thenReturn(Arrays.asList(legacyRole));
		when(permissionService.permissionRepository.getRolesWithRestrictions()).thenReturn(Arrays.asList(newRole));

		//When
		Map<String, List<RestrictionDTO>> result = permissionService.getPermissions();

		//Then
		Assert.assertEquals(2, result.size());
		Assert.assertEquals(2, result.get("aLegacyTestRole").size());
		Assert.assertEquals(1, result.get("aNewTestRole").size());
		Assert.assertEquals("aTestPermission", result.get("aNewTestRole").get(0).getPermissionName());
	}

	@Test
	public void shouldObtainDeployableRolesOnGetDeployableRolesNonCached() {
		//Given
		when(permissionService.permissionRepository.getDeployableRoles()).thenReturn(EMPTY_LIST);

		//When
		permissionService.getDeployableRolesNonCached();

		//Then
		verify(permissionService.permissionRepository, times(1)).getDeployableRoles();
	}

	@Test
	public void shouldObtainDeployableRolesOnGetDeployableRolesWhenNeeded() {
		//Given
		when(permissionService.permissionRepository.isReloadDeployableRoleList()).thenReturn(true);
		when(permissionService.permissionRepository.getDeployableRoles()).thenReturn(EMPTY_LIST);

		//When
		permissionService.getDeployableRoles();

		//Then
		verify(permissionService.permissionRepository, times(1)).getDeployableRoles();
	}
	
}