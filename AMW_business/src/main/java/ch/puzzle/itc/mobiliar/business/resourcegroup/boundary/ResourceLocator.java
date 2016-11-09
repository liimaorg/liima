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

package ch.puzzle.itc.mobiliar.business.resourcegroup.boundary;

import ch.puzzle.itc.mobiliar.business.generator.control.extracted.ResourceDependencyResolverService;
import ch.puzzle.itc.mobiliar.business.releasing.boundary.ReleaseLocator;
import ch.puzzle.itc.mobiliar.business.releasing.entity.ReleaseEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.ResourceRepository;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ConsumedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.utils.ValidationException;
import ch.puzzle.itc.mobiliar.business.utils.ValidationHelper;
import ch.puzzle.itc.mobiliar.common.util.ConfigurationService;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
public class ResourceLocator {

    public static final String WS_CPI_TYPE = "wscpi";
    public static final String WS_PPI_TYPE = "wsppi";

	@Inject
	ResourceRepository resourceRepository;

	@Inject
	ReleaseLocator releaseLocator;

    @Inject
    ResourceDependencyResolverService resourceDependencyResolverService;

    @Inject
    EntityManager entityManager;

	@Inject
	Logger log;

	/**
	 * @param name name of resource group
	 * @param releaseName release name
	 * @return
	 * @throws ValidationException thrown if one of the arguments is either empty or null
	 */
	public ResourceEntity getResourceByGroupNameAndRelease(String name, String releaseName)
			throws ValidationException {
		ValidationHelper.validateNotNullOrEmptyChecked(name, releaseName);

		ReleaseEntity release = releaseLocator.getReleaseByName(releaseName);
        try {
            return resourceRepository.getResourceByNameAndRelease(name, release);
        }
        catch (NoResultException e) {
            return null;
        }
	}

	/**
	 * @param name
	 * @param releaseName
	 * @return
	 * @throws ValidationException thrown if one of the arguments is either empty or null
	 */
	public ResourceEntity getResourceByNameAndReleaseWithRelations(String name, String releaseName)
			throws ValidationException {
		ValidationHelper.validateNotNullOrEmptyChecked(name, releaseName);
		ReleaseEntity release = releaseLocator.getReleaseByName(releaseName);
		return resourceRepository.getResourceByNameAndReleaseWithRelations(name, release);
	}

	public List<ResourceEntity> getResourcesByGroupNameWithRelations(String groupName)
			throws ValidationException {
		ValidationHelper.validateNotNullOrEmptyChecked(groupName);
		return resourceRepository.getResourcesByGroupNameWithRelations(groupName);
	}

    public List<ResourceEntity> getResourcesByGroupNameWithAllRelationsOrderedByRelease(String groupName)
            throws ValidationException {
        ValidationHelper.validateNotNullOrEmptyChecked(groupName);
        return resourceRepository.getResourcesByGroupNameWithAllRelationsOrderedByRelease(groupName);
    }

    /**
     * @return resource for id with resourceGroup and other resources
     */
    public ResourceEntity getResourceWithGroupAndRelatedResources(Integer resourceId) {
        return resourceRepository.loadWithResourceGroupAndRelatedResourcesForId(resourceId);
    }

    /**
     *
     * @param application
     *             - the resource entity representing the application
     * @return the resource entity representing the application server for the release of the given
     *         application or null if not available (or resource is not an application)
     */
    public ResourceEntity getApplicationServerForApplication(ResourceEntity application) {
        ResourceEntity res = entityManager.find(ResourceEntity.class, application.getId());
        if (res.getResourceType().isApplicationResourceType()) {
            ResourceGroupEntity appserverResgroup = null;
            for (ConsumedResourceRelationEntity rel : res.getConsumedSlaveRelations()) {
                if (rel.getMasterResource().getResourceType().isApplicationServerResourceType()) {
                    appserverResgroup = rel.getMasterResource().getResourceGroup();
                    break;
                }
            }
            if (appserverResgroup != null) {
                return resourceDependencyResolverService.getResourceEntityForRelease(appserverResgroup,
                        res.getRelease());
            }
        }
        return null;
    }

    /**
     * returns true if one of the resourcetypes (within tree up until root resource type) has the same name as one of the comma separated values of the system property {@link ch.puzzle.itc.mobiliar.common.util.ConfigurationService.ConfigKey#PROVIDABLE_SOFTLINK_RESOURCE_TYPES}
     * @param resource
     * @return
     */
    public boolean hasResourceProvidableSoftlinkType(ResourceEntity resource){
        List<String> providableSoftlinkResourceTypes;
        providableSoftlinkResourceTypes = extractResourceTypeSystemProperties(ConfigurationService.ConfigKey.PROVIDABLE_SOFTLINK_RESOURCE_TYPES, WS_PPI_TYPE);
        return isTypeOrHasTypeAsSuperType(providableSoftlinkResourceTypes, resource.getResourceType());
    }


    /**
     * returns true if one of the resourcetypes (within tree up until root resource type) has the same name as one of the comma separated values of the system property {@link ch.puzzle.itc.mobiliar.common.util.ConfigurationService.ConfigKey#PROVIDABLE_SOFTLINK_RESOURCE_TYPES}
     * @param resourceId
     * @return
     */
    public boolean hasResourceProvidableSoftlinkType(Integer resourceId){
        return hasResourceProvidableSoftlinkType(entityManager.find(ResourceEntity.class, resourceId));
    }

    /**
     * returns true if one of the resourcetypes (within tree up until root resource type) has the same name as one of the comma separated values of the system property {@link ch.puzzle.itc.mobiliar.common.util.ConfigurationService.ConfigKey#CONSUMABLE_SOFTLINK_RESOURCE_TYPES}
     */
    public boolean hasResourceConsumableSoftlinkType(Integer resourceId){
        return hasResourceConsumableSoftlinkType(entityManager.find(ResourceEntity.class, resourceId));
    }

    /**
     * returns true if one of the resourcetypes (within tree up until root resource type) has the same name as one of the comma separated values of the system property {@link ch.puzzle.itc.mobiliar.common.util.ConfigurationService.ConfigKey#CONSUMABLE_SOFTLINK_RESOURCE_TYPES}
     * @param resource
     * @return
     */
    public boolean hasResourceConsumableSoftlinkType(ResourceEntity resource){
        List<String> consumableSoftlinkResourceTypes = extractResourceTypeSystemProperties(ConfigurationService.ConfigKey.CONSUMABLE_SOFTLINK_RESOURCE_TYPES, WS_CPI_TYPE);
        return isTypeOrHasTypeAsSuperType(consumableSoftlinkResourceTypes, resource.getResourceType());
    }

    private boolean isTypeOrHasTypeAsSuperType(List<String> resourceTypes, ResourceTypeEntity resourceType){
        if (resourceType != null){
            if (resourceTypes.contains(resourceType.getName().toLowerCase())){
                return true;
            }

            if (!resourceType.isRootResourceType()){
                return isTypeOrHasTypeAsSuperType(resourceTypes, resourceType.getParentResourceType());
            }
        }
        return false;
    }


    protected List<String> extractResourceTypeSystemProperties(ConfigurationService.ConfigKey systemProperty, String defaultValue) {
        String commaseperatedProperties;

        if (defaultValue != null){
            commaseperatedProperties = ConfigurationService.getProperty(systemProperty, defaultValue);
        } else {
            commaseperatedProperties = ConfigurationService.getProperty(systemProperty);
        }

        List<String> result = new ArrayList<>();

        if (commaseperatedProperties != null) {
            String[] properties = commaseperatedProperties.split(",");
            for (String property : properties) {
                result.add(property.trim().toLowerCase());
            }
        }
        return result;
    }

    
    /**
     * Für JavaBatch Monitor
     * @param name
     *            name of resource group
     * @return List of ResourceEntities
     * @throws ValidationException
     *             thrown if one of the arguments is either empty or null
     */
    public List<ResourceEntity> getResourceByGroupName(String name) throws ValidationException {
        ValidationHelper.validateNotNullOrEmptyChecked(name);

        try {
            return resourceRepository.getResourceByName(name);
        } catch (NoResultException e) {
            return null;
        }
    }
    
    /**
     *  Für JavaBatch Monitor
     * @param resource
     * @return
     */
    public List<ResourceEntity> getAllApplicationsWithResource(int resource) {
        return resourceRepository.getAllApplicationsWithResource(resource);
    }

    /**
     *  Für JavaBatch Monitor
     * @param apps
     * @return
     * @throws ValidationException
     */
    public List<ResourceEntity> getBatchJobConsumedResources(List<String> apps) throws ValidationException {
        if (apps == null || apps.isEmpty()) {
            return null;
        }

        try {
            return resourceRepository.getBatchJobConsumedResources(apps);
        } catch (NoResultException e) {
            return null;
        }
    }
    
    /**
     *  Für JavaBatch Monitor
     * @param apps
     * @return
     * @throws ValidationException
     */
    public List<ResourceEntity> getBatchJobProvidedResources(List<String> apps) throws ValidationException {
        if (apps == null || apps.isEmpty()) {
            return null;
        }
        try {
            return resourceRepository.getBatchJobProvidedResources(apps);
        } catch (NoResultException e) {
            return null;
        }
    }
    
    /**
     *  Für JavaBatch Monitor
     * @param appServerList
     * @return Map<String app, String server>
     */
    public Map<String, String> getAppToAppServerMapping(List<String> appServerList) {
        if (appServerList==null || appServerList.isEmpty()) {
            return null;
        }
        Map<String, String> map = new HashMap<>();        
        List<ResourceEntity> resultList = resourceRepository.getAppToAppServerMapping(appServerList);
        for (ResourceEntity entity : resultList) {
            for (ConsumedResourceRelationEntity rel: entity.getConsumedMasterRelations()) {
                map.put(rel.getSlaveResource().getName(), entity.getName());
                break; //einmal zuweisen genügt
            }
            
        }
        return map;
    }

}
