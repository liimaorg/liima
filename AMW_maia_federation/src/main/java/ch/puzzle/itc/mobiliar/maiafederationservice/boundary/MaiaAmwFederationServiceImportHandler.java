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

package ch.puzzle.itc.mobiliar.maiafederationservice.boundary;

import ch.mobi.xml.datatype.ch.mobi.maia.amw.maiaamwfederation.v1_0.*;
import ch.mobi.xml.datatype.ch.mobi.maia.amw.maiaamwfederationservicetypes.v1_0.Message;
import ch.mobi.xml.datatype.ch.mobi.maia.amw.maiaamwfederationservicetypes.v1_0.ProcessingState;
import ch.puzzle.itc.mobiliar.business.environment.control.ContextDomainService;
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableOwner;
import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableOwnerViolationException;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.ResourceDependencyResolverService;
import ch.puzzle.itc.mobiliar.business.property.control.PropertyDescriptorService;
import ch.puzzle.itc.mobiliar.business.property.control.PropertyImportService;
import ch.puzzle.itc.mobiliar.business.property.control.PropertyTypeScreenDomainService;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyDescriptorEntity;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyTypeEntity;
import ch.puzzle.itc.mobiliar.business.releasing.control.ReleaseMgmtPersistenceService;
import ch.puzzle.itc.mobiliar.business.releasing.entity.ReleaseEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.ResourceBoundary;
import ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.ResourceLocator;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.*;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceContextEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.control.RelationImportService;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.AbstractResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ConsumedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ProvidedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.softlinkRelation.control.SoftlinkRelationService;
import ch.puzzle.itc.mobiliar.business.softlinkRelation.entity.SoftlinkRelationEntity;
import ch.puzzle.itc.mobiliar.common.exception.AMWException;
import ch.puzzle.itc.mobiliar.common.exception.AMWRuntimeException;
import ch.puzzle.itc.mobiliar.common.exception.ElementAlreadyExistsException;
import ch.puzzle.itc.mobiliar.common.exception.ResourceNotFoundException;
import ch.puzzle.itc.mobiliar.maiafederationservice.entity.ResourceHelper;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import static ch.mobi.xml.datatype.common.commons.v3.MessageSeverity.INFO;

@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
public class MaiaAmwFederationServiceImportHandler {

    public static final ForeignableOwner FEDERATION_SERVICE_IMPORT_OWNER = ForeignableOwner.MAIA;
    public static final String MAIA_PROPERTY_TYPE = "MAIAPropertyType";

    @Inject
    private ResourcesScreenDomainService resourcesService;

    @Inject
    private ResourceBoundary resourceBoundary;

    @Inject
    private ReleaseMgmtPersistenceService releaseService;

    @Inject
    private PropertyDescriptorService descriptorService;


    @Inject
    private ContextDomainService contextDomainService;

    @Inject
    private ResourceTypeDomainService resourceTypeService;


    @Inject
    private RelationImportService relationImportService;

    @Inject
    private PropertyImportService propertyImportService;

    @Inject
    ResourceRepository resourceRepository;

    @Inject
    private ResourceLocator resourceLocator;

    @Inject
    private PropertyTypeScreenDomainService propertyTypeScreenDomainService;

    @Inject
    SoftlinkRelationService softlinkRelationService;

    @Inject
    private Logger log;

    @Inject
    private ResourceImportService resourceImportService;

    @Inject
    private CopyResourceDomainService copyResourceDomainService;

    @Inject
    private ResourceDependencyResolverService resourceDependencyResolverService;

    // TODO Remove member on a Stateless Bean
    PropertyTypeEntity maiaPropertyType;

    @PostConstruct
    protected void initialize() {
        this.maiaPropertyType = propertyTypeScreenDomainService.getPropertyTypeByName(MAIA_PROPERTY_TYPE);
    }

    // 0
    public ResourceHelper handleUpdateAggregate(Application app) {
        log.info("Start importing aggregate for " + app.toString());
        List<ApplicationReleaseBinding> bindings = app.getStructure();

        ResourceHelper appResourceHelper = new ResourceHelper();

        for (ApplicationReleaseBinding binding : bindings) {
            ReleaseEntity re = releaseService.findByName(binding.getRelease());
            // conditio sine qua non: release must exist
            if (re == null) {
                log.warning("Release " + binding.getRelease() + " does not exist. Abort aggregate import");
                throw new AMWRuntimeException("Release " + binding.getRelease() + " does not exist.");
            }
            ResourceHelper resourceHelper = handleUpdateApplicationRelease(app, binding, re);
            appResourceHelper.addMessages(resourceHelper.getMessages());
            appResourceHelper.addResources(resourceHelper.getResources());
            appResourceHelper.setNouveau(resourceHelper.isNouveau());
        }

        appResourceHelper.setAppName(app.getId().getName());
        appResourceHelper.setProcessingState(ProcessingState.OK);
        appResourceHelper.setAppLink(buildAmwResourceLink(appResourceHelper.getResources(), app.getId().getName()));

        log.info("End importing aggregate for " + app.toString());

        return appResourceHelper;
    }

    /**
     * Do not use this method - it should be private but it's used in tests.
     *
     * @param app
     * @param binding
     * @param release
     * @return
     */
    //    1
    protected ResourceHelper handleUpdateApplicationRelease(Application app, ApplicationReleaseBinding binding, ReleaseEntity release) {

        // check if the application resource already exists
        ResourceEntity application = null;
        try {
            application = resourceRepository.getApplicationByNameAndRelease(app.getId().getName(), release);
        } catch (NoResultException e) {
            log.info("Application " + app.getId().getName() + " with " + release.getName() + " does not exist yet");
        }
        ResourceHelper result;

        if (application == null) {
            //create new application (release)
            result = createApplicationRelease(app, binding, release);

        } else {
            // Only Update if Owner is Maia
            if (ForeignableOwner.MAIA.equals(application.getOwner())) {
                // update existing application (release)
                result = updateApplicationMainRelease(app, binding, release, application);

                List<ResourceEntity> allMinorReleasesFollowingRelease = resourceImportService.getAllMinorReleasesFollowingRelease(application.getResourceGroup().getResources(), release);
                log.info("number of zwischenreleases following release " + release.getName() + " : " + allMinorReleasesFollowingRelease.size());
                for (ResourceEntity minorReleaseResourceToUpdate : allMinorReleasesFollowingRelease) {
                    updateMinorReleaseResource(binding, minorReleaseResourceToUpdate);
                }

            } else {
                log.warning("The Application ("+app.getId().getName()+") in Release (" + release.getName() + ") must be owned by Maia");
                throw new AMWRuntimeException("The Application ("+app.getId().getName()+") in Release (" + release.getName() + ") must be owned by Maia");
            }
        }

        return result;
    }


    //  2
    private ResourceHelper createApplicationRelease(Application app, ApplicationReleaseBinding binding, ReleaseEntity release) {

        ResourceHelper resourceHelper = new ResourceHelper();
        resourceHelper.setNouveau(true);

        // create the app
        ResourceEntity application = createApplication(app, release.getId(), FEDERATION_SERVICE_IMPORT_OWNER);

        resourceHelper.addResource(application);

        // create its properties
        handlePropertiesForResource(application, binding.getPayload().getProperties());

        for (ConsumedPort consumedPort : binding.getPayload().getConsumedPorts()) {
            createConsumedPort(release, resourceHelper, application, consumedPort);
        }

        for (ProvidedPort providedPort : binding.getPayload().getProvidedPorts()) {
            createProvidedPort(release, resourceHelper, application, providedPort);
        }

        //copy AMW decorations from latest ZwischenRelease
        copyAmwDecorationsFromPreviousRelease(application);

        setOutOfService(app.getOutOfServiceByRelease(), application.getResourceGroup());

        resourceHelper.addMessage(new Message(INFO, "created application " + app.getId().getName() + " for release " + release.getName()));

        return resourceHelper;
    }

    private void copyAmwDecorationsFromPreviousRelease(ResourceEntity application) {
        ResourceEntity previousRelease = resourceImportService.getPreviousRelease(application.getResourceGroup().getResources(), application);
        if(previousRelease != null){
            // copy all AMW added Stuff from the latest Release to the new MAIA app.
            try {
                copyResourceDomainService.copyFromPredecessorToSuccessorResource(previousRelease, application, ForeignableOwner.MAIA);
            } catch (ForeignableOwnerViolationException e) {
                log.warning("Error copying from previous Release for Application (" + application.getName() + ") in previous Release (" + previousRelease.getName() + ") is not owned by AMW");
                throw new AMWRuntimeException("Error copying from Previous Release for Application (" + application.getName() + ") in previous Release (" + previousRelease.getName() + ") is not owned by AMW", e);
            } catch (AMWException e) {
                log.warning("Error coping from previous Release for Application (" + application.getName() + ") in previous Release (" + previousRelease.getName() + ")");
                throw new AMWRuntimeException("Error coping from previous Release for Application (" + application.getName() + ") in previous Release (" + previousRelease.getName() + ")", e);
            }
        }
    }

    private void setOutOfService(String releaseName, ResourceGroupEntity resourceGroupToSetOutOfService){
        if(releaseName != null && !releaseName.isEmpty()) {
            ReleaseEntity re = releaseService.findByName(releaseName);
            if (re != null) {
                resourceGroupToSetOutOfService.setOutOfServiceRelease(re);
            } else {
                log.warning("Out of Service Release ("+releaseName+") not found for Resource " + resourceGroupToSetOutOfService.getName());
                throw new AMWRuntimeException("Out of Service Release ("+releaseName+") not found for Resource " + resourceGroupToSetOutOfService.getName());
            }
        }
    }

    //    2
    private ResourceHelper updateApplicationMainRelease(Application app, ApplicationReleaseBinding binding, ReleaseEntity release, ResourceEntity application) {

        ResourceHelper resourceHelper = new ResourceHelper();

        // obtain all existing application properties, compare its PropertyDescriptors and Tags (add/remove/update)
        application.setExternalKey(app.getFcKey());
        application.setExternalLink(app.getFcLink());

        // update its properties
        handlePropertiesForResource(application, binding.getPayload().getProperties());

        // obtain all existing consumed ports/relations with their properties (add/remove/update)
        updateConsumedPorts(binding.getPayload().getConsumedPorts(), release, application);

        // obtain all existing provided ports/relations with their properties (add/remove/update)
        updateProvidedPorts(binding.getPayload().getProvidedPorts(), release, application);

        // update Out Of Service on Resource Group
        setOutOfService(app.getOutOfServiceByRelease(), application.getResourceGroup());

        // merge
        resourceBoundary.updateResource(application);

        resourceHelper.addResource(application);

        resourceHelper.addMessage(new Message(INFO, "updated application " + app.getId().getName() + " for release " + release.getName()));

        return resourceHelper;
    }

    //    2
    private void updateMinorReleaseResource(ApplicationReleaseBinding binding, ResourceEntity minorReleaseResourceToUpdate) {
        handlePropertiesForResource(minorReleaseResourceToUpdate, binding.getPayload().getProperties());

        // obtain all existing consumed ports/relations with their properties (add/remove/update)
        updateConsumedPorts(binding.getPayload().getConsumedPorts(), minorReleaseResourceToUpdate.getRelease(), minorReleaseResourceToUpdate);

        // obtain all existing provided ports/relations with their properties (add/remove/update)
        updateProvidedPorts(binding.getPayload().getProvidedPorts(), minorReleaseResourceToUpdate.getRelease(), minorReleaseResourceToUpdate);
    }

    //    5
    private void createConsumedPort(ReleaseEntity release, ResourceHelper resourceHelper, ResourceEntity application, ConsumedPort consumedPort) {
        // create resource
        ResourceEntity cpi = getOrCreateConsumedPortResource(consumedPort, FEDERATION_SERVICE_IMPORT_OWNER, release.getId());

        resourceHelper.addResource(cpi);

        // create its properties
        handlePropertiesForResource(cpi, consumedPort.getProperties());

        // add relation
        relationImportService.addRelation(FEDERATION_SERVICE_IMPORT_OWNER, application, cpi, release.getId(), false);

        // add softlinkRelation
        createOrUpdateSoftlinkRelation(cpi, consumedPort.getProvidedPortRef());

        //copy AMW decorations from latest ZwischenRelease
        copyAmwDecorationsFromPreviousRelease(cpi);
    }

//    5
    private void createProvidedPort(ReleaseEntity release, ResourceHelper resourceHelper, ResourceEntity application, ProvidedPort providedPort) {
        // create resource
        ResourceEntity ppi = getOrCreateProvidedPortResource(providedPort, FEDERATION_SERVICE_IMPORT_OWNER, release.getId());

        resourceHelper.addResource(ppi);

        // create its properties
        handlePropertiesForResource(ppi, providedPort.getProperties());

        // add relation
        relationImportService.addRelation(FEDERATION_SERVICE_IMPORT_OWNER, application, ppi, release.getId(), true);
        //copy AMW decorations from latest ZwischenRelease
        copyAmwDecorationsFromPreviousRelease(ppi);
    }

    //    6
    protected SoftlinkRelationEntity createOrUpdateSoftlinkRelation(ResourceEntity cpi, ProvidedPortID ppDto) {
        if (ppDto == null) {
            // no more softlinkRef: remove existing softlink
            softlinkRelationService.removeSoftlinkRelation(cpi);
        } else {
            SoftlinkRelationEntity softlinkRelation = cpi.getSoftlinkRelation();
            if (softlinkRelation == null || !softlinkRelation.getSoftlinkRef().equals(ppDto.getName())) {
                // create softlink relation
                softlinkRelation = new SoftlinkRelationEntity();
            }
            softlinkRelation.setSoftlinkRef(ppDto.getName());
            softlinkRelation.setCpiResource(cpi);
            softlinkRelation.setOwner(cpi.getOwner());
            softlinkRelationService.setSoftlinkRelation(cpi, softlinkRelation);

            return softlinkRelation;
        }
        return null;
    }


    //    3
    private ResourceHelper updateProvidedPorts(List<ProvidedPort> providedPorts, ReleaseEntity release, ResourceEntity application) {

        ResourceHelper resourceHelper = createOrUpdateProvidedPortRelations(providedPorts, release, application);

        // iterate over the existing ProvidedSlaveRelations
        if (application.getProvidedMasterRelations() != null) {
            List<ProvidedResourceRelationEntity> toBeDeleted = findProvidedPortRelationsToDelete(providedPorts, application);
            relationImportService.deleteProvidedPortRelations(application, toBeDeleted);
        }
        return resourceHelper;
    }

//    4
    private List<ProvidedResourceRelationEntity> findProvidedPortRelationsToDelete(List<ProvidedPort> providedPorts, ResourceEntity application) {
        List<ProvidedResourceRelationEntity> toBeDeleted = new ArrayList<>();
        for (ProvidedResourceRelationEntity providedMasterRelation : application.getProvidedMasterRelations()) {
            String slaveResourceName = providedMasterRelation.getSlaveResource().getName().toLowerCase();

            ProvidedPort providedPort = getMatchingProvidedResource(slaveResourceName, providedPorts);

            // doesn't exist anymore => remove if owner is MAIA
            if (providedPort == null) {
                // remove if owner is MAIA
                if (providedMasterRelation.getOwner().equals(FEDERATION_SERVICE_IMPORT_OWNER)) {
                    toBeDeleted.add(providedMasterRelation);
                }
            }
        }
        return toBeDeleted;
    }

//    4
    private ResourceHelper createOrUpdateProvidedPortRelations(List<ProvidedPort> providedPorts, ReleaseEntity release, ResourceEntity application) {
        ResourceHelper resourceHelper = new ResourceHelper();

        // iterate over the received ConsumedPorts
        for (ProvidedPort providedPort : providedPorts) {
            String slaveResourceName = providedPort.getDisplayName().toLowerCase();

            ProvidedResourceRelationEntity providedMasterRelation = relationImportService.getMatchingResource(slaveResourceName, application.getProvidedMasterRelations());

            // didn't exist before => add
            if (providedMasterRelation == null) {
                // create resource & relation
                createProvidedPort(release, resourceHelper, application, providedPort);
            }
            // exists => update
            else {
                updateProvidedPort(providedPort, providedMasterRelation, release);
            }
        }
        return resourceHelper;
    }



    //    3
    private ResourceHelper updateConsumedPorts(List<ConsumedPort> consumedPorts, ReleaseEntity release, ResourceEntity application)  {

        ResourceHelper resourceHelper = createOrUpdateConsumedPortRelations(consumedPorts, release, application);

        // iterate over the existing ConsumedSlaveRelations
        if (application.getConsumedMasterRelations() != null) {


            try {
                List<ConsumedResourceRelationEntity> toBeDeleted = findConsumedPortRelationsToDelete(consumedPorts, application);

                relationImportService.deleteConsumedPortRelations(toBeDeleted);

            } catch (ResourceNotFoundException| ElementAlreadyExistsException e){
                // TODO message human readable
//                String resourceName = (application != null ? ("for resource " + resource.getName()) : "");
                log.warning("Delete consumed port relation failed! Reason: " + e.getMessage());
                throw new AMWRuntimeException("CODE: Consumed port relation deletion failed: " + e.getMessage(), e);
            }
        }

        return resourceHelper;
    }



    //    4
    private List<ConsumedResourceRelationEntity> findConsumedPortRelationsToDelete(List<ConsumedPort> consumedPorts, ResourceEntity application) {
        List<ConsumedResourceRelationEntity> toBeDeleted = new ArrayList<>();
        for (ConsumedResourceRelationEntity consumedMasterRelation : application.getConsumedMasterRelations()) {
            String slaveResourceName = consumedMasterRelation.getSlaveResource().getName().toLowerCase();

            ConsumedPort consumedPort = getMatchingConsumedResource(slaveResourceName, consumedPorts);

            // doesn't exist anymore => remove if owner is MAIA
            if (consumedPort == null) {
                // remove if owner is MAIA
                if (consumedMasterRelation.getOwner().equals(FEDERATION_SERVICE_IMPORT_OWNER)) {
                    toBeDeleted.add(consumedMasterRelation);
                }
            }
        }
        return toBeDeleted;
    }

    //    4
    private ResourceHelper createOrUpdateConsumedPortRelations(List<ConsumedPort> consumedPorts, ReleaseEntity release, ResourceEntity application) {
        ResourceHelper resourceHelper = new ResourceHelper();
        // iterate over the received ConsumedPorts
        for (ConsumedPort consumedPort : consumedPorts) {
            String slaveResourceName = consumedPort.getDisplayName().toLowerCase();

            ConsumedResourceRelationEntity consumedMasterRelation = relationImportService.getMatchingResource(slaveResourceName, application.getConsumedMasterRelations());

            // didn't exist before => add
            if (consumedMasterRelation == null) {
                // create resource & relation
                createConsumedPort(release, resourceHelper, application, consumedPort);
            }
            // exists => update
            else {
                updateConsumedPort(consumedPort, consumedMasterRelation, release);
            }
        }
        return resourceHelper;
    }

    //    5
    private ConsumedPort getMatchingConsumedResource(String slaveResourceName, List<ConsumedPort> consumedPorts) {
        for (ConsumedPort consumedPort : consumedPorts) {
            if (consumedPort.getDisplayName().equalsIgnoreCase(slaveResourceName)) {
                return consumedPort;
            }
        }
        return null;
    }

//    5
    private ProvidedPort getMatchingProvidedResource(String slaveResourceName, List<ProvidedPort> providedPorts) {
        for (ProvidedPort providedPort : providedPorts) {
            if (providedPort.getDisplayName().equalsIgnoreCase(slaveResourceName)) {
                return providedPort;
            }
        }
        return null;
    }



    //    5
    private void updateConsumedPort(ConsumedPort consumedPort, ConsumedResourceRelationEntity consumedMasterRelation, ReleaseEntity release) {

        // check if owner is MAIA
        if (consumedMasterRelation.getOwner().equals(FEDERATION_SERVICE_IMPORT_OWNER)) {
            // obtain the ResourceEntity for the ConsumedPortResource
            ResourceEntity cpi = getOrCreateConsumedPortResource(consumedPort, FEDERATION_SERVICE_IMPORT_OWNER, release.getId());

            // add, remove or update its properties
            handlePropertiesForRelation(consumedMasterRelation, cpi, consumedPort.getProperties());

            // add softlinkRelation
            createOrUpdateSoftlinkRelation(cpi, consumedPort.getProvidedPortRef());
        } else {
            throw new AMWRuntimeException("Failed to update non " + FEDERATION_SERVICE_IMPORT_OWNER + " consumed relation " + consumedMasterRelation.getRelationIdentifier() + " to resource " + consumedMasterRelation.getSlaveResource().getName());
        }
    }

//    5
    private void updateProvidedPort(ProvidedPort providedPort, ProvidedResourceRelationEntity providedMasterRelation, ReleaseEntity release) {

        // check if owner is MAIA
        if (providedMasterRelation.getOwner().equals(FEDERATION_SERVICE_IMPORT_OWNER)) {
            // obtain the ResourceEntity for the ProvidedPortResource
            ResourceEntity ppi = getOrCreateProvidedPortResource(providedPort, FEDERATION_SERVICE_IMPORT_OWNER, release.getId());

            // add, remove or update its properties
            handlePropertiesForRelation(providedMasterRelation, ppi, providedPort.getProperties());
        } else {
            throw new AMWRuntimeException("Failed to update non " + FEDERATION_SERVICE_IMPORT_OWNER + " provided relation " + providedMasterRelation.getIdentifier() + " to resource " + providedMasterRelation.getSlaveResource().getName());
        }
    }

//    3
    private ResourceEntity createApplication(Application app, Integer releaseId, ForeignableOwner owner) {
        try {
            ch.puzzle.itc.mobiliar.business.resourcegroup.entity.Application applicationWithoutAppServer = resourceBoundary.createNewApplicationWithoutAppServerByName(owner, app.getFcKey(), app.getFcLink(), app.getId().getName(), releaseId, true);
            if (applicationWithoutAppServer != null) {
                log.info("Application " + applicationWithoutAppServer.getName() + " successfully created");
                return applicationWithoutAppServer.getEntity();
            } else {
                throw new AMWException("Could not create application");
            }

        } catch (AMWException e) {
            // TODO message human readable
            log.warning("Application creation for " + app.toString() + " failed! Reason: " + e.getMessage());
            throw new AMWRuntimeException(e.getMessage(), e);
        }
    }


    //    6
//    6
    private void handlePropertiesForRelation(AbstractResourceRelationEntity relationEntity, ResourceEntity resource, List<PropertyDeclaration> properties) {
        try {
            createOrUpdateMaiaPropertyDescriptors(resource, properties);
            List<PropertyDescriptorEntity> propertyDescriptorsToDelete = findPropertyDescriptorsToDelete(resource, properties);

            propertyImportService.deleteAllPropertiesAndValuesInAllContextForRelation(resource, relationEntity, propertyDescriptorsToDelete);

        } catch (AMWException e) {
            String resourceName = (resource != null ? ("to resource " + resource.getName()) : "");
            log.warning("Property creation/update on relation " + resourceName + " failed! Reason: " + e.getMessage());
            throw new AMWRuntimeException("CODE: Property creation/update on relation " + resourceName + " failed: " + e.getMessage(), e);
        }
    }


    //    3
//    6
//    6
    private void handlePropertiesForResource(ResourceEntity resource, List<PropertyDeclaration> properties) {
        try {
            createOrUpdateMaiaPropertyDescriptors(resource, properties);
            List<PropertyDescriptorEntity> propertyDescriptorsToDelete = findPropertyDescriptorsToDelete(resource, properties);
            propertyImportService.deleteAllPropertiesAndValuesInAllContextForResource(resource, propertyDescriptorsToDelete);

        } catch (AMWException e) {
            // TODO message human readable
            String resourceName = (resource != null ? ("for resource " + resource.getName()) : "");
            log.warning("Property creation/update " + resourceName + " failed! Reason: " + e.getMessage());
            throw new AMWRuntimeException("CODE: Property creation/update " + resourceName + " failed: " + e.getMessage(), e);
        }
    }


    //    4
    private void createOrUpdateMaiaPropertyDescriptors(ResourceEntity resource, List<PropertyDeclaration> properties) throws AMWException {
        for (PropertyDeclaration property : properties) {
            if (maiaPropertyType == null) {
                // TODO message human readable
                log.warning("Maia property type " + MAIA_PROPERTY_TYPE + " does not exist!");
                throw new AMWException("Maia property type " + MAIA_PROPERTY_TYPE + " does not exist!");
            }
            createOrUpdatePropertyForResource(resource, property);
            log.info("Property " + property.getTechnicalKey() + " successfully created/updated");
        }
    }

    //    4
//    7
    private List<PropertyDescriptorEntity> findPropertyDescriptorsToDelete(ResourceEntity resource, List<PropertyDeclaration> properties) {
        // delete propertyDescriptors with owner MAIA which are not in the properties

        ContextEntity globalResourceContextEntity = contextDomainService.getGlobalResourceContextEntity();

        ResourceContextEntity resourceContext = resource.getOrCreateContext(globalResourceContextEntity);
        Set<PropertyDescriptorEntity> propertyDescriptors = resourceContext.getPropertyDescriptors();
        List<PropertyDescriptorEntity> toBeDeleted = new ArrayList<>();

        if (propertyDescriptors != null) {
            for (PropertyDescriptorEntity propertyDescriptor : propertyDescriptors) {
                PropertyDescriptorEntity tbd = getPropertyToBeDeleted(properties, propertyDescriptor);
                if (tbd != null) {
                    toBeDeleted.add(tbd);
                }
            }
        }
        return toBeDeleted;
    }

    //    5
    private void createOrUpdatePropertyForResource(ResourceEntity resource, PropertyDeclaration property) throws AMWException {
        PropertyDescriptorEntity propDesc = getPropertyToUpdate(resource, property);
        // add
        if (propDesc == null) {
            propDesc = createorUpdateProperty(property, maiaPropertyType, null);
        }
        // update
        else {
            // ok if owner is MAIA
            if (propDesc.getOwner().isSameOwner(FEDERATION_SERVICE_IMPORT_OWNER)) {
                propDesc = createorUpdateProperty(property, maiaPropertyType, propDesc);
            } else {
                throw new AMWRuntimeException("Failed to update non " + FEDERATION_SERVICE_IMPORT_OWNER + " property " + propDesc.getPropertyDescriptorDisplayName());
            }
        }
        propertyImportService.savePropertyDescriptorWithTags(propDesc, property.getTags(), resource, FEDERATION_SERVICE_IMPORT_OWNER);
    }

//    6
    private PropertyDescriptorEntity getPropertyToUpdate(ResourceEntity resource, PropertyDeclaration property) {

        ContextEntity globalResourceContextEntity = contextDomainService.getGlobalResourceContextEntity();

        ResourceContextEntity resourceContext = resource.getOrCreateContext(globalResourceContextEntity);
        // existing PropertyDescriptorEntity
        Set<PropertyDescriptorEntity> propertyDescriptors = resourceContext.getPropertyDescriptors();
        if (propertyDescriptors != null) {
            for (PropertyDescriptorEntity propertyDescriptor : propertyDescriptors) {
                if (property.getTechnicalKey().trim().equalsIgnoreCase(propertyDescriptor.getPropertyName())) {
                    return propertyDescriptor;
                }
            }
        }
        return null;
    }

    //    5
    private PropertyDescriptorEntity getPropertyToBeDeleted(List<PropertyDeclaration> properties, PropertyDescriptorEntity propertyDescriptor) {

        for (PropertyDeclaration property : properties) {
            if (property.getTechnicalKey().trim().equalsIgnoreCase(propertyDescriptor.getPropertyName())) {
                return null;
            }
        }
        // deletetion is ok if owner is MAIA
        if (propertyDescriptor.getOwner().isSameOwner(FEDERATION_SERVICE_IMPORT_OWNER)) {
            return propertyDescriptor;
        }
        return null;
    }

    //    6
    private ResourceEntity getOrCreateConsumedPortResource(ConsumedPort consumedPort, ForeignableOwner owner, Integer releaseId) {
        try {

            Integer resourceTypeId = obtainResourceTypeId(consumedPort.getResourceType());

            // create a basic resource (entity)
            ResourceEntity resourceEntity = resourcesService.getOrCreateNewResourceByName(owner, consumedPort.getDisplayName(), resourceTypeId, releaseId).getEntity();

            // additional attribute(s)
            resourceEntity.setExternalKey(consumedPort.getFcKey());
            resourceEntity.setExternalLink(consumedPort.getFcLink());
            resourceEntity.setLocalPortId(consumedPort.getLocalPortID());

            log.info("Consumed port instance " + resourceEntity.getName() + " successfully created");
            return resourceEntity;
        } catch (AMWException e) {
            // TODO message human readable
            log.warning("Consumed port instance creation for " + consumedPort.toString() + " failed! Reason: " + e.getMessage());
            throw new AMWRuntimeException("CODE: Consumed port instance creation failed: " + e.getMessage(), e);
        }
    }

//    6
//    6
    private ResourceEntity getOrCreateProvidedPortResource(ProvidedPort providedPort, ForeignableOwner owner, Integer releaseId) {
        try {

            Integer resourceTypeId = obtainResourceTypeId(providedPort.getResourceType());

            // create a basic resource (entity)
            ResourceEntity resourceEntity = resourcesService.getOrCreateNewResourceByName(owner, providedPort.getDisplayName(), resourceTypeId, releaseId).getEntity();

            // additional attribute(s)
            resourceEntity.setExternalKey(providedPort.getFcKey());
            resourceEntity.setExternalLink(providedPort.getFcLink());
            resourceEntity.setLocalPortId(providedPort.getLocalPortID());
            resourceEntity.setSoftlinkId(providedPort.getId().getName());

            log.info("Provided port instance " + resourceEntity.getName() + " successfully created");
            return resourceEntity;
        } catch (AMWException e) {
            // TODO message human readable
            log.warning("Provided port instance creation for " + providedPort.toString() + " failed! Reason: " + e.getMessage());
            throw new AMWRuntimeException("CODE: Provided port instance creation failed: " + e.getMessage(), e);
        }
    }



// 6
    private PropertyDescriptorEntity createorUpdateProperty(PropertyDeclaration props, PropertyTypeEntity propertyType, PropertyDescriptorEntity propDesc) {

        if (propDesc == null) {
            propDesc = new PropertyDescriptorEntity();
        }
        propDesc.setDisplayName(props.getDisplayName());
        propDesc.setPropertyName(props.getTechnicalKey());
        propDesc.setDefaultValue(props.getDefaultValue());
        propDesc.setExampleValue(props.getExampleValue());
        propDesc.setValidationLogic(props.getValidationPattern());
        propDesc.setMachineInterpretationKey(props.getMachineInterpretationKey());
        // boolean fields - treat null (= missing in the request) as false
        propDesc.setEncrypt(props.isEncrypted());
        propDesc.setNullable(props.isIsValueOptional());
        propDesc.setOptional(props.isIsKeyOptional());
        // the tags will be assigned by the service method descriptorService.savePropertyDescriptorForOwner

        propDesc.setPropertyTypeEntity(propertyType);

        return propDesc;
    }



    //    7
//    7
    private Integer obtainResourceTypeId(String resourceTypeName) throws AMWException {
        // find it
        Integer resourceTypeId = resourceTypeService.getResourceTypeIdByResourceTypeName(resourceTypeName);
        // the ResourceTypeId must already exist throw exception
        if (resourceTypeId == null) {
            log.warning("ResourceType " + resourceTypeName + " does not exist");
            throw new AMWException("ResourceType " + resourceTypeName + " does not exist");
        }
        return resourceTypeId;
    }

    //    1
    private String buildAmwResourceLink(List<ResourceEntity> resources, String applicationName) {
        ResourceEntity resource = resourceDependencyResolverService.findMostRelevantResource(resources, new Date());

        if(resource != null) {
            return resourceImportService.getImportedResourceBacklink() + resource.getId();
        }

        log.info("application " + applicationName + " does not exist thus no link is created");
        return null;
    }

    public String buildAmwResourceLink(String applicationName) {
        try {
            List<ResourceEntity> resourcesByGroupNameWithRelations = resourceLocator.getResourcesByGroupNameWithRelations(applicationName);
            return buildAmwResourceLink(resourcesByGroupNameWithRelations, applicationName);
        } catch (ch.puzzle.itc.mobiliar.business.utils.ValidationException e) {
            log.info("Invalid application name " + applicationName + " ! Thus no link is created");
            return null;
        }
    }
}
