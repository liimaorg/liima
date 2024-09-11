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

package ch.puzzle.itc.mobiliar.business.template.control;

import ch.puzzle.itc.mobiliar.business.environment.control.ContextDomainService;
import ch.puzzle.itc.mobiliar.business.environment.entity.AbstractContext;
import ch.puzzle.itc.mobiliar.business.property.entity.*;
import ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.ResourceLocator;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.*;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.*;
import ch.puzzle.itc.mobiliar.business.security.entity.Action;
import ch.puzzle.itc.mobiliar.business.security.interceptor.HasPermission;
import ch.puzzle.itc.mobiliar.business.resourcerelation.control.ResourceRelationService;
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.security.entity.Permission;
import ch.puzzle.itc.mobiliar.business.template.entity.TemplateDescriptorEntity;
import ch.puzzle.itc.mobiliar.common.exception.*;
import ch.puzzle.itc.mobiliar.common.util.SystemCallTemplate;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.*;
import java.util.logging.Logger;

@Stateless
public class TemplatesScreenDomainService {

    @Inject
    private Logger log;

    @Inject
    private EntityManager entityManager;

    @Inject
    private ContextDomainService contextService;

    @Inject
    ResourceLocator resourceLocator;

    @Inject
    ResourceRelationService resourceRelationService;

    public List<TemplateDescriptorEntity> getGlobalTemplateDescriptorsForResourceType(ResourceTypeEntity resourceType) {
        resourceType = entityManager.find(ResourceTypeEntity.class, resourceType.getId());
        return getTemplateDescriptorsForResourceTypeContext(contextService.getGlobalResourceContextEntity(), resourceType, new ArrayList<TemplateDescriptorEntity>());
    }

    public List<TemplateDescriptorEntity> getGlobalTemplateDescriptorsForResource(ResourceEntity resource) {
        resource = entityManager.find(ResourceEntity.class, resource.getId());
        return getTemplateDescriptorsForResourceContext(contextService.getGlobalResourceContextEntity(), resource, new ArrayList<TemplateDescriptorEntity>());
    }

    public List<TemplateDescriptorEntity> getGlobalTemplateDescriptorsForResource(String resourceGroupName, String releaseName) throws ValidationException {
        ResourceEntity resource = resourceLocator.getResourceByGroupNameAndRelease(resourceGroupName, releaseName);
        return getTemplateDescriptorsForResourceContext(contextService.getGlobalResourceContextEntity(), resource, new ArrayList<TemplateDescriptorEntity>());
    }

    public List<TemplateDescriptorEntity> getGlobalTemplatesForResourceRelation(ResourceEditRelation relation) {
        return getTemplateDescriptorsForResourceRelationContext(contextService.getGlobalResourceContextEntity(), resourceRelationService.getResourceRelation(relation.getResRelId()), new ArrayList<TemplateDescriptorEntity>());
    }

    public List<TemplateDescriptorEntity> getGlobalTemplatesForResourceRelationType(ResourceEditRelation relation) {
        ResourceRelationTypeEntity resRelType = entityManager.find(ResourceRelationTypeEntity.class, relation.getResRelTypeId());
        //TODO parent resource types!?
        return getTemplateDescriptorsForResourceRelationTypeContext(contextService.getGlobalResourceContextEntity(), resRelType, new ArrayList<TemplateDescriptorEntity>());
    }

    public List<TemplateDescriptorEntity> getTemplatesForResourceRelation(AbstractResourceRelationEntity relation) throws ResourceNotFoundException {
        return getTemplateDescriptorsForResourceRelationContext(contextService.getGlobalResourceContextEntity(), resourceRelationService.getResourceRelation(relation.getId()), new ArrayList<TemplateDescriptorEntity>());
    }

    public List<TemplateDescriptorEntity> getGlobalTemplateDescriptorsForResourceRelation(Integer identifier) {
        ResourceRelationTypeEntity resRelType = entityManager.find(ResourceRelationTypeEntity.class, identifier);
        return getTemplateDescriptorsForResourceRelationTypeContext(contextService.getGlobalResourceContextEntity(), resRelType, new ArrayList<TemplateDescriptorEntity>());
    }

    private List<TemplateDescriptorEntity> getTemplateDescriptorsForResourceTypeContext(ContextEntity context, ResourceTypeEntity resourceType,
                                                                                        List<TemplateDescriptorEntity> templateDescriptors) {
        if (resourceType != null) {

            if (resourceType.getContexts() != null) {
                // Get templates of resource type context
                for (ResourceTypeContextEntity resourceTypeContext : resourceType.getContexts()) {
                    if (resourceTypeContext.getContext() != null && resourceTypeContext.getContext().getId().equals(context.getId())) {
                        templateDescriptors = collectTemplateDescriptors(resourceTypeContext, templateDescriptors);
                    }
                }
            }
        }

        if (context.getParent() != null) {
            templateDescriptors = getTemplateDescriptorsForResourceTypeContext(context.getParent(), resourceType, templateDescriptors);
        }
        return templateDescriptors;
    }

    private List<TemplateDescriptorEntity> getTemplateDescriptorsForResourceContext(ContextEntity context, ResourceEntity resource, List<TemplateDescriptorEntity> templateDescriptors) {
        if (resource != null) {
            if (resource.getContexts() != null) {
                // Get templates of resource context
                for (ResourceContextEntity resourceContext : resource.getContexts()) {
                    if (resourceContext.getContext() != null && resourceContext.getContext().getId().equals(context.getId())) {
                        templateDescriptors = collectTemplateDescriptors(resourceContext, templateDescriptors);
                    }
                }
            }
        }
        if (context.getParent() != null) {
            templateDescriptors = getTemplateDescriptorsForResourceContext(context.getParent(), resource, templateDescriptors);
        }
        return templateDescriptors;
    }

    private List<TemplateDescriptorEntity> getTemplateDescriptorsForResourceRelationTypeContext(ContextEntity context, ResourceRelationTypeEntity relationType,
                                                                                                List<TemplateDescriptorEntity> templateDescriptors) {
        if (relationType != null) {

            if (relationType.getContexts() != null) {
                // Get templates of resource type context
                for (ResourceRelationTypeContextEntity resourceRelationTypeContext : relationType.getContexts()) {
                    if (resourceRelationTypeContext.getContext() != null && resourceRelationTypeContext.getContext().getId().equals(context.getId())) {
                        templateDescriptors = collectTemplateDescriptors(resourceRelationTypeContext, templateDescriptors);
                    }
                }
            }
        }

        if (context.getParent() != null) {
            templateDescriptors = getTemplateDescriptorsForResourceRelationTypeContext(context.getParent(), relationType, templateDescriptors);
        }
        return templateDescriptors;
    }

    private List<TemplateDescriptorEntity> getTemplateDescriptorsForResourceRelationContext(ContextEntity context, AbstractResourceRelationEntity relation,
                                                                                            List<TemplateDescriptorEntity> templateDescriptors) {

        if (relation != null) {
            if (relation.getContexts() != null) {
                // Get templates of resource context
                for (ResourceRelationContextEntity resourceRelationContext : relation.getContexts()) {
                    if (resourceRelationContext.getContext() != null && resourceRelationContext.getContext().getId().equals(context.getId())) {
                        templateDescriptors = collectTemplateDescriptors(resourceRelationContext, templateDescriptors);
                    }
                }
            }
        }

        if (context.getParent() != null) {
            templateDescriptors = getTemplateDescriptorsForResourceRelationContext(context.getParent(), relation, templateDescriptors);
        }
        return templateDescriptors;
    }

    private List<TemplateDescriptorEntity> collectTemplateDescriptors(AbstractContext context, List<TemplateDescriptorEntity> templateDescriptorList) {
        if (context.getTemplates() != null) {
            for (TemplateDescriptorEntity templateDescriptor : context.getTemplates()) {
                templateDescriptor.setOwnerResource(context);
                templateDescriptorList.add(templateDescriptor);
            }
        }
        return templateDescriptorList;
    }

    private void removeTemplate(Integer selectedTemplateId, boolean isResType) throws TemplateNotDeletableException {
		if (isResType) {
            removeDefaultResTypeTemplate(selectedTemplateId);
            //The template is an InstanceResource_Template. Permitted to app_developer
        } else {
            removeDefaultResTemplate(selectedTemplateId);
        }
    }

    @HasPermission(permission = Permission.RESOURCE_TEMPLATE, action = Action.DELETE)
    private void removeDefaultResTemplate(Integer selectedTemplateId) throws TemplateNotDeletableException {
        doRemoveTemplate(selectedTemplateId);
    }

    @HasPermission(permission = Permission.RESOURCETYPE_TEMPLATE, action = Action.DELETE)
    private void removeDefaultResTypeTemplate(Integer selectedTemplateId) throws TemplateNotDeletableException {
        doRemoveTemplate(selectedTemplateId);
    }


    /**
     * Retruns a list of templates with the name templateName
     *
     * @param templateName
     * @return
     */
    private List<TemplateDescriptorEntity> getTemplateListByName(String templateName) {
        ArrayList<TemplateDescriptorEntity> result = (ArrayList<TemplateDescriptorEntity>) entityManager.createQuery("from TemplateDescriptorEntity tde where tde.name=:templateName", TemplateDescriptorEntity.class)
                .setParameter("templateName", templateName).getResultList();
        return result == null ? new ArrayList<TemplateDescriptorEntity>() : result;
    }

    /**
     * Löscht die Template von eine ResourceType.
     *
     * @param selectedTemplateId
     * @throws ResourceTypeNotFoundException
     * @throws TemplateNotDeletableException
     */
    private void doRemoveTemplate(Integer selectedTemplateId) throws TemplateNotDeletableException {
        TemplateDescriptorEntity templateDescriptor = entityManager.find(TemplateDescriptorEntity.class, selectedTemplateId);
        if (templateDescriptor != null && templateDescriptor.getName() != null && SystemCallTemplate.getName().equals(templateDescriptor.getName())) {
            String message = SystemCallTemplate.getName() + " Template can't be deleted!";
            log.info(message);
            throw new TemplateNotDeletableException(message);
        }
        AbstractContext owner = getOwnerOfTemplate(templateDescriptor);
        if (owner != null) {
            owner.removeTemplate(templateDescriptor);
        }
        entityManager.remove(templateDescriptor);
        log.info("Template Id: " + selectedTemplateId + " was deleted successfully.");
    }

    public AbstractContext getOwnerOfTemplate(TemplateDescriptorEntity templateDescriptor) {
        // ContextEntity
        AbstractContext c;
        c = (AbstractContext) getSingleObjectOrNull(
                entityManager.createQuery("select distinct n from ContextEntity n where :templ member of n.templates")
                        .setParameter("templ", templateDescriptor));
        if (c != null) {
            return c;
        }
        c = (AbstractContext) getSingleObjectOrNull(entityManager
                .createQuery("select distinct n from ContextTypeEntity n where :templ member of n.templates")
                .setParameter("templ", templateDescriptor));
        if (c != null) {
            return c;
        }
        c = (AbstractContext) getSingleObjectOrNull(entityManager
                .createQuery("select distinct n from ResourceContextEntity n where :templ member of n.templates")
                .setParameter("templ", templateDescriptor));
        if (c != null) {
            return c;
        }
        c = (AbstractContext) getSingleObjectOrNull(entityManager
                .createQuery(
                        "select distinct n from ResourceRelationContextEntity n where :templ member of n.templates")
                .setParameter("templ", templateDescriptor));
        if (c != null) {
            return c;
        }
        c = (AbstractContext) getSingleObjectOrNull(entityManager
                .createQuery(
                        "select distinct n from ResourceRelationTypeContextEntity n where :templ member of n.templates")
                .setParameter("templ", templateDescriptor));
        if (c != null) {
            return c;
        }
        c = (AbstractContext) getSingleObjectOrNull(entityManager
                .createQuery("select distinct n from ResourceTypeContextEntity n where :templ member of n.templates")
                .setParameter("templ", templateDescriptor));
        return c;
    }

    private Object getSingleObjectOrNull(Query q) {
        try {
            return q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    protected List<TemplateDescriptorEntity> getTemplateDescriptorByName(String templateName) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<TemplateDescriptorEntity> q = cb.createQuery(TemplateDescriptorEntity.class);
        Root<TemplateDescriptorEntity> root = q.from(TemplateDescriptorEntity.class);
        Predicate templateNamePred = cb.like(root.<String>get("name"), templateName);
        q.where(cb.and(templateNamePred));

        TypedQuery<TemplateDescriptorEntity> query = entityManager.createQuery(q);
        return query.getResultList();
    }

    public void renameTestingTemplates(String oldName, String newName) {
        List<TemplateDescriptorEntity> templates = getTemplateDescriptorByName(oldName);
        for (TemplateDescriptorEntity t : templates) {
            t.setName(newName);
            entityManager.merge(t);
            String message = "Template with name '" + oldName + "' renamed to '" + newName + "'";
            log.info(message);
        }
    }
}
