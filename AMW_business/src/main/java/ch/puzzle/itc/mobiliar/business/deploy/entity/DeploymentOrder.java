package ch.puzzle.itc.mobiliar.business.deploy.entity;

import ch.puzzle.itc.mobiliar.business.domain.commons.Sort;
import ch.puzzle.itc.mobiliar.business.domain.commons.Sort.Order;

import java.util.Objects;
import java.util.Set;

import static ch.puzzle.itc.mobiliar.business.deploy.entity.DeploymentFilterTypes.*;

public class DeploymentOrder extends Order {

    public static Set<DeploymentFilterTypes> DEPLOYMENT_FILTER_TYPES_FOR_ORDER =Set.of(ID, TRACKING_ID, DEPLOYMENT_STATE, APPSERVER_NAME, RELEASE, ENVIRONMENT_NAME, DEPLOYMENT_DATE);

    protected DeploymentOrder(String colToSort, Sort.SortingDirectionType sortingDirection, boolean lowerSortCol) {
        super(colToSort, sortingDirection, lowerSortCol);
    }

    public static DeploymentOrder of(String colToSort, Sort.SortingDirectionType sortingDirection, boolean lowerSortCol) {
        Objects.requireNonNull(colToSort, "colToSort may not be null");
        Objects.requireNonNull(sortingDirection, "sortingDirection may not be null");
        return DEPLOYMENT_FILTER_TYPES_FOR_ORDER.stream()
                .map(DeploymentFilterTypes::getFilterTabColumnName)
                .filter(c -> Objects.equals(c, colToSort))
                .findFirst()
                .map(c -> new DeploymentOrder(c, sortingDirection, lowerSortCol))
                .orElseThrow(() -> new IllegalArgumentException("colToSort not found"));
    }
}
