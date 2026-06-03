package ch.puzzle.itc.mobiliar.business.property.boundary;

import ch.puzzle.itc.mobiliar.common.exception.NotFoundException;
import ch.puzzle.itc.mobiliar.common.exception.ResourceNotFoundException;
import ch.puzzle.itc.mobiliar.common.exception.ValidationException;

/**
 * Use case for updating properties on resource relations and resource type relations.
 */
public interface UpdateRelationPropertiesUseCase {

    /**
     * Set a property value on a consumed resource relation for a given context.
     *
     * @param relationId   the resource relation ID
     * @param contextId    the context ID
     * @param propertyName the property technical key
     * @param value        the value to set
     */
    void setPropertyOnResourceRelation(Integer relationId, Integer contextId, String propertyName, String value)
            throws ResourceNotFoundException, ValidationException;

    /**
     * Reset a property value on a consumed resource relation for a given context.
     *
     * @param relationId   the resource relation ID
     * @param contextId    the context ID
     * @param propertyName the property technical key
     */
    void resetPropertyOnResourceRelation(Integer relationId, Integer contextId, String propertyName)
            throws ResourceNotFoundException, ValidationException;

    /**
     * Set a property value on a resource type relation for a given context.
     *
     * @param relTypeId    the resource relation type ID
     * @param contextId    the context ID
     * @param propertyName the property technical key
     * @param value        the value to set
     */
    void setPropertyOnResourceTypeRelation(Integer relTypeId, Integer contextId, String propertyName, String value)
            throws NotFoundException, ValidationException;

    /**
     * Reset a property value on a resource type relation for a given context.
     *
     * @param relTypeId    the resource relation type ID
     * @param contextId    the context ID
     * @param propertyName the property technical key
     */
    void resetPropertyOnResourceTypeRelation(Integer relTypeId, Integer contextId, String propertyName)
            throws NotFoundException, ValidationException;

    /**
     * Update the identifier (relation name) on a consumed resource relation.
     *
     * @param relationId    the resource relation ID
     * @param newIdentifier the new identifier value
     */
    void updateResourceRelationIdentifier(Integer relationId, String newIdentifier)
            throws ResourceNotFoundException, ValidationException;

    /**
     * Update the identifier (relation name) on a resource type relation.
     *
     * @param relTypeId     the resource relation type ID
     * @param newIdentifier the new identifier value
     */
    void updateResourceTypeRelationIdentifier(Integer relTypeId, String newIdentifier)
            throws NotFoundException, ValidationException;
}
