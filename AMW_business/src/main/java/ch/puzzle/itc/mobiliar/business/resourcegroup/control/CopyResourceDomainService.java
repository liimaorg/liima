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

package ch.puzzle.itc.mobiliar.business.resourcegroup.control;

import ch.puzzle.itc.mobiliar.business.auditview.control.AuditService;
import ch.puzzle.itc.mobiliar.business.domain.commons.CommonDomainService;
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextDependency;
import ch.puzzle.itc.mobiliar.business.function.entity.AmwFunctionEntity;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyDescriptorEntity;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyEntity;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyTagEntity;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyTagType;
import ch.puzzle.itc.mobiliar.business.releasing.entity.ReleaseEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.ResourceLocator;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceContextEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceFactory;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.AbstractResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ConsumedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ProvidedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ResourceRelationContextEntity;
import ch.puzzle.itc.mobiliar.business.template.entity.TemplateDescriptorEntity;
import ch.puzzle.itc.mobiliar.common.exception.AMWException;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Service wird verwendet zum kopieren von Ressourcen ("Templating") und zum erstellen von neuen Releases.<br/>
 * Es gibt gewisse Unterschiede zwischen dem Erstellen einer Kopie und dem Erstellen eines Release, deshalb
 * mit {@link ch.puzzle.itc.mobiliar.business.resourcegroup.control.CopyResourceDomainService.CopyMode}
 * angegeben werden, welche der beiden Varianten ausgeführt werden soll.<br/>
 * <h1>Allgemein</h1>
 * <ul>
 * <li>Die Instanzen müssen vom gleichen Typ sein</li>
 * <li>Bestehende Werte werden überschrieben</li>
 * <li>Daten auf der Zielresource die auf der Ursprungsresource nicht vorhanden sind, bleiben bestehen.</li>
 * </ul>
 * <h1>Was wird kopiert?</h1>
 * <table>
 * <tr>
 * <th></th>
 * <th>Copy</th>
 * <th>Release</th>
 * </tr>
 * <tr>
 * <td>PropertyDescriptors und ihre Properties</td>
 * <td>ja</td>
 * <td>ja</td>
 * </tr>
 * <tr>
 * <td>Instanz PropertyDescriptors und ihre Properties</td>
 * <td>ja</td>
 * <td>ja</td>
 * </tr>
 * <tr>
 * <td>Instanz Templates</td>
 * <td>ja</td>
 * <td>ja</td>
 * </tr>
 * <tr>
 * <td>Consumed Relations</td>
 * <td>ja, ausser wenn die slave resource eine Applikation ist da diese nur von einem AS konsumiert werden
 * können</td>
 * <td>ja</td>
 * </tr>
 * <tr>
 * <td>Provided Relations</td>
 * <td>nein</td>
 * <td>ja</td>
 * </tr>
 * <tr>
 * <td>Properties auf Relations</td>
 * <td>ja</td>
 * <td>ja</td>
 * </tr>
 * <tr>
 * <td>Templates auf Relations</td>
 * <td>ja</td>
 * <td>ja</td>
 * </tr>
 * <tr>
 * <td>AppServerRelations</td>
 * <td>ja, ausser wenn die slave resource eine Applikation ist da diese nur von einem AS konsumiert werden
 * können und entsprechen alle AppServerRelations die eine Applikation als direkten oder übergeordneten parent
 * haben.</td>
 * <td></td>
 * </tr>
 * <tr>
 * <td>Import name</td>
 * <td>nein</td>
 * <td>nein</td>
 * </tr>
 * </table>
 *
 * @author cweber
 */
public class CopyResourceDomainService {

    @Inject
    private EntityManager entityManager;

    @Inject
    CommonDomainService commonDomainService;

    @Inject
    ResourceLocator resourceLocator;

    @Inject
    AuditService auditService;

    public enum CopyMode {
        COPY, RELEASE
    }

    public CopyResourceResult copyFromOriginToTargetResource(ResourceEntity origin, ResourceEntity target)
            throws AMWException {
        if (target == null) {
            throw new RuntimeException("Target resource should not be null for copy action");
        } else {
            return doCopyResourceAndSave(new CopyUnit(origin, target, CopyMode.COPY));
        }
    }

    /**
     * @param origin  - the resource to create a new release from
     * @param release - the release to create
     */
    public CopyResourceResult createReleaseFromOriginResource(ResourceEntity origin, ReleaseEntity release)
            throws AMWException {
        ResourceEntity target = commonDomainService.getResourceEntityByGroupAndRelease(origin.getResourceGroup().getId(), release.getId());
        if (target == null) {
            target = ResourceFactory.createNewResource(origin.getResourceGroup());
            target.setRelease(release);
        }
        return doCopyResourceAndSave(new CopyUnit(origin, target, CopyMode.RELEASE));
    }

    /**
     * Persists the target
     *
     * @return result if copy was successful, contains a list with error messages if copy fails
     */
    protected CopyResourceResult doCopyResourceAndSave(CopyUnit copyUnit) {
        doCopy(copyUnit);
        if (!copyUnit.getResult().isSuccess()) {
            return copyUnit.getResult();
        }
        auditService.storeIdInThreadLocalForAuditLog(copyUnit.getTargetResource());
        entityManager.persist(copyUnit.getTargetResource());
        return copyUnit.getResult();
    }

    protected void doCopy(CopyUnit copyUnit) {
        copyUnit.getOriginResource().getCopy(copyUnit.getTargetResource(), copyUnit);
        copyResourceContexts(copyUnit);
        copyConsumedMasterRelations(copyUnit);
        copyConsumedSlaveRelations(copyUnit);
        copyProvidedMasterRelations(copyUnit);
        copyProvidedSlaveRelations(copyUnit);
        copyFunctions(copyUnit);
        copyUnit.getResult().setTargetResource(copyUnit.getTargetResource());
    }

    protected void copyConsumedMasterRelations(CopyUnit copyUnit) {
        Set<ConsumedResourceRelationEntity> targetConsumedMasterRel = copyUnit.getTargetResource().getConsumedMasterRelations();
        Set<ConsumedResourceRelationEntity> originConsumedMasterRel = copyUnit.getOriginResource().getConsumedMasterRelations();
        if(targetConsumedMasterRel == null) {
            targetConsumedMasterRel = new HashSet<ConsumedResourceRelationEntity>();
        }
        copyConsumedResourceRelationEntities(originConsumedMasterRel, targetConsumedMasterRel, copyUnit, false);
        copyUnit.getTargetResource().setConsumedMasterRelations(targetConsumedMasterRel);
    }

    protected void copyConsumedSlaveRelations(CopyUnit copyUnit) {
        Set<ConsumedResourceRelationEntity> targetConsumedSlaveRel = copyUnit.getTargetResource().getConsumedSlaveRelations();
        Set<ConsumedResourceRelationEntity> originConsumedSlaveRel = copyUnit.getOriginResource().getConsumedSlaveRelations();
        if(targetConsumedSlaveRel == null) {
            targetConsumedSlaveRel = new HashSet<ConsumedResourceRelationEntity>();
        }
        if (copyUnit.getMode() == CopyMode.RELEASE) {
            copyConsumedResourceRelationEntities(originConsumedSlaveRel, targetConsumedSlaveRel, copyUnit, true);
            copyUnit.getTargetResource().setConsumedSlaveRelations(targetConsumedSlaveRel);
        } else if (originConsumedSlaveRel != null) {
            for (ConsumedResourceRelationEntity consumed : originConsumedSlaveRel) {
                copyUnit.getResult().addSkippedConsumedRelation(copyUnit.getOriginResource().getId(),
                        consumed.getMasterResource().getName(), consumed.getSlaveResource().getName(),
                        consumed.getIdentifier(),
                        consumed.getMasterResource().getResourceType().getName(),
                        consumed.getSlaveResource().getResourceType().getName());
            }
        }
    }

    /**
     * iterate and copy
     */
    protected void copyConsumedResourceRelationEntities(Set<ConsumedResourceRelationEntity> origins,
                                                        Set<ConsumedResourceRelationEntity> targets,
                                                        CopyUnit copyUnit, boolean slaveRelation) {
        if (origins == null) {
            return;
        }
        Map<String, ConsumedResourceRelationEntity> targetMap = new HashMap<>();
        // prepare map with identifier as key and list of runtime relations
        Set<ConsumedResourceRelationEntity> runtimeRelations = new HashSet<>();
        for (ConsumedResourceRelationEntity target : targets) {
            if (target.getSlaveResource() != null) {
                if (target.getSlaveResource().getResourceType().isRuntimeType()) {
                    runtimeRelations.add(target);
                }
                targetMap.put(target.buildIdentifer(), target);
            }
        }

        for (ConsumedResourceRelationEntity origin : origins) {
            // If a runtime already exists and another runtime is copied, we overwrite the previous
            // TODO: runtime - write test
            if (shallReplaceRuntime(runtimeRelations, origin)) {
                for (ConsumedResourceRelationEntity rel : runtimeRelations) {
                    targets.remove(rel);
                    entityManager.remove(rel);
                }
                runtimeRelations.clear();
            }
            String key = origin.buildIdentifer();
            ConsumedResourceRelationEntity target = targetMap.get(key);
            target = origin.getCopy(target, copyUnit);

            if (target != null) {
                copyResourceRelationContexts(origin.getContexts(), target, copyUnit, slaveRelation);
                targets.add(target);
            }
        }
    }

    private boolean shallReplaceRuntime(Set<ConsumedResourceRelationEntity> originalRelations,
                                        ConsumedResourceRelationEntity relationToCheck) {
        if (originalRelations.isEmpty() || !relationToCheck.getSlaveResource().getResourceType().isRuntimeType()) {
            return false;
        }
        ResourceGroupEntity originalRuntime = originalRelations.iterator().next().getSlaveResource()
                .getResourceGroup();
        ResourceGroupEntity newRuntime = relationToCheck.getSlaveResource().getResourceGroup();
        return !originalRuntime.getId().equals(newRuntime.getId());
    }

    protected void copyProvidedMasterRelations(CopyUnit copyUnit) {
        Set<ProvidedResourceRelationEntity> targetProvidedResRels = copyUnit.getTargetResource().getProvidedMasterRelations();
        Set<ProvidedResourceRelationEntity> originProvidedResRels = copyUnit.getOriginResource().getProvidedMasterRelations();
        if (targetProvidedResRels == null) {
            targetProvidedResRels = new HashSet<ProvidedResourceRelationEntity>();
        }
        if (copyUnit.getMode() == CopyMode.RELEASE) {
            copyProvidedResourceRelationEntities(originProvidedResRels, targetProvidedResRels, copyUnit, false);
            copyUnit.getTargetResource().setProvidedMasterRelations(targetProvidedResRels);
        } else if (originProvidedResRels != null) {
            for (ProvidedResourceRelationEntity prov : originProvidedResRels) {
                copyUnit.getResult().addSkippedProvidedRelation(copyUnit.getOriginResource().getId(),
                        prov.getMasterResource().getName(), prov.getSlaveResource().getName(),
                        prov.getIdentifier(), prov.getMasterResource().getResourceType().getName(),
                        prov.getSlaveResource().getResourceType().getName());
            }
        }
    }

    protected void copyProvidedSlaveRelations(CopyUnit copyUnit) {
        Set<ProvidedResourceRelationEntity> targetProvidedSlaveRels = copyUnit.getTargetResource().getProvidedSlaveRelations();
        Set<ProvidedResourceRelationEntity> originProvidedSlaveRels = copyUnit.getOriginResource().getProvidedSlaveRelations();
        if (targetProvidedSlaveRels == null) {
            targetProvidedSlaveRels = new HashSet<ProvidedResourceRelationEntity>();
        }
        if (copyUnit.getMode() == CopyMode.RELEASE) {
            copyProvidedResourceRelationEntities(originProvidedSlaveRels, targetProvidedSlaveRels, copyUnit, true);
            copyUnit.getTargetResource().setProvidedSlaveRelations(targetProvidedSlaveRels);
        } else if (originProvidedSlaveRels != null) {
            for (ProvidedResourceRelationEntity prov : originProvidedSlaveRels) {
                copyUnit.getResult().addSkippedProvidedRelation(copyUnit.getOriginResource().getId(),
                        prov.getMasterResource().getName(), prov.getSlaveResource().getName(),
                        prov.getIdentifier(), prov.getMasterResource().getResourceType().getName(),
                        prov.getSlaveResource().getResourceType().getName());
            }
        }
    }

    

    /**
     * iterate and copy
     */
    protected void copyProvidedResourceRelationEntities(Set<ProvidedResourceRelationEntity> origins,
                                                        Set<ProvidedResourceRelationEntity> targets, CopyUnit copyUnit, boolean slaveRelation) {
        if (origins == null) {
            return;
        }
        Map<String, ProvidedResourceRelationEntity> targetMap = new HashMap<>();
        // prepare map with identifier as key
        for (ProvidedResourceRelationEntity target : targets) {
            targetMap.put(target.buildIdentifer(), target);
        }

        for (ProvidedResourceRelationEntity origin : origins) {
            String key = origin.buildIdentifer();
            ProvidedResourceRelationEntity target = targetMap.get(origin.buildIdentifer());

            target = origin.getCopy(target, copyUnit);

            if (target != null) {
                copyResourceRelationContexts(origin.getContexts(), target, copyUnit, slaveRelation);

                if (!targetMap.containsKey(key)) {
                    targets.add(target);
                }
            }
        }
    }

    /**
     * iterate and copy
     */
    protected void copyResourceContexts(CopyUnit copyUnit) {
        Set<ResourceContextEntity> targets = copyUnit.getTargetResource().getContexts();
        Set<ResourceContextEntity> origins = copyUnit.getOriginResource().getContexts();

        if (origins == null) {
            return;
        }

        // prepare map with contextId as key
        Map<Integer, ResourceContextEntity> targetsMap = new HashMap<>();
        if (targets != null) {
            for (ResourceContextEntity target : targets) {
                targetsMap.put(target.getContext().getId(), target);
            }
        }
        // 1. copy descriptors for all contexts
        Map<String, PropertyDescriptorEntity> allPropertyDescriptorsMap = new HashMap<>();
        for (ResourceContextEntity origin : origins) {
            Integer key = origin.getContext().getId();
            ResourceContextEntity target = targetsMap.containsKey(key) ? targetsMap.get(key) : new ResourceContextEntity();
            allPropertyDescriptorsMap.putAll(copyPropertyDescriptors(origin.getPropertyDescriptors(), target.getPropertyDescriptors(), target, copyUnit));
            if (!targetsMap.containsKey(key)) {
                copyUnit.getTargetResource().addContext(target);
            }
            targetsMap.put(key, target);
        }

        // 2. copy context with properties
        for (ResourceContextEntity origin : origins) {
            Integer key = origin.getContext().getId();
            ResourceContextEntity target = targetsMap.get(key);
            copyContextDependency(origin, target, copyUnit, allPropertyDescriptorsMap);
            target.setContextualizedObject(copyUnit.getTargetResource());
        }
    }

    /**
     * iterate and copy
     * slaveRelation: are slave or master relations copied?
     */
    protected void copyResourceRelationContexts(Set<ResourceRelationContextEntity> origins,
                                                AbstractResourceRelationEntity targetResRel,
                                                CopyUnit copyUnit, boolean slaveRelation) {
        if (origins == null) {
            return;
        }
        // prepare map with contextId as key
        Set<ResourceRelationContextEntity> targets = targetResRel.getContexts();
        Map<Integer, ResourceRelationContextEntity> targetsMap = new HashMap<>();
        if (targets != null) {
            for (ResourceRelationContextEntity target : targets) {
                targetsMap.put(target.getContext().getId(), target);
            }
        }
        // 1. copy descriptors for all contexts
        // currently relations don't have descriptors
        Map<String, PropertyDescriptorEntity> allPropertyDescriptorsMap = new HashMap<>();
        for (ResourceRelationContextEntity origin : origins) {
            Integer key = origin.getContext().getId();
            ResourceRelationContextEntity target = targetsMap.containsKey(key) ? targetsMap.get(key) : new ResourceRelationContextEntity();
            allPropertyDescriptorsMap.putAll(copyPropertyDescriptors(origin.getPropertyDescriptors(),
                    target.getPropertyDescriptors(), target, copyUnit));
            if (!targetsMap.containsKey(key)) {
                targetResRel.addContext(target);
            }
            targetsMap.put(key, target);
        }

        // for slave relations the copied descriptor needs to be used
        if (slaveRelation) {
            if (copyUnit.getTargetResource().getContexts() != null) {
                for (ResourceContextEntity resourceContextEntity : copyUnit.getTargetResource().getContexts()) {
                    if (resourceContextEntity.getPropertyDescriptors() != null) {
                        for (PropertyDescriptorEntity propertyDescriptorEntity : resourceContextEntity.getPropertyDescriptors()) {
                            String key = createDescriptorKey(propertyDescriptorEntity);
                            // only add if desc is not on rel
                            if(!allPropertyDescriptorsMap.containsKey(key)) {
                                allPropertyDescriptorsMap.put(key, propertyDescriptorEntity);
                            }
                        }
                    }
                }
            }
        }

        

        // do copy for all contexts
        for (ResourceRelationContextEntity origin : origins) {
            Integer key = origin.getContext().getId();
            ResourceRelationContextEntity target = targetsMap.containsKey(key) ? targetsMap.get(key) : new ResourceRelationContextEntity();
            copyContextDependency(origin, target, copyUnit, allPropertyDescriptorsMap);
            target.setContextualizedObject(targetResRel);
        }
    }

    /**
     * iterate and copy
     */
    protected Map<String, PropertyDescriptorEntity> copyPropertyDescriptors(
            Set<PropertyDescriptorEntity> origins, Set<PropertyDescriptorEntity> targets,
            ContextDependency<?> targetContextDependency, CopyUnit copyUnit) {
        // prepare map with propertyName and isTesting as key
        Map<String, PropertyDescriptorEntity> targetsMap = new HashMap<>();
        if (targets != null) {
            for (PropertyDescriptorEntity target : targets) {
                String key = createDescriptorKey(target);
                targetsMap.put(key, target);
            }
        }

        if (origins != null) {
            for (PropertyDescriptorEntity origin : origins) {
                String key = createDescriptorKey(origin);
                PropertyDescriptorEntity targetDescriptor = targetsMap.get(key);
                copyPropertyDescriptor(targetContextDependency, copyUnit, targetsMap, origin, key, targetDescriptor);
            }
        }
        return targetsMap;
    }

    private void copyPropertyDescriptor(ContextDependency<?> targetContextDependency, CopyUnit copyUnit, Map<String, PropertyDescriptorEntity> targetsMap, PropertyDescriptorEntity origin, String key, PropertyDescriptorEntity targetDescriptor) {
        PropertyDescriptorEntity target = origin.getCopy(targetDescriptor, copyUnit);

        copyTags(origin, target);

        if (!targetsMap.containsKey(key)) {
            targetContextDependency.addPropertyDescriptor(target);
        }
        targetsMap.put(key, target);
    }

    protected String createDescriptorKey(PropertyDescriptorEntity desc) {
        return desc.getPropertyName();
    }

    protected void copyTags(PropertyDescriptorEntity origin, PropertyDescriptorEntity target) {
        Set<String> tagNames = new HashSet<>();
        for (PropertyTagEntity targetTag : target.getPropertyTags()) {
            tagNames.add(targetTag.getName());
        }

        for (PropertyTagEntity originTag : origin.getPropertyTags()) {
            if (!tagNames.contains(originTag.getName())) {
                PropertyTagEntity copy = new PropertyTagEntity();
                copy.setName(originTag.getName());
                copy.setTagType(PropertyTagType.LOCAL);
                target.addPropertyTag(copy);
            }
        }
    }

    /**
     * Copies values from origin to target contextDependency (with properties and templates).
     */
    protected ContextDependency<?> copyContextDependency(ContextDependency<?> origin,
                                                         ContextDependency<?> target,
                                                         CopyUnit copyUnit,
                                                         Map<String, PropertyDescriptorEntity> allTargetDescriptors) {
        // context
        target.setContext(origin.getContext());

        // properties
        Set<PropertyEntity> properties = copyProperties(origin.getProperties(), allTargetDescriptors, target.getProperties(), copyUnit);
        for (PropertyEntity property : properties) {
            target.addProperty(property);
        }

        // templates
        Set<TemplateDescriptorEntity> templates = copyTemplates(origin.getTemplates(), target.getTemplates(), copyUnit);
        for (TemplateDescriptorEntity template : templates) {
            target.addTemplate(template);
        }

        return target;
    }

    /**
     * <ul>
     * <li>The identifier between target propertyDescriptor and origin TargetDescriptor is the propertyName (= technicalKey)</li>
     * <li>If a propertyDescript of the targetResources has already a properyValue, this value will not be overwritten.
     * </ul>
     *
     * @param origins,                 all properties of the origin resource for one context
     * @param targetPropDescriptorMap, map with all propertyDescriptors of the targetResource (after copy), with the propertyName (= technicalKey) as key
     * @param targetProperties         all properties of the target resource for one context
     */
    protected Set<PropertyEntity> copyProperties(Set<PropertyEntity> origins,
                                                 Map<String, PropertyDescriptorEntity> targetPropDescriptorMap,
                                                 Set<PropertyEntity> targetProperties,
                                                 CopyUnit copyUnit) {
        Map<Integer, PropertyEntity> existingPropertiesByDescriptorId = new HashMap<>();
        if (targetProperties != null) {
            for (PropertyEntity existingProperty : targetProperties) {
                if (existingProperty.getDescriptor() != null && existingProperty.getDescriptor().getId() != null) {
                    existingPropertiesByDescriptorId.put(existingProperty.getDescriptor().getId(), existingProperty);
                }
            }
        }

        Set<PropertyEntity> targets = new HashSet<>();
        if (origins != null) {
            for (PropertyEntity origin : origins) {
                // If a property exists on this context for the same descriptor, we define it as the
                // target property...
                PropertyEntity targetProperty = existingPropertiesByDescriptorId.get(origin.getDescriptor().getId());
                PropertyDescriptorEntity targetDescriptor = null;
                if (targetProperty == null) {
                    // If it can't be found, it's possible that we have copied the target descriptor.
                    // Let's look for it.
                    String key = createDescriptorKey(origin.getDescriptor());
                    targetDescriptor = targetPropDescriptorMap.get(key);
                    if (targetDescriptor != null) {
                        // If a property is already defined for the existing descriptor, we update this
                        // value...
                        targetProperty = existingPropertiesByDescriptorId.get(targetDescriptor.getId());
                    }
                }
                if (targetProperty == null) {
                    // If no property for the found property descriptor exists, we create a new one...
                    PropertyEntity target = origin.getCopy(null, copyUnit);
                    // targetDescriptor null come for properties on ResourceTypes or master relations
                    if (targetDescriptor != null) {
                        target.setDescriptor(targetDescriptor);
                    }
                    targets.add(target);
                } else {
                    // otherwise, we merge the new value with the old property entity
                    targets.add(mergePropertyEntity(origin, targetProperty));
                }
            }
        }
        return targets;
    }

    /**
     * Merges the value (and comment) of the original property into the target property.
     */
    protected PropertyEntity mergePropertyEntity(PropertyEntity origin, PropertyEntity target) {
        target.setValue(origin.getValue());
        return target;
    }

    /**
     * Existing templates in target will be overwritten! <br/>
     */
    protected Set<TemplateDescriptorEntity> copyTemplates(Set<TemplateDescriptorEntity> origins,
                                                          Set<TemplateDescriptorEntity> targets,
                                                          CopyUnit copyUnit) {
        Map<String, TemplateDescriptorEntity> targetTemplatesMap = new HashMap<>();
        if (targets != null) {
            for (TemplateDescriptorEntity t : targets) {
                targetTemplatesMap.put(t.getName(), t);
            }
        }

        if (origins != null) {
            for (TemplateDescriptorEntity origin : origins) {
                targetTemplatesMap.put(origin.getName(), origin.getCopy(targetTemplatesMap.get(origin.getName()), copyUnit));
            }
        }
        return new HashSet<>(targetTemplatesMap.values());
    }

    /**
     * Existing functions in target won't be overwritten. <br/>
     */
    protected void copyFunctions(CopyUnit copyUnit) {
        Set<String> targetFunctions = new HashSet<>();

        for (AmwFunctionEntity targetFct : copyUnit.getTargetResource().getFunctions()) {
            targetFunctions.add(targetFct.getName());
        }

        for (AmwFunctionEntity origFct : copyUnit.getOriginResource().getFunctions()) {
            if (!targetFunctions.contains(origFct.getName())) {
                copyUnit.getTargetResource().addFunction(origFct.getCopy(null, copyUnit));
            }
        }
    }

}
