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

package ch.puzzle.itc.mobiliar.business.property.control;


import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;

import ch.puzzle.itc.mobiliar.business.property.entity.PropertyTagEntity;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyTagEntityHolder;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyTagType;
import ch.puzzle.itc.mobiliar.common.exception.AMWRuntimeException;

import static ch.puzzle.itc.mobiliar.business.property.entity.PropertyTagType.LOCAL;

public class PropertyTagEditingService {

    @Inject
    EntityManager entityManager;

    @Inject
    private Logger log;

    /**
     * Returns a list containing all existing GLOBAL PropertyTagEntities
     * @param sortDesc
     * @return
     */
    public List<PropertyTagEntity> loadAllGlobalPropertyTagEntities(boolean sortDesc) {
        return loadAllPropertyTagEntities(sortDesc, PropertyTagType.GLOBAL);
    }

    /**
     * Returns a list containing all existing LOCAL PropertyTagEntities
     * @param sortDesc
     * @return
     */
    public List<PropertyTagEntity> loadAllLocalPropertyTagEntities(boolean sortDesc) {
        return loadAllPropertyTagEntities(sortDesc, LOCAL);
    }

    /**
     * Persists a new PropertyTag.
     * If the PropertyTag to be persisted has PropertyTagType.GLOBAL,
     * it assures that its name is unique
     * @param propertyTag
     */
    public PropertyTagEntity addPropertyTag(PropertyTagEntity propertyTag) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Object> query = cb.createQuery();
        Root<PropertyTagEntity> from = query.from(PropertyTagEntity.class);
        CriteriaQuery<Object> select = query.select(from);

        if (propertyTag.getTagType().equals(PropertyTagType.GLOBAL)) {
            Predicate predicate1 = cb.equal(from.get("name"), propertyTag.getName());
            Predicate predicate2 = cb.equal(from.get("tagType"), propertyTag.getTagType());
            query.where(predicate1, predicate2);
            TypedQuery<Object> tq = entityManager.createQuery(select);
            List<Object> result = tq.getResultList();

            if (result.isEmpty()) {
                entityManager.persist(propertyTag);
            } else {
                throw new AMWRuntimeException("Tag " + propertyTag.getName() + " already exists");
            }
        }
        else {
            entityManager.persist(propertyTag);
            log.info("PropertyTag " + propertyTag.getName() + " persisted");
        }
        return propertyTag;
    }

    /**
     * Deletes a PropertyTagEntity with the given id
     * @param propertyTagId
     */
    public boolean deletePropertyTagById(Integer propertyTagId) {
        PropertyTagEntity propertyTag;
        try {
            propertyTag = entityManager.find(PropertyTagEntity.class, propertyTagId);
            entityManager.remove(propertyTag);
        } catch (IllegalArgumentException e) {
            throw e;
        }
        log.info("PropertyTag " + propertyTag.getName() + " removed");
        return true;
    }

    private Order getDefaultOrder(Root<PropertyTagEntity> root, boolean desc) {
        if (desc) {
            return entityManager.getCriteriaBuilder().desc(root.get("name"));
        }
        return entityManager.getCriteriaBuilder().asc(root.get("name"));
    }

    private List<PropertyTagEntity> loadAllPropertyTagEntities(boolean sortDesc, PropertyTagType tagType) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<PropertyTagEntity> query = cb.createQuery(PropertyTagEntity.class);
        Root<PropertyTagEntity> root = query.from(PropertyTagEntity.class);
        if (tagType != null) {
            Predicate predicate = cb.equal(root.get("tagType"), tagType);
            query.where(predicate);
        }
        query.orderBy(getDefaultOrder(root, sortDesc));
        return entityManager.createQuery(query).getResultList();
    }

    /**
     * converts a comma separated String to PropertyTagEntities
     *
     * @param propertyTagsString
     * @return
     */
    public List<PropertyTagEntity> convertToTags(String propertyTagsString) {
        List<PropertyTagEntity> ptes = new ArrayList<>();
        if (propertyTagsString != null && !propertyTagsString.isEmpty()) {
            String[] tagStrings = propertyTagsString.split(",");
            for (String tagString : tagStrings) {
                ptes.add(createPropertyTagEntity(tagString));
            }
        }
        return ptes;
    }

    /**
     * creates a (local)  PropertyTagEntity from a given String
     *
     * @param tagString
     * @return
     */
    public PropertyTagEntity createPropertyTagEntity(String tagString) {
        return createPropertyTagEntity(tagString, LOCAL);
    }

    public PropertyTagEntity createPropertyTagEntity(String tagString, PropertyTagType type) {
        PropertyTagEntity pte = new PropertyTagEntity();
        pte.setName(tagString);
        pte.setTagType(type);
        return pte;
    }

    /**
     * Updates the Tags of the given tagHolder
     *
     * @param newTags
     * @param tagHolder
     */
    public void updateTags(List<PropertyTagEntity> newTags, PropertyTagEntityHolder tagHolder){

        // add new tags
        for (PropertyTagEntity tag : newTags) {
            if (!hasName(tagHolder.getPropertyTags(),tag.getName())) {
                entityManager.persist(tag);
                tagHolder.addPropertyTag(tag);
            }
        }
        // remove missing tags
        if(tagHolder.getPropertyTags() != null) {

            List<PropertyTagEntity> toRemove = new ArrayList<>();

            for (PropertyTagEntity pt : tagHolder.getPropertyTags()) {
                if (!hasName(newTags,pt.getName())) {
                    entityManager.remove(pt);
                    toRemove.add(pt);
                }
            }
            // remove them
            for(PropertyTagEntity pt: toRemove){
                tagHolder.removePropertyTag(pt);
            }
        }
    }

    private boolean hasName(List<PropertyTagEntity> tags, String tagName) {

        for (PropertyTagEntity pt : tags) {
            if (pt.getName().equals(tagName)) {
                return true;
            }
        }
        return false;
    }

}