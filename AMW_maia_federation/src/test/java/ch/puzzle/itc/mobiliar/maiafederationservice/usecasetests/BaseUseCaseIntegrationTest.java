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

package ch.puzzle.itc.mobiliar.maiafederationservice.usecasetests;

import ch.puzzle.itc.mobiliar.business.environment.control.ContextDomainService;
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableOwner;
import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableOwnerViolationException;
import ch.puzzle.itc.mobiliar.business.function.entity.AmwFunctionEntity;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyDescriptorEntity;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyEntity;
import ch.puzzle.itc.mobiliar.business.releasing.entity.ReleaseEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.CopyResourceResult;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.ResourceTypeProvider;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.ResourcesScreenDomainService;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.*;
import ch.puzzle.itc.mobiliar.business.resourcerelation.control.ResourceRelationService;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ConsumedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ProvidedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ResourceRelationContextEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ResourceRelationTypeEntity;
import ch.puzzle.itc.mobiliar.business.template.entity.TemplateDescriptorEntity;
import ch.puzzle.itc.mobiliar.business.utils.ValidationException;
import ch.puzzle.itc.mobiliar.common.exception.AMWException;
import ch.puzzle.itc.mobiliar.common.exception.ElementAlreadyExistsException;
import ch.puzzle.itc.mobiliar.common.exception.ResourceNotFoundException;
import ch.puzzle.itc.mobiliar.common.util.DefaultResourceTypeDefinition;
import ch.puzzle.itc.mobiliar.maiafederationservice.utils.BaseIntegrationTest;

import javax.inject.Inject;
import javax.persistence.TypedQuery;
import java.util.*;

import static org.junit.Assert.*;

public abstract class BaseUseCaseIntegrationTest extends BaseIntegrationTest {

    protected static final String MAIN_RELEASE_PAST = "Past";

    protected static final String MINOR_RELEASE_16_05 = "ZW-16.05";
    protected static final String MINOR_RELEASE_16_06 = "ZW-16.06";
    protected static final String MINOR_RELEASE_16_11 = "ZW-16.11";
    protected static final String MINOR_RELEASE_16_12 = "ZW-16.12";


    protected static final String PUZZLE_SHOP_V_1 = "ch_mobi_testing_PuzzleShop_v1_default";
    protected static final String PUZZLE_BANK_V_1 = "ch_mobi_testing_PuzzleBank_v1_default";

    protected static final String PUZZLE_BANK_V_2 = "ch_mobi_testing_PuzzleBank_v2_default";

    protected static final String PUZZLE_BANK_V_1_PPI_PAYMENT = "ch.mobi.testing.PuzzleBank.v1.default#payment";
    protected static final String PUZZLE_BANK_V_1_PPI_CUSTOMER = "ch.mobi.testing.PuzzleBank.v1.default#customer";
    protected static final String PUZZLE_SHOP_V_1_CPI_PAYMENTSTUFF = "ch.mobi.testing.PuzzleShop.v1.default#paymentStuff";


    protected static final String PUZZLE_SHOP_PROPERTY = "puzzleShopProperty";
    protected static final String PUZZLE_BANK_PAYMENT_PROPERTY = "puzzleBankPaymentProperty";
    protected static final String PUZZLE_BANK_CUSTOMER_PROPERTY = "puzzleBankCustomerProperty";
    protected static final String PUZZLE_SHOP_PAYMENTSTUFF_PROPERTY = "puzzleShopPaymentStuffProperty";


    @Inject
    private ContextDomainService contextDomainService;

    @Inject
    private ResourceRelationService resourceRelationService;

    @Inject
    private ResourcesScreenDomainService resourcesScreenDomainService;

    @Inject
    private ResourceTypeProvider resourceTypeProvider;


    protected void createMinorReleasesForResource(String resourceName, String... minorReleases) throws ForeignableOwnerViolationException, AMWException {
        List<ResourceEntity> allPuzzleShopResources = getResourceByName(resourceName);
        ResourceEntity puzzleShop = allPuzzleShopResources.get(0);

        for (String release : minorReleases) {
            CopyResourceResult copyResult = copyResourceDomainService.createReleaseFromOriginResource(puzzleShop, findReleaseByName(release), ForeignableOwner.AMW);
            assertTrue(copyResult.isSuccess());
        }

        allPuzzleShopResources = getResourceByName(resourceName);
        assertEquals(minorReleases.length + 1, allPuzzleShopResources.size());
    }


    protected void verifyNumberReleasesFor(String resourceName, int expectedReleases) {
        List<ResourceEntity> apps = getResourceByName(resourceName);
        assertNotNull(apps);
        assertEquals(expectedReleases, apps.size());
    }

    protected void decorateAMWTemplateOnResourceForRelease(String resourceName, String releaseName) {
        TemplateDescriptorEntity template = createTemplate("template_" + resourceName + "_" + releaseName, "path", "content");
        ResourceEntity resource = getResourceByNameAndRelease(resourceName, releaseName);

        addTemplate(resource, template);
    }

    private TemplateDescriptorEntity createTemplate(String name, String path, String content) {
        TemplateDescriptorEntity template = new TemplateDescriptorEntity();
        template.setFileContent(content);
        template.setName(name);
        template.setTargetPath(path);
        template.setTesting(false);

        TypedQuery<ResourceGroupEntity> createQuery = entityManager.createNamedQuery(ResourceGroupEntity.ALLRESOURCESBYTYPE_QUERY, ResourceGroupEntity.class).setParameter("restype", ResourceTypeEntity.RUNTIME);
        Set<ResourceGroupEntity> platforms = new HashSet<>(createQuery.getResultList());
        template.setTargetPlatforms(platforms);
        return template;
    }

    private void addTemplate(ResourceEntity resource, TemplateDescriptorEntity template) {
        ContextEntity context = contextDomainService.getGlobalResourceContextEntity();
        ResourceContextEntity resourceContextEntity = resource.getOrCreateContext(context);
        resourceContextEntity.addTemplate(template);

        entityManager.persist(resourceContextEntity);
        entityManager.persist(resource);
    }

    protected void decorateAMWFunctionOnResourceForRelease(String resourceName, String releaseName) {
        ResourceEntity resource = getResourceByNameAndRelease(resourceName, releaseName);
        AmwFunctionEntity function = new AmwFunctionEntity();
        function.setName("function_" + resourceName + "_" + releaseName);
        function.setResource(resource);
        function.setImplementation("implementation");
        resource.addFunction(function);
        entityManager.persist(function);
    }

    protected void decorateConsumedRelation(String resourceName, String resourceReleaseName, String consumedResourceName, String consumedResourceReleaseName) throws ResourceNotFoundException, ElementAlreadyExistsException {
        ResourceEntity resource = getResourceByNameAndRelease(resourceName, resourceReleaseName);
        ResourceEntity relatedResource = getResourceByNameAndRelease(consumedResourceName, consumedResourceReleaseName);
        resourceRelationService.doAddResourceRelationForSpecificRelease(resource.getId(), relatedResource.getResourceGroup().getId(), false, null, null, null, ForeignableOwner.AMW);
    }

    protected void decorateProvidedRelation(String resourceName, String resourceReleaseName, String providedResourceName, String providedResourceReleaseName) throws ResourceNotFoundException, ElementAlreadyExistsException {
        ResourceEntity resource = getResourceByNameAndRelease(resourceName, resourceReleaseName);
        ResourceEntity relatedResource = getResourceByNameAndRelease(providedResourceName, providedResourceReleaseName);
        resourceRelationService.doAddResourceRelationForSpecificRelease(resource.getId(), relatedResource.getResourceGroup().getId(), true, null, null, null, ForeignableOwner.AMW);
    }

    protected void decorateAMWPropertyOnResourceForRelease(String resourceName, String releaseName) throws ValidationException {
        decorateAMWPropertyOnResourceForRelease(resourceName, releaseName, buildPropertyNameFor(resourceName, releaseName));
    }

    protected void decorateAMWPropertyOnResourceForRelease(String resourceName, String releaseName, String propertyName) throws ValidationException {
        ResourceEntity resource = getResourceByNameAndRelease(resourceName, releaseName);
        ResourceContextEntity context = resource.getOrCreateContext(contextService.getGlobalResourceContextEntity());

        PropertyDescriptorEntity propertyDescriptorEntity = new PropertyDescriptorEntity();
        propertyDescriptorEntity.setPropertyName(propertyName);

        context.addPropertyDescriptor(propertyDescriptorEntity);

        entityManager.persist(propertyDescriptorEntity);
    }

    protected void updatePropertyValueOnConsumedRelationForRelease(String resourceName, String releaseName, String relatedResourceName, String relatedReleaseName, String propertyName, String value) throws ValidationException {
        ResourceEntity resource = getResourceByNameAndRelease(resourceName, releaseName);
        ResourceEntity consumedResource = getResourceByNameAndRelease(relatedResourceName, relatedReleaseName);
        List<ConsumedResourceRelationEntity> consumedSlaveRelations = resourceRelationService.getConsumedSlaveRelations(consumedResource);
        for (ConsumedResourceRelationEntity consumedRelation : consumedSlaveRelations) {
            if (consumedRelation.getMasterResource().equals(resource)) {
                ResourceRelationContextEntity context = consumedRelation.getOrCreateContext(contextService.getGlobalResourceContextEntity());
                ResourceContextEntity consumedResourceContext = consumedResource.getOrCreateContext(contextService.getGlobalResourceContextEntity());
                for (PropertyDescriptorEntity propertyDesc : consumedResourceContext.getPropertyDescriptors()) {
                    if (propertyName.equals(propertyDesc.getPropertyName())) {
                        propertyValueService.setPropertyValue(context, propertyDesc.getId(), value);
                        entityManager.merge(context);
                        return;
                    }
                }
            }
        }
    }

    protected void updatePropertyValueOnProvidedRelationForRelease(String resourceName, String releaseName, String relatedResourceName, String relatedReleaseName, String propertyName, String value) throws ValidationException {
        ResourceEntity resource = getResourceByNameAndRelease(resourceName, releaseName);
        ResourceEntity providedResource = getResourceByNameAndRelease(relatedResourceName, relatedReleaseName);
        List<ProvidedResourceRelationEntity> providedSlaveRelations = resourceRelationService.getProvidedSlaveRelations(providedResource);


        for (ProvidedResourceRelationEntity providedRelation : providedSlaveRelations) {
            if (providedRelation.getMasterResource().equals(resource)) {
                ResourceRelationContextEntity context = providedRelation.getOrCreateContext(contextService.getGlobalResourceContextEntity());

                ResourceContextEntity providedResourceContext = providedResource.getOrCreateContext(contextService.getGlobalResourceContextEntity());

                for (PropertyDescriptorEntity propertyDesc : providedResourceContext.getPropertyDescriptors()) {
                    if (propertyName.equals(propertyDesc.getPropertyName())) {
                        propertyValueService.setPropertyValue(context, propertyDesc.getId(), value);
                        entityManager.merge(context);
                        return;
                    }
                }
            }
        }
    }


    protected String buildPropertyNameFor(String resourceName, String releaseName) {
        return "property_" + resourceName + "_" + releaseName;
    }

    protected String buildPropertyValueFor(String resourceName, String releaseName) {
        return "property_value_" + resourceName + "_" + releaseName;
    }

    protected void updatePropertyValue(String resourceName, String releaseName, String propertyName, String value) throws ch.puzzle.itc.mobiliar.business.utils.ValidationException {
        ResourceEntity resource = getResourceByNameAndRelease(resourceName, releaseName);
        updatePropertyValue(resource, propertyName, value);
    }

    protected void updatePropertyValue(ResourceEntity app, String propertyName, String value) throws ch.puzzle.itc.mobiliar.business.utils.ValidationException {
        ResourceContextEntity context = app.getOrCreateContext(contextService.getGlobalResourceContextEntity());

        for (PropertyDescriptorEntity propertyDesc : context.getPropertyDescriptors()) {
            if (propertyName.equals(propertyDesc.getPropertyName())) {
                propertyValueService.setPropertyValue(context, propertyDesc.getId(), value);
                entityManager.merge(context);
                return;
            }
        }
    }

    protected void verifyPropertyValuePresent(ResourceEntity resource, String propertyName, String value) {
        assertNotNull(resource);

        ResourceContextEntity context = resource.getOrCreateContext(contextService.getGlobalResourceContextEntity());
        if (context.getProperties() != null) {
            for (PropertyEntity property : context.getProperties()) {
                if (property.getDescriptor().getPropertyName().equals(propertyName)) {
                    assertEquals(property.getValue(), value);
                    return;
                }
            }
        }
        fail("expected property not found");

    }

    protected void verifyPropertyValuePresent(String resourceName, String releaseName, String propertyName, String value) {
        ResourceEntity resource = getResourceByNameAndRelease(resourceName, releaseName);
        // verify that property descriptor is present
//        verifyPropertiesPresent(resource, buildPropertyNameFor(resourceName, releaseName));

        verifyPropertyValuePresent(resource, propertyName, value);
    }

    protected void verifyPropertyValueNotPresent(ResourceEntity resource, String propertyName) {
        assertNotNull(resource);

        ResourceContextEntity context = resource.getOrCreateContext(contextService.getGlobalResourceContextEntity());
        if (context.getProperties() != null && !context.getProperties().isEmpty()) {
            for (PropertyEntity property : context.getProperties()) {
                if (property.getDescriptor().getPropertyName().equals(propertyName)) {
                    fail("Value for property should not be set");
                }
            }
        } else {
            assertTrue("No property value present for descriptor", true);
        }
    }

    protected void verifyPropertyValueNotPresent(String resourceName, String releaseName, String propertyName) {
        ResourceEntity resource = getResourceByNameAndRelease(resourceName, releaseName);
        verifyPropertyValueNotPresent(resource, propertyName);
    }

    protected void verifyPropertiesPresentInAllReleasesFor(String resourceName, String... propertyNames) {
        List<ResourceEntity> apps = getResourceByName(resourceName);
        assertNotNull(apps);
        for (ResourceEntity app : apps) {
            verifyPropertiesPresent(app, propertyNames);
        }
    }

    private void verifyPropertiesPresent(ResourceEntity resource, String... propertyNames) {
        ResourceContextEntity context = resource.getOrCreateContext(contextService.getGlobalResourceContextEntity());
        Set<PropertyDescriptorEntity> propertyDescriptors = context.getPropertyDescriptors();

        if (propertyDescriptors != null) {
            assertEquals(propertyNames.length, propertyDescriptors.size());
            if (propertyNames.length != 0) {
                assertTrue(Arrays.asList(propertyNames).contains(context.getPropertyDescriptors().iterator().next().getPropertyName()));
            }
        } else {
            assertEquals(0, propertyNames.length);
        }
    }

    protected void verifyPropertiesPresentInResourceRelease(String resourceName, String releaseName, String... propertyNames) {
        ResourceEntity resource = getResourceByNameAndRelease(resourceName, releaseName);
        assertNotNull(resource);
        verifyPropertiesPresent(resource, propertyNames);
    }


    protected void verifyPropertiesPresentInConsumedRelation(String resourceName, String releaseName, String consumedResourceName, String consumedResourceReleaseName, String... propertyNames) {
        ResourceEntity resource = getResourceByNameAndRelease(resourceName, releaseName);
        assertNotNull(resource);
        Set<ConsumedResourceRelationEntity> consumedMasterRelations = resource.getConsumedMasterRelations();
        for (ConsumedResourceRelationEntity consumedRelation : consumedMasterRelations) {
            ResourceEntity consumedResource = consumedRelation.getSlaveResource();
            if (consumedResource.getName().equals(consumedResourceName) && consumedResource.getRelease().getName().equals(consumedResourceReleaseName)) {
                verifyPropertiesPresent(consumedResource, propertyNames);
            }
        }
    }

    protected void verifyPropertiesPresentInProvidedRelation(String resourceName, String releaseName, String providedResourceName, String providedResourceReleaseName, String... propertyNames) {
        ResourceEntity resource = getResourceByNameAndRelease(resourceName, releaseName);
        assertNotNull(resource);
        Set<ProvidedResourceRelationEntity> providedMasterRelations = resource.getProvidedMasterRelations();
        for (ProvidedResourceRelationEntity providedRelation : providedMasterRelations) {
            ResourceEntity providedResource = providedRelation.getSlaveResource();
            if (providedResource.getName().equals(providedResourceName) && providedResource.getRelease().getName().equals(providedResourceReleaseName)) {
                verifyPropertiesPresent(providedResource, propertyNames);
            }
        }
    }

    protected void verifyConsumedRelationsPresentInAllReleasesFor(String resourceName, String... consumedResourceNames) {
        List<ResourceEntity> apps = getResourceByName(resourceName);
        assertNotNull(apps);
        for (ResourceEntity app : apps) {
            verifyConsumedRelationsPresent(app, consumedResourceNames);
        }
    }

    protected void verifyResourcePresentInRelease(String resourceName, String releaseName) {
        ResourceEntity resource = getResourceByNameAndRelease(resourceName, releaseName);
        assertNotNull(resource);
    }

    protected void verifyConsumedRelationsPresentInResourceRelease(String resourceName, String releaseName, String... consumedResourceNames) {
        ResourceEntity resource = getResourceByNameAndRelease(resourceName, releaseName);
        assertNotNull(resource);

        verifyConsumedRelationsPresent(resource, consumedResourceNames);
    }

    private void verifyConsumedRelationsPresent(ResourceEntity resource, String... consumedResourceNames) {
        if (resource.getConsumedMasterRelations() != null) {
            List<ConsumedResourceRelationEntity> consumedMasterRelations = new ArrayList<>(resource.getConsumedMasterRelations());
            assertEquals(consumedResourceNames.length, consumedMasterRelations.size());

            // for (String consumedRelation : consumedResourceNames){
            for (ConsumedResourceRelationEntity relation : resource.getConsumedMasterRelations()) {
                if (Arrays.asList(consumedResourceNames).contains(relation.getSlaveResource().getName())) {
                    consumedMasterRelations.remove(relation);
                }
            }

            assertTrue(consumedMasterRelations.isEmpty());
        } else {
            assertEquals(0, consumedResourceNames.length);
        }
    }

    protected void verifyProvidedRelationsPresentInAllReleasesFor(String resourceName, String... providedResourceNames) {
        List<ResourceEntity> apps = getResourceByName(resourceName);
        assertNotNull(apps);
        for (ResourceEntity app : apps) {
            verifyProvidedRelationsPresent(app, providedResourceNames);
        }
    }

    protected void verifyProvidedRelationsPresentInResourceRelease(String resourceName, String releaseName, String... providedResourceNames) {
        ResourceEntity resource = getResourceByNameAndRelease(resourceName, releaseName);
        assertNotNull(resource);

        verifyProvidedRelationsPresent(resource, providedResourceNames);
    }

    private void verifyProvidedRelationsPresent(ResourceEntity resource, String... providedResourceNames) {

        if (resource.getProvidedMasterRelations() != null) {
            List<ProvidedResourceRelationEntity> providedMasterRelations = new ArrayList<>(resource.getProvidedMasterRelations());
            assertEquals(providedResourceNames.length, providedMasterRelations.size());

            // for (String consumedRelation : consumedResourceNames){
            for (ProvidedResourceRelationEntity relation : resource.getProvidedMasterRelations()) {
                if (Arrays.asList(providedResourceNames).contains(relation.getSlaveResource().getName())) {
                    providedMasterRelations.remove(relation);
                }
            }

            assertTrue(providedMasterRelations.isEmpty());
        } else {
            assertEquals(0, providedResourceNames.length);
        }
    }

    protected void createAppserver(String name, String... releaseNames) throws AMWException, ForeignableOwnerViolationException {
        List<ReleaseEntity> releases = createReleasesFromNames(releaseNames);

        ResourceTypeEntity appserverType = resourceTypeProvider.getFromDB(DefaultResourceTypeDefinition.APPLICATIONSERVER.name());
        Resource appserver = null;
        if (!releases.isEmpty()){
            // create as for first release
            ReleaseEntity firstRelease = releases.remove(0);
            appserver = resourcesScreenDomainService.createNewResourceByName(ForeignableOwner.AMW, name, appserverType.getId(), firstRelease.getId());

            for(ReleaseEntity followingRelease : releases){
                // create release for following releases of appserver
                copyResourceDomainService.createReleaseFromOriginResource(appserver.getEntity(), followingRelease, ForeignableOwner.AMW);
            }
        }

    }

    private List<ReleaseEntity> createReleasesFromNames(String[] releaseNames) {
        List<ReleaseEntity> releases = new ArrayList<>();

        for (String releaseName : releaseNames) {
            releases.add(findReleaseByName(releaseName));
        }

        Collections.sort(releases);

        return releases;
    }

    protected void addApplicationToAppServer(String applicationServerName, String asReleaseName, String applicationName, String appReleaseName) throws ResourceNotFoundException, ElementAlreadyExistsException {
        ResourceEntity asResource = getResourceByNameAndRelease(applicationServerName, asReleaseName);
        ResourceEntity appResource = getResourceByNameAndRelease(applicationName, appReleaseName);

        ResourceTypeEntity appserverType = resourceTypeProvider.getFromDB(DefaultResourceTypeDefinition.APPLICATIONSERVER.name());
        ResourceTypeEntity appType = resourceTypeProvider.getFromDB(DefaultResourceTypeDefinition.APPLICATION.name());
        ResourceRelationTypeEntity resRelType = resourceTypeProvider.getResourceRelationTypeIfAvailableIncludingParents(appserverType, appType, null);

        ConsumedResourceRelationEntity consumedResourceRelationEntity = asResource.addConsumedResourceRelation(appResource, resRelType, 1, ForeignableOwner.AMW);
        entityManager.persist(consumedResourceRelationEntity);
        Set<ConsumedResourceRelationEntity> relations = new HashSet<>();
        relations.add(consumedResourceRelationEntity);
        appResource.setConsumedSlaveRelations(relations);
        entityManager.persist(appResource);

    }

}
