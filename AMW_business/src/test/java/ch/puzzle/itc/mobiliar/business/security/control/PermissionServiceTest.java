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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.SessionContext;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import ch.puzzle.itc.mobiliar.builders.ResourceEntityBuilder;
import ch.puzzle.itc.mobiliar.builders.RestrictionDTOBuilder;
import ch.puzzle.itc.mobiliar.business.integration.entity.util.ResourceTypeEntityBuilder;
import ch.puzzle.itc.mobiliar.business.security.entity.*;
import org.junit.Assert;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceFactory;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceType;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;
import ch.puzzle.itc.mobiliar.common.util.DefaultResourceTypeDefinition;

import static org.mockito.Mockito.when;


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
//	private ResourceTypeEntityBuilder resourceTypeEntityBuilder = new ResourceTypeEntityBuilder();

    @Before
	public void setUp(){
		permissionService = new PermissionService();
		sessionContext = Mockito.mock(SessionContext.class);
		permissionService.sessionContext = sessionContext;
		permissionRepository = Mockito.mock(PermissionRepository.class);
		permissionService.permissionRepository = permissionRepository;
	}
	
	@Test
	public void hasPermissionToEditPropertiesWhenIsApplicationResTypeAndAppDeveloperTest(){
		//given
		when(sessionContext.isCallerInRole(APP_DEVELOPER)).thenReturn(true);
		myRoles = new HashMap<>();
		//Add permissions to app_developer
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.A);
		myRoles.put(APP_DEVELOPER, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.EDIT_PROP_LIST_OF_INST_APP, res)));
		permissionService.rolesWithRestrictions = myRoles;

		//When
		boolean result = permissionService.hasPermissionToEditResourceTypeProperties();
		
		//Then
		Assert.assertFalse(result);
	}


	@Test
	public void hasPermissionToEditPropertiesWhenUserIsConfigAdminAndResTypeIsNotDefault(){
		//Given
		when(sessionContext.isCallerInRole(CONFIG_ADMIN)).thenReturn(true);
		myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.A);
		myRoles.put(CONFIG_ADMIN, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.EDIT_NOT_DEFAULT_RES_OF_RESTYPE, res)));
		permissionService.rolesWithRestrictions = myRoles;
			
		//When
		boolean result = permissionService.hasPermissionToEditResourceTypeProperties();
				
		//Then
		Assert.assertTrue(result);
	}


	@Test
	public void hasPermissionToRenameResourceTypeWhenResTypeIsNotDefaultResTypeTest(){
		//Given
		//Create notDeafultResourceType
		ResourceTypeEntity databaseResTypeEntity = new ResourceTypeEntity();
		databaseResTypeEntity.setName("database");
		//End create notDefaultResource
		when(sessionContext.isCallerInRole(CONFIG_ADMIN)).thenReturn(true);
		myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.A);
		myRoles.put(CONFIG_ADMIN, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.EDIT_RES_OR_RESTYPE_NAME, res)));
		permissionService.rolesWithRestrictions = myRoles;
			
		//When
		boolean result = permissionService.hasPermissionToRenameResourceType(databaseResTypeEntity);
		
		//Then
		Assert.assertTrue(result);
	}
	
	@Test
	public void hasPermissionToRenameResourceTypeWhenResTypeIsDefaultApplicationResTypeTest(){ 
		//Given
		//create APPLICATION resouceTypeEntity 
		ResourceTypeEntity applicationResTypeEntity = new ResourceTypeEntity();
		applicationResTypeEntity.setName(DefaultResourceTypeDefinition.APPLICATION.name());
		//end create resourceType
		
		when(sessionContext.isCallerInRole(CONFIG_ADMIN)).thenReturn(true);
		myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.A);
		myRoles.put(CONFIG_ADMIN, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.EDIT_RES_OR_RESTYPE_NAME, res)));
		permissionService.rolesWithRestrictions = myRoles;
					
		//When
		boolean result = permissionService.hasPermissionToRenameResourceType(applicationResTypeEntity);
				
		//Then
		Assert.assertFalse(result);
	}
	
	@Test
	public void hasPermissionToRenameResourceTypeWhenResTypeIsDefaultApplicationServerResTypeTest(){ 
		//Given
		//create APPLICATION resouceTypeEntity 
		ResourceTypeEntity applicationServerResTypeEntity = new ResourceTypeEntity();
		applicationServerResTypeEntity.setName(DefaultResourceTypeDefinition.APPLICATIONSERVER.name());
		//end create resourceType
		
		when(sessionContext.isCallerInRole(CONFIG_ADMIN)).thenReturn(true);
		myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.A);
		myRoles.put(CONFIG_ADMIN, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.EDIT_RES_OR_RESTYPE_NAME, res)));
		permissionService.rolesWithRestrictions = myRoles;
		//When
		boolean result = permissionService.hasPermissionToRenameResourceType(applicationServerResTypeEntity);
				
		//Then
		Assert.assertFalse(result);
	}
	
	@Test
	public void hasPermissionToRenameResourceTypeWhenResTypeIsDefaultNodeResTypeTest(){ 
		//Given
		//Create NODE resourceTypeEntity
		ResourceTypeEntity nodeResTypeEntity = new ResourceTypeEntity();
		nodeResTypeEntity.setName(DefaultResourceTypeDefinition.NODE.name());
		//end Create Node
		when(sessionContext.isCallerInRole(CONFIG_ADMIN)).thenReturn(true);
		myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.A);
		myRoles.put(CONFIG_ADMIN, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.EDIT_RES_OR_RESTYPE_NAME, res)));
		permissionService.rolesWithRestrictions = myRoles;
					
		//When
		boolean result = permissionService.hasPermissionToRenameResourceType(nodeResTypeEntity);
		
		//Then
		Assert.assertFalse(result);
	}

	@Test
	public void hasPermissionToRenameResourceTypeWhenResTypeIsNullTest(){ 
		//Given
		when(sessionContext.isCallerInRole(CONFIG_ADMIN)).thenReturn(true);
		myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.A);
		myRoles.put(CONFIG_ADMIN, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.EDIT_RES_OR_RESTYPE_NAME, res)));
		permissionService.rolesWithRestrictions = myRoles;
					
		//When
		boolean result = permissionService.hasPermissionToRenameResourceType(null);
				
		//Then
		Assert.assertFalse(result);
	}
	
	@Test
	public void hasPermissionToRenameResourceTypeWhenResTypeIsDefaultNodeResTypeAndUserIsNotConfigAdminTest(){ 
		//Given
		//Create NODE resourceTypeEntity
		ResourceTypeEntity nodeResTypeEntity = new ResourceTypeEntity();
		nodeResTypeEntity.setName(DefaultResourceTypeDefinition.NODE.name());
		//end Create Node
		when(sessionContext.isCallerInRole(VIEWER)).thenReturn(true);
		myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.A);
		myRoles.put(VIEWER, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.DEFAULT, res)));
		permissionService.rolesWithRestrictions = myRoles;
					
		//When
		boolean result = permissionService.hasPermissionToRenameResourceType(nodeResTypeEntity);
				
		//Then
		Assert.assertFalse(result);
	}
	
	
	@Test
	public void hasPermissionToEditPropertiesOfResourceWhenUserIsConfigAdminAndResourceIsParentOfApplicationResTypeTest(){
		//Given
		//create APPLICATION resouceTypeEntity 
		ResourceTypeEntity applicationResTypeEntity = new ResourceTypeEntity();
		applicationResTypeEntity.setName(DefaultResourceTypeDefinition.APPLICATION.name());
		//wrap ReourceTypeEntity to ResourceType 
		ResourceType defaultApplicationResTyp = new ResourceType();
		defaultApplicationResTyp.wrap(applicationResTypeEntity);
		//end create resourceType
		when(sessionContext.isCallerInRole(CONFIG_ADMIN)).thenReturn(true);
		myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.A);
		myRoles.put(CONFIG_ADMIN, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.EDIT_ALL_PROPERTIES, res)));
		permissionService.rolesWithRestrictions = myRoles;
					
		//When
		boolean result = permissionService.hasPermissionToEditPropertiesOfResource(defaultApplicationResTyp.getEntity());
				
		//Then
		Assert.assertTrue(result);
	}

    @Test
	public void hasPermissionToEditPropertiesOfResourceWhenUserIsConfigAdminAndResourceTypeIsNotDefaultResTypeTest(){
		//Given
		//Create notDeafultResourceType
		ResourceTypeEntity databaseResTypeEntity = new ResourceTypeEntity();
		ResourceType notDefaultDatabaseResType = new ResourceType();
		notDefaultDatabaseResType.wrap(databaseResTypeEntity);
		//End create notDefaultResource
		when(sessionContext.isCallerInRole(CONFIG_ADMIN)).thenReturn(true);
		myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.A);
		myRoles.put(CONFIG_ADMIN, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.EDIT_ALL_PROPERTIES, res)));
		permissionService.rolesWithRestrictions = myRoles;
					
		//When
		boolean result = permissionService.hasPermissionToEditPropertiesOfResource(notDefaultDatabaseResType.getEntity());
				
		//Then
		Assert.assertTrue(result);
	}
	
	@Test
	public void hasPermissionToEditPropertiesOfResourceWhenUserIsAppDeveloperAndResourceTypeIsApplicationDefaultResTypeTest(){
		//Given
		//create APPLICATION resouceTypeEntity 
		ResourceTypeEntity applicationResTypeEntity = new ResourceTypeEntity();
		applicationResTypeEntity.setName(DefaultResourceTypeDefinition.APPLICATION.name());
		//wrap ReourceTypeEntity to ResourceType 
		ResourceType defaultApplicationResTyp = new ResourceType();
		defaultApplicationResTyp.wrap(applicationResTypeEntity);
		when(sessionContext.isCallerInRole(APP_DEVELOPER)).thenReturn(true);
		myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.A);
		myRoles.put(APP_DEVELOPER, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.EDIT_PROP_LIST_OF_INST_APP, res)));
		permissionService.rolesWithRestrictions = myRoles;
		
		//When
		boolean result = permissionService.hasPermissionToEditPropertiesOfResource(defaultApplicationResTyp.getEntity());
		
		//Then
		Assert.assertTrue(result);
	}
	
	@Test
	public void hasPermissionToEditPropertiesOfResourceWhenUserIsAppDeveloperAndResourceTypeIsApplicationServerDefaultResTypeTest(){
		//Given
		//Create APPLICATIONSERVER resourceTypeEntity
		ResourceTypeEntity applicationServerResTypEntity = new ResourceTypeEntity();
		applicationServerResTypEntity.setName(DefaultResourceTypeDefinition.APPLICATIONSERVER.name());
		//wrap ReourceTypeEntity to ResourceType 
		ResourceType defaultApplicationServerResTyp = new ResourceType();
		defaultApplicationServerResTyp.wrap(applicationServerResTypEntity);
		//end create resourceType
		when(sessionContext.isCallerInRole(APP_DEVELOPER)).thenReturn(true);
		myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.A);
		myRoles.put(APP_DEVELOPER, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.EDIT_PROP_LIST_OF_INST_APP, res)));
		permissionService.rolesWithRestrictions = myRoles;
		//When
		boolean result = permissionService.hasPermissionToEditPropertiesOfResource(defaultApplicationServerResTyp.getEntity());
				
		//Then
		Assert.assertFalse(result);
	}
	
	@Test
	public void hasPermissionToEditPropertiesOfResourceWhenUserIsAppDeveloperAndResourceTypeIsNotDefaultResTypeTest(){
		//Given
		//Create notDeafultResourceType
		ResourceTypeEntity databaseResTypeEntity = new ResourceTypeEntity();
		databaseResTypeEntity.setName("database");
		ResourceType notDefaultDatabaseResType = new ResourceType();
		notDefaultDatabaseResType.wrap(databaseResTypeEntity);;
		//End create notDefaultResource
		when(sessionContext.isCallerInRole(APP_DEVELOPER)).thenReturn(true);
		myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.A);
		myRoles.put(APP_DEVELOPER, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.EDIT_PROP_LIST_OF_INST_APP, res)));
		permissionService.rolesWithRestrictions = myRoles;
					
		//When
		boolean result = permissionService.hasPermissionToEditPropertiesOfResource(notDefaultDatabaseResType.getEntity());
				
		//Then
		Assert.assertFalse(result);
	}
	
	@Test
	public void hasPermissionToEditPropertiesOfResourceWhenResourceTypeIsNullTest(){
		//Given
		when(sessionContext.isCallerInRole(APP_DEVELOPER)).thenReturn(true);
		myRoles = new HashMap<>();
					
		//When
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.A);
		RestrictionDTO resDTO = new RestrictionDTOBuilder().mockRestrictionDTO(Permission.EDIT_PROP_LIST_OF_INST_APP, res);
		myRoles.put(APP_DEVELOPER, Arrays.asList(resDTO));
		permissionService.rolesWithRestrictions = myRoles;
				
		//Then
		Assert.assertNotNull(
				permissionService.rolesWithRestrictions.containsKey(APP_DEVELOPER));
		Assert.assertThat(permissionService.rolesWithRestrictions.get(APP_DEVELOPER),
				CoreMatchers.is(Arrays.asList(resDTO)));
		
		boolean result = permissionService.hasPermissionToEditPropertiesOfResource(null);
		Assert.assertFalse(result);
	}
	
	@Test
	public void hasPermissionToEditPropertiesOfResourceWhenResourceTypeIsApplicationResTypeAndUserIsNotAppDeveloperTest(){
		//Given
		
		//create APPLICATION resouceTypeEntity 
		ResourceTypeEntity applicationResTypeEntity = new ResourceTypeEntity();
		applicationResTypeEntity.setName(DefaultResourceTypeDefinition.APPLICATION.name());
		//wrap ReourceTypeEntity to ResourceType 
		ResourceType defaultApplicationResTyp = new ResourceType();
		defaultApplicationResTyp.wrap(applicationResTypeEntity);
		//end create resourceType
		when(sessionContext.isCallerInRole(VIEWER)).thenReturn(true);
		myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.A);
		myRoles.put(VIEWER, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.DEFAULT, res)));
		permissionService.rolesWithRestrictions = myRoles;
					
		//When
		boolean result = permissionService.hasPermissionToEditPropertiesOfResource(defaultApplicationResTyp.getEntity());
				
		//Then		
		Assert.assertFalse(result);
	}
	
	@Test
	public void hasPermissionToRenameResourceWhenUserIsConfigAdminAndResourceIsInstanceOfApplicationResTypeTest(){
		//Given
		//create APPLICATION resouceTypeEntity 
		ResourceTypeEntity applicationResTypeEntity = new ResourceTypeEntity();
		applicationResTypeEntity.setName(DefaultResourceTypeDefinition.APPLICATION.name());
		//wrap ReourceTypeEntity to ResourceType 
		ResourceType defaultApplicationResTyp = new ResourceType();
		defaultApplicationResTyp.wrap(applicationResTypeEntity);
		//Add resource to ResourceType
		ResourceEntity instanceResOfApplication =  ResourceFactory.createNewResource("instanceApp");
		instanceResOfApplication.setResourceType(applicationResTypeEntity);
		//end create resourceType
		when(sessionContext.isCallerInRole(CONFIG_ADMIN)).thenReturn(true);
		myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.A);
		myRoles.put(CONFIG_ADMIN, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.EDIT_RES_OR_RESTYPE_NAME, res)));
		permissionService.rolesWithRestrictions = myRoles;
		
		//When
		boolean result = permissionService.hasPermissionToRenameResource(instanceResOfApplication);
		
		//Then
		Assert.assertTrue(result);
	}
	
	
	@Test
	public void hasPermissionToRenameResourceWhenUserIsServerAdminAndResourceIsInstanceOfApplicationResTypeTest(){
		//Given
		//create APPLICATION resouceTypeEntity 
		ResourceTypeEntity applicationResTypeEntity = new ResourceTypeEntity();
		applicationResTypeEntity.setName(DefaultResourceTypeDefinition.APPLICATION.name());
		//wrap ReourceTypeEntity to ResourceType 
		ResourceType defaultApplicationResTyp = new ResourceType();
		defaultApplicationResTyp.wrap(applicationResTypeEntity);
		//Add resource to ResourceType
		ResourceEntity instanceResOfApplication =  ResourceFactory.createNewResource("instanceApp");
		instanceResOfApplication.setResourceType(applicationResTypeEntity);
		//end create resourceType
		when(sessionContext.isCallerInRole(SERVER_ADMIN)).thenReturn(true);
		myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.A);
		myRoles.put(SERVER_ADMIN, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.RENAME_INSTANCE_DEFAULT_RESOURCE, res)));
		permissionService.rolesWithRestrictions = myRoles;
							
		//When
		boolean result = permissionService.hasPermissionToRenameResource(instanceResOfApplication);
						
		//Then
		Assert.assertTrue(result);
	}
	
	@Test
	public void hasPermissionToRenameResourceWhenUserIsServerAdminAndResourceIsInstanceOfApplicationServerResTypeTest(){
		//Given
		//Create APPLICATIONSERVER resourceTypeEntity
		ResourceTypeEntity applicationServerResTypEntity = new ResourceTypeEntity();
		applicationServerResTypEntity.setName(DefaultResourceTypeDefinition.APPLICATIONSERVER.name());
		//wrap ReourceTypeEntity to ResourceType 
		ResourceType defaultApplicationServerResTyp = new ResourceType();
		defaultApplicationServerResTyp.wrap(applicationServerResTypEntity);
		//Add resource to ResourceType
		ResourceEntity appServerInstance =  ResourceFactory.createNewResource("Server");
		appServerInstance.setResourceType(applicationServerResTypEntity);
		//end create resourceType
		when(sessionContext.isCallerInRole(SERVER_ADMIN)).thenReturn(true);
		myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.A);
		myRoles.put(SERVER_ADMIN, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.RENAME_INSTANCE_DEFAULT_RESOURCE, res)));
		permissionService.rolesWithRestrictions = myRoles;
		
		//When
		boolean result = permissionService.hasPermissionToRenameResource(appServerInstance);
		
		//Then
		Assert.assertTrue(result);
	}
	
	@Test
	public void hasPermissionToRenameResourceWhenUserIsServerAdminAndResourceIsInstanceOfNodeResTypeTest(){
		//Given
		//Create NODE resourceTypeEntity
		ResourceTypeEntity nodeResTypeEntity = new ResourceTypeEntity();
		nodeResTypeEntity.setName(DefaultResourceTypeDefinition.NODE.name());
		//wrap ResourceTypeEntity to ResourceType
		ResourceType defaultNodeResType = new ResourceType();
		defaultNodeResType.wrap(nodeResTypeEntity);
		ResourceEntity nodeInstance =  ResourceFactory.createNewResource("Node");
		nodeInstance.setResourceType(nodeResTypeEntity);
		//end Create Node
		when(sessionContext.isCallerInRole(SERVER_ADMIN)).thenReturn(true);
		myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.A);
		myRoles.put(SERVER_ADMIN, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.RENAME_INSTANCE_DEFAULT_RESOURCE, res)));
		permissionService.rolesWithRestrictions = myRoles;
							
		//When
		boolean result = permissionService.hasPermissionToRenameResource(nodeInstance);
						
		//Then
		Assert.assertTrue(result);
	}
	
	@Test
	public void hasPermissionToRenameResourceWhenUserIsServerAdminAndResourceIsWithoutResTypeTest(){
		//Given
		//Create resource without resourceType
		ResourceEntity	resourceWithoutResourceType =  ResourceFactory.createNewResource("Orphan");
		//end Create resource without resourceType
				
		when(sessionContext.isCallerInRole(SERVER_ADMIN)).thenReturn(true);
		myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.A);
		myRoles.put(SERVER_ADMIN, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.RENAME_INSTANCE_DEFAULT_RESOURCE, res)));
		permissionService.rolesWithRestrictions = myRoles;
							
		//When
		boolean result = permissionService.hasPermissionToRenameResource(resourceWithoutResourceType);
						
		//Then
		Assert.assertFalse(result);
	}
	
	@Test
	public void hasPermissionToRenameResourceWhenUserIsViewerAndResourceIsInstanceOfApplicationResTypeTest(){
		//Given
		//create APPLICATION resouceTypeEntity 
		ResourceTypeEntity applicationResTypeEntity = new ResourceTypeEntity();
		applicationResTypeEntity.setName(DefaultResourceTypeDefinition.APPLICATION.name());
		//wrap ReourceTypeEntity to ResourceType 
		ResourceType defaultApplicationResTyp = new ResourceType();
		defaultApplicationResTyp.wrap(applicationResTypeEntity);
		//Add resource to ResourceType
		ResourceEntity instanceResOfApplication =  ResourceFactory.createNewResource("instanceApp");
		instanceResOfApplication.setResourceType(applicationResTypeEntity);
		//end create resourceType
		when(sessionContext.isCallerInRole(VIEWER)).thenReturn(true);
		myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.A);
		myRoles.put(VIEWER, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.DEFAULT, res)));
		permissionService.rolesWithRestrictions = myRoles;
							
		//When
		boolean result = permissionService.hasPermissionToRenameResource(instanceResOfApplication);
						
		//Then
		Assert.assertFalse(result);
		
	}
	
	
	@Test
	public void hasPermissionToRemoveDefaultInstanceOfResTypeWhenUserIsConfigAdmin(){
		//Given
		when(sessionContext.isCallerInRole(CONFIG_ADMIN)).thenReturn(true);
		myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.A);
		myRoles.put(CONFIG_ADMIN, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.DELETE_RES, res)));
		permissionService.rolesWithRestrictions = myRoles;
		//When
		boolean result = permissionService.hasPermissionToRemoveDefaultInstanceOfResType(true);
		//Then
		Assert.assertTrue(result);
	}
	
	@Test
	public void hasPermissionToRemoveDefaultInstanceOfResTypeWhenUserIsServerAdminAndIsNotDefaultResourceType(){
		//Given
		when(sessionContext.isCallerInRole(SERVER_ADMIN)).thenReturn(true);
		myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.A);
		myRoles.put(SERVER_ADMIN, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.DELETE_RES_INSTANCE_OF_DEFAULT_RESTYPE, res)));
		permissionService.rolesWithRestrictions = myRoles;
		//When
		boolean result = permissionService.hasPermissionToRemoveDefaultInstanceOfResType(false);
		//Then
		Assert.assertFalse(result);
	}
	
	@Test
	public void hasPermissionToRemoveDefaultInstanceOfResTypeWhenUserIsServerAdminAndIsDefaultResourceType(){
		//Given
		when(sessionContext.isCallerInRole(SERVER_ADMIN)).thenReturn(true);
		myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.A);
		myRoles.put(SERVER_ADMIN, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.DELETE_RES_INSTANCE_OF_DEFAULT_RESTYPE, res)));
		permissionService.rolesWithRestrictions = myRoles;
		//When
		boolean result = permissionService.hasPermissionToRemoveDefaultInstanceOfResType(true);
		//Then
		Assert.assertTrue(result);
	}

    @Test
    public void hasPermissionToEditPropertiesByResOrResTypeWhenUserIsConfigAdmin() {
        //Given
        ResourceEntity app = resourceEntityBuilder.mockApplicationEntity("app", null, null);

        when(sessionContext.isCallerInRole(CONFIG_ADMIN)).thenReturn(true);
        myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.A);
		myRoles.put(CONFIG_ADMIN, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.EDIT_ALL_PROPERTIES, res)));
        permissionService.rolesWithRestrictions = myRoles;

        //When
        boolean result = permissionService.hasPermissionToEditPropertiesByResource(app, false);

        //Then
        Assert.assertTrue(result);
    }

    @Test
	public void hasPermissionToEditPropertiesByResOrResTypeWhenUserIsAppDeveloperAndResourceIsInstanceOfApplicationResType(){
		//Given
        ResourceEntity app = resourceEntityBuilder.mockApplicationEntity("app", null, null);

		when(sessionContext.isCallerInRole(APP_DEVELOPER)).thenReturn(true);
		myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.A);
		myRoles.put(APP_DEVELOPER, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.EDIT_PROP_LIST_OF_INST_APP, res)));
		permissionService.rolesWithRestrictions = myRoles;

		//When
		boolean result = permissionService.hasPermissionToEditPropertiesByResource(app, false);

		//Then
		Assert.assertTrue(result);
	}

    @Test
    public void hasPermissionToEditPropertiesByResOrResTypeWhenUserIsAppDeveloperAndResourceIsInstanceOfApplicationServerResType() {
        //Given
        ResourceEntity as = resourceEntityBuilder.mockAppServerEntity("as", null, null, null);

        when(sessionContext.isCallerInRole(APP_DEVELOPER)).thenReturn(true);
        myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.A);
		myRoles.put(APP_DEVELOPER, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.EDIT_PROP_LIST_OF_INST_APP, res)));
        permissionService.rolesWithRestrictions = myRoles;

        //When
        boolean result = permissionService.hasPermissionToEditPropertiesByResource(as, false);

        //Then
        Assert.assertFalse(result);
    }

    @Test
    public void hasPermissionToEditPropertiesByResOrResTypeWhenUserIsShakedownAdminAndIsNotTestingMode() {
        //Given
        ResourceEntity as = resourceEntityBuilder.mockAppServerEntity("as", null, null, null);

        when(sessionContext.isCallerInRole(SHAKEDOWN_ADMIN)).thenReturn(true);
        myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.A);
		myRoles.put(SHAKEDOWN_ADMIN, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.SHAKEDOWN_TEST_MODE, res)));
        permissionService.rolesWithRestrictions = myRoles;

        //When
        boolean result = permissionService.hasPermissionToEditPropertiesByResource(as, false);

        //Then
        Assert.assertFalse(result);
    }

    @Test
    public void hasPermissionToEditPropertiesByResOrResTypeWhenUserIsShakedownAdminAndIsTestingMode() {
        //Given
        ResourceEntity as = resourceEntityBuilder.mockAppServerEntity("as", null, null, null);

        when(sessionContext.isCallerInRole(SHAKEDOWN_ADMIN)).thenReturn(true);
        myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.A);
		myRoles.put(SHAKEDOWN_ADMIN, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.SHAKEDOWN_TEST_MODE, res)));
        permissionService.rolesWithRestrictions = myRoles;

        //When
        boolean result = permissionService.hasPermissionToEditPropertiesByResource(as, true);

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
		res.setAction(Action.A);
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
		res.setAction(Action.A);
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
		res.setAction(Action.A);
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
		res.setAction(Action.A);
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
		res.setAction(Action.A);
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
		res.setAction(Action.A);
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
		res.setAction(Action.A);
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
		res.setAction(Action.A);
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
		res.setAction(Action.A);
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
		res.setAction(Action.A);
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
		res.setAction(Action.A);
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
		res.setAction(Action.A);
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
		res.setAction(Action.A);
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
		res.setAction(Action.A);
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
		res.setAction(Action.A);
		myRoles.put(SHAKEDOWN_ADMIN, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.SHAKEDOWN_TEST_MODE, res)));
        permissionService.rolesWithRestrictions = myRoles;

        //When
        boolean result = permissionService.hasPermissionToTemplateModify(as, true);

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
		res.setAction(Action.A);
		myRoles.put(VIEWER, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.DEFAULT, res)));
        permissionService.rolesWithRestrictions = myRoles;

        //When
        boolean result = permissionService.hasPermissionToTemplateModify(as, true);

        //Then
        Assert.assertFalse(result);
    }


    @Test
    public void hasPermissionToTemplateModifyWhenUserIsConfigAdminAndIsNotTestingMode() {
        //Given
        ResourceEntity as = resourceEntityBuilder.mockAppServerEntity("as", null, null, null);

        when(sessionContext.isCallerInRole(CONFIG_ADMIN)).thenReturn(true);
        myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.A);
		myRoles.put(CONFIG_ADMIN, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.SAVE_RESTYPE_TEMPLATE, res),
				new RestrictionDTOBuilder().mockRestrictionDTO(Permission.EDIT_RESTYPE_TEMPLATE, res)));
        permissionService.rolesWithRestrictions = myRoles;

        //When
        boolean result = permissionService.hasPermissionToTemplateModify(as, false);

        //Then
        Assert.assertTrue(result);
    }

    @Test
    public void hasPermissionToTemplateModifyWhenUserIsAppDeveloperAndIsApplicationResTypeAndIsNotTestingMode() {
        //Given
        ResourceEntity app = resourceEntityBuilder.mockApplicationEntity("app", null, null);

        when(sessionContext.isCallerInRole(APP_DEVELOPER)).thenReturn(true);
        myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.A);
		myRoles.put(APP_DEVELOPER, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.SAVE_RES_TEMPLATE, res)));
        permissionService.rolesWithRestrictions = myRoles;

        //When
        boolean result = permissionService.hasPermissionToTemplateModify(app, false);

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
		res.setAction(Action.A);
		myRoles.put(SHAKEDOWN_ADMIN, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.SHAKEDOWN_TEST_MODE, res)));
        permissionService.rolesWithRestrictions = myRoles;

        //When
        boolean result = permissionService.hasPermissionToTemplateModify(as, false);

        //Then
        Assert.assertFalse(result);
    }

	
	List<RoleEntity> deployableRoles;
	@Test
	public void hasPermissionToDeployWhenRoleIsNotDeployable(){
		//given
		RoleEntity roleToDeployEnvC = new RoleEntity();
		roleToDeployEnvC.setName(TEST_DEPLOYER);
		roleToDeployEnvC.setDeployable(true);
		
		when(sessionContext.isCallerInRole(ROLE_NOT_DEPLOY)).thenReturn(true);
		
		deployableRoles = new ArrayList<RoleEntity>();
		deployableRoles.add(roleToDeployEnvC);
		
		permissionService.deployableRoles = deployableRoles;
		
		//When
		boolean result = permissionService.hasPermissionToDeploy();

		//then
		Assert.assertFalse(result);
	}
	
	@Test
	public void hasPermissionToDeployWhenEmptyList(){
		//given
		List<RoleEntity>deployableRoles = new ArrayList<RoleEntity>();
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
	public void hasPermissionToDeployWhenUserIsDeployable(){
		//given
		List<RoleEntity>deployableRoles = new ArrayList<RoleEntity>();
		RoleEntity deployableRole = new RoleEntity();
		deployableRole.setName(TEST_DEPLOYER);
		deployableRoles.add(deployableRole);
		EntityManager entityManager = Mockito.mock(EntityManager.class);

		Query value = Mockito.mock(Query.class);
		when(entityManager.createQuery("from RoleEntity r where r.deployable=1")).thenReturn(value);
		when(value.getResultList()).thenReturn(deployableRoles);

		
		when(sessionContext.isCallerInRole(Mockito.anyString())).thenAnswer(new Answer<Boolean>() {

			@Override
			public Boolean answer(InvocationOnMock invocation) throws Throwable {
				Object[] arguments = invocation.getArguments();
				if(arguments[0].equals(TEST_DEPLOYER)){
					return true;
				}else{
					return false;
				}
				
			}
		});
		
		//When
		boolean result = permissionService.hasPermissionToDeploy();
		//then	
		Assert.assertTrue(result);
	}
	
	//TODO: controllare
	@Test
	public void hasPermissionToDeployWhenRoleIsDeployable(){
		//given
		RoleEntity roleToDeployEnvC = new RoleEntity();
		roleToDeployEnvC.setName(TEST_DEPLOYER);
		roleToDeployEnvC.setDeployable(true);
		when(sessionContext.isCallerInRole(TEST_DEPLOYER)).thenReturn(true);
		deployableRoles = new ArrayList<>();
		deployableRoles.add(roleToDeployEnvC);
		permissionService.deployableRoles = deployableRoles;
		
		//When
		boolean result = permissionService.hasPermissionToDeploy();
		//then
		Assert.assertTrue(result);
	}
	
	@Test
	public void hasPermissionToDeployEnvCTest(){
		//given
		RoleEntity roleToDeployEnvC = new RoleEntity();
		roleToDeployEnvC.setName(TEST_DEPLOYER);
		roleToDeployEnvC.setDeployable(true);
		PermissionEntity permissionToDeployC = new PermissionEntity();
		permissionToDeployC.setValue("C");
		roleToDeployEnvC.getPermissions().add(permissionToDeployC);
		when(sessionContext.isCallerInRole(TEST_DEPLOYER)).thenReturn(true);
		myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.A);
		myRoles.put(TEST_DEPLOYER, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(permissionToDeployC, res)));
		permissionService.rolesWithRestrictions = myRoles;
		
		//When
		boolean result = permissionService.hasPermission(permissionToDeployC.getValue());

		//Then
		Assert.assertTrue(result);
	}
	@Test
	public void hasNotPermissionToDeployEnvZTest(){
		//given
		//Create deployable role
		RoleEntity roleToDeployEnvC = new RoleEntity();
		roleToDeployEnvC.setName(TEST_DEPLOYER);
		roleToDeployEnvC.setDeployable(true);
		//Create permission Z and not assign to deployable role
		PermissionEntity permissionToDeployZ = new PermissionEntity();
		permissionToDeployZ.setValue("Z");
		//End create permissin Z
		//Create Permission "C" and assign to role
		PermissionEntity permissionToDeployC = new PermissionEntity();
		permissionToDeployC.setValue("C");
		roleToDeployEnvC.getPermissions().add(permissionToDeployC);
		//End create permission "C"
		when(sessionContext.isCallerInRole(TEST_DEPLOYER)).thenReturn(true);
		myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.A);
		myRoles.put(TEST_DEPLOYER, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(permissionToDeployC, res)));
		permissionService.rolesWithRestrictions = myRoles;
		//When
		boolean result = permissionService.hasPermission(permissionToDeployZ.getValue());

		//Then
		Assert.assertFalse(result);
	}
	
	@Test
	public void hasPermissionToDeployEnvCAndHasNotPermissionToDeployEnvZ(){
		//given
		RoleEntity roleToDeployEnvC = new RoleEntity();
		roleToDeployEnvC.setName(TEST_DEPLOYER);
		roleToDeployEnvC.setDeployable(true);
		PermissionEntity permissionToDeployC = new PermissionEntity();
		permissionToDeployC.setValue("C");
		roleToDeployEnvC.getPermissions().add(permissionToDeployC);
		
		PermissionEntity permissionToDeployZ = new PermissionEntity();
		permissionToDeployZ.setValue("Z");
	
		when(sessionContext.isCallerInRole(TEST_DEPLOYER)).thenReturn(true);
		myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.A);
		myRoles.put(TEST_DEPLOYER, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(permissionToDeployC, res)));
		permissionService.rolesWithRestrictions = myRoles;
		//When
		boolean canDeploy = permissionService.hasPermission(permissionToDeployC.getValue());
		boolean canNotDeploy = permissionService.hasPermission(permissionToDeployZ.getValue());

		//Then
		Assert.assertTrue(canDeploy);
		Assert.assertFalse(canNotDeploy);
	}


	@Test
	public void hasPermissionToTemplateModifyResourceTypeWhenUserIsShakedownAdminAndIsTestingMode() {
		//Given
		ResourceTypeEntity as = ResourceTypeEntityBuilder.APPLICATION_SERVER_TYPE;

		when(sessionContext.isCallerInRole(SHAKEDOWN_ADMIN)).thenReturn(true);
		myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.A);
		myRoles.put(SHAKEDOWN_ADMIN, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.SHAKEDOWN_TEST_MODE, res)));
		permissionService.rolesWithRestrictions = myRoles;

		//When
		boolean result = permissionService.hasPermissionToTemplateModify(as, true);

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
		res.setAction(Action.A);
		myRoles.put(VIEWER, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.DEFAULT, res)));
		permissionService.rolesWithRestrictions = myRoles;

		//When
		boolean result = permissionService.hasPermissionToTemplateModify(as, true);

		//Then
		Assert.assertFalse(result);
	}


	@Test
	public void hasPermissionToTemplateModifyResourceTypeWhenUserIsConfigAdminAndIsNotTestingMode() {
		//Given
		ResourceTypeEntity as = ResourceTypeEntityBuilder.APPLICATION_SERVER_TYPE;

		when(sessionContext.isCallerInRole(CONFIG_ADMIN)).thenReturn(true);
		myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.A);
		myRoles.put(CONFIG_ADMIN, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.SAVE_RESTYPE_TEMPLATE, res),
				new RestrictionDTOBuilder().mockRestrictionDTO(Permission.EDIT_RESTYPE_TEMPLATE, res)));
		permissionService.rolesWithRestrictions = myRoles;

		//When
		boolean result = permissionService.hasPermissionToTemplateModify(as, false);

		//Then
		Assert.assertTrue(result);
	}

	@Test
	public void hasPermissionToTemplateModifyResourceTypeWhenUserIsAppDeveloperAndIsApplicationResTypeAndIsNotTestingMode() {
		//Given
		ResourceTypeEntity app = ResourceTypeEntityBuilder.APPLICATION_TYPE;

		when(sessionContext.isCallerInRole(APP_DEVELOPER)).thenReturn(true);
		myRoles = new HashMap<>();
		RestrictionEntity res = new RestrictionEntity();
		res.setAction(Action.A);
		myRoles.put(APP_DEVELOPER, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.SAVE_RES_TEMPLATE, res)));
		permissionService.rolesWithRestrictions = myRoles;

		//When
		boolean result = permissionService.hasPermissionToTemplateModify(app, false);

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
		res.setAction(Action.A);
		myRoles.put(SHAKEDOWN_ADMIN, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.SHAKEDOWN_TEST_MODE, res)));
		permissionService.rolesWithRestrictions = myRoles;

		//When
		boolean result = permissionService.hasPermissionToTemplateModify(as, false);

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
		res.setAction(Action.A);
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
		res.setAction(Action.A);
		myRoles.put(CONFIG_ADMIN, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.EDIT_RES_OR_RESTYPE_NAME, res)));
		myRoles.put(SERVER_ADMIN, Arrays.asList(new RestrictionDTOBuilder().mockRestrictionDTO(Permission.EDIT_RES_OR_RESTYPE_NAME, res)));
		permissionService.rolesWithRestrictions = myRoles;
			
		//When
		boolean result = permissionService.hasPermission(Permission.EDIT_RES_OR_RESTYPE_NAME);
		
		//Then
		Assert.assertFalse(result);
	}
	
}