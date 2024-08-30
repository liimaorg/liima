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

import ch.puzzle.itc.mobiliar.business.domain.commons.CommonDomainService;
import ch.puzzle.itc.mobiliar.business.domain.commons.CommonQueries;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyTypeEntity;
import ch.puzzle.itc.mobiliar.business.security.entity.Permission;
import ch.puzzle.itc.mobiliar.business.security.interceptor.HasPermission;
import ch.puzzle.itc.mobiliar.common.exception.*;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.logging.Logger;

/**
 * A control service for the provision of property type logic
 */
public class PropertyTypeService {

    @Inject
    private Logger log;

    @Inject
    private CommonDomainService commonService;

    @Inject
    private CommonQueries commonQueries;

    @Inject
    private PropertyTagEditingService propertyTagEditingService;

    @Inject
    EntityManager entityManager;

    /**
     * Returns all PropertyTypes with Tags. Because of left join on PropertyTags, PropertyTypes
     * are listed multiple times when having more than one tag.
     * @return List<PropertyTypeEntity>
     */
    public List<PropertyTypeEntity> loadAll() {
        TypedQuery<PropertyTypeEntity> query = entityManager.createQuery("from PropertyTypeEntity ptype " +
                "left join fetch ptype.propertyTags order by LOWER(ptype.propertyTypeName) asc", PropertyTypeEntity.class);
        return query.getResultList();
    }

    @HasPermission(permission = Permission.SAVE_SETTINGS_PROPTYPE)
    public void update(int id, PropertyTypeEntity propertyType)
            throws NotFoundException, ValidationException {

        PropertyTypeEntity propertyTypeEntity = getById(id);
        PropertyTypeEntity propertyByName = getByName(propertyType.getPropertyTypeName());

        if (propertyByName != null && !propertyByName.getId().equals(propertyTypeEntity.getId())) {
            String message = "An other PropertyType with the same Name already exists.";
            throw new ValidationException(message, propertyType);
        }

        propertyTypeEntity.setEncrypt(propertyType.isEncrypt());
        propertyTypeEntity.setValidationRegex(propertyType.getValidationRegex());
        propertyTypeEntity.setPropertyTypeName(propertyType.getPropertyTypeName());

        entityManager.merge(propertyTypeEntity);

        // Update Tags
        propertyTagEditingService.updateTags(propertyType.getPropertyTags(), propertyTypeEntity);
    }

    /**
     * Erstellt einen neuen PropertyType
     *
     * @param propertyType
     * @throws ValidationException
     */
    @HasPermission(permission = Permission.ADD_PROPTYPE)
    public void create(PropertyTypeEntity propertyType) throws ValidationException {

        if (commonService.isUnique(propertyType.getPropertyTypeName())) {
            PropertyTypeEntity propertyTypeEntity = new PropertyTypeEntity();
            propertyTypeEntity.setPropertyTypeName(propertyType.getPropertyTypeName());
            propertyTypeEntity.setEncrypt(propertyType.isEncrypt());
            propertyTypeEntity.setValidationRegex(propertyType.getValidationRegex());
            entityManager.persist(propertyType);

          propertyTagEditingService.updateTags(propertyType.getPropertyTags(), propertyTypeEntity);
        } else {
            throw new ValidationException("Property type already exists.",
                    propertyType);
        }
    }

    /**
     * Löscht den PropertyType anhand der Id.
     *
     * @param id
     * @throws NotFoundException
     * @throws ValidationException
     */
    @HasPermission(permission = Permission.DELETE_PROPTYPE)
    public void deleteById(int id) throws NotFoundException, ValidationException {
        PropertyTypeEntity propertyTypeEntity = getById(id);

        if (!propertyTypeEntity.getPropertyDescriptors().isEmpty()) {
            throw new ValidationException("Could not delete Property type because it is used by properties.", propertyTypeEntity);
        }

        entityManager.remove(propertyTypeEntity);
        log.info("Property type: " + propertyTypeEntity.getPropertyTypeName() + " successfully deleted.");
    }

    private PropertyTypeEntity getById(int id) throws NotFoundException {
        PropertyTypeEntity propertyTypeEntity = entityManager.find(PropertyTypeEntity.class, id);

        if (propertyTypeEntity == null) {
            throw new NotFoundException("Property type with id: " +  id + " has not been found.");
        }
        return propertyTypeEntity;
    }

    public PropertyTypeEntity getByName(String propertyTypeName) {
        PropertyTypeEntity propertyType = null;
        try {
            Query searchPropertyTypeQuery = commonQueries.searchPropertyTypeByName(propertyTypeName);
            propertyType = (PropertyTypeEntity) searchPropertyTypeQuery.getSingleResult();
        } catch (NoResultException nre) {
            String message = "Der ResourceType: " + propertyTypeName + " existiert nicht auf der DB";
            log.info(message);
        }

        return propertyType;
    }
}
