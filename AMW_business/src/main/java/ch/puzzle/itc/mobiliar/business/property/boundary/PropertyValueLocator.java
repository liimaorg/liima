package ch.puzzle.itc.mobiliar.business.property.boundary;

import ch.puzzle.itc.mobiliar.business.environment.entity.ContextDependency;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyEntity;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyValueLocation;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceContextEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeContextEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ResourceRelationContextEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ResourceRelationTypeContextEntity;
import ch.puzzle.itc.mobiliar.business.utils.JpaWildcardConverter;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Stateless
public class PropertyValueLocator {

    @Inject
    EntityManager entityManager;

    public List<PropertyValueLocation> findByValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Property value must not be blank");
        }
        String pattern = value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
        List<PropertyValueLocation> locations = new ArrayList<>();
        locations.addAll(findByValue(ResourceContextEntity.class, pattern));
        locations.addAll(findByValue(ResourceTypeContextEntity.class, pattern));
        locations.addAll(findByValue(ResourceRelationContextEntity.class, pattern));
        locations.addAll(findByValue(ResourceRelationTypeContextEntity.class, pattern));
        locations.sort(Comparator.comparing(PropertyValueLocation::getDefinedOn)
                .thenComparing(PropertyValueLocation::getOwnerId)
                .thenComparing(PropertyValueLocation::getContextId)
                .thenComparing(PropertyValueLocation::getPropertyId));
        return locations;
    }

    private <T extends ContextDependency<?>> List<PropertyValueLocation> findByValue(Class<T> definitionType,
                                                                                   String pattern) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = builder.createTupleQuery();
        Root<T> definition = query.from(definitionType);
        Join<T, PropertyEntity> property = definition.join("properties");
        query.multiselect(definition.alias("definition"), property.alias("property"));
        // LIKE without wildcards supports Oracle CLOBs, unlike equality.
        query.where(builder.isFalse(property.get("descriptor").get("encrypt")),
                builder.like(property.get("value"), pattern, JpaWildcardConverter.ESCAPE_CHARACTER));

        List<PropertyValueLocation> locations = new ArrayList<>();
        for (Tuple row : entityManager.createQuery(query).getResultList()) {
            locations.add(new PropertyValueLocation(row.get("property", PropertyEntity.class),
                    row.get("definition", definitionType)));
        }
        return locations;
    }
}
