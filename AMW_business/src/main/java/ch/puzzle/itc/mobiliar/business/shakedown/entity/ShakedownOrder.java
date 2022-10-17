package ch.puzzle.itc.mobiliar.business.shakedown.entity;

import ch.puzzle.itc.mobiliar.business.domain.commons.Sort;
import ch.puzzle.itc.mobiliar.business.domain.commons.Sort.Order;

import java.util.Objects;
import java.util.Set;

import static ch.puzzle.itc.mobiliar.business.shakedown.entity.ShakedownTestFilterTypes.*;
import static com.google.common.base.Preconditions.checkNotNull;

public class ShakedownOrder extends Order {

    public static Set<ShakedownTestFilterTypes> SHAKEDOWN_TEST_FILTER_TYPES_FOR_ORDER = Set.of(ID, TRACKING_ID, TEST_STATE, APPSERVER_NAME, APPSERVER_RELEASE, ENVIRONMENT_NAME, TEST_DATE);

    private ShakedownOrder(String colToSort, Sort.SortingDirectionType sortingDirection, boolean lowerSortCol) {
        super(colToSort, sortingDirection, lowerSortCol);
    }

    public static ShakedownOrder of(String colToSort, Sort.SortingDirectionType sortingDirection, boolean lowerSortCol) {
        checkNotNull(colToSort, "colToSort may not be null");
        checkNotNull(sortingDirection, "sortingDirection may not be null");
        return SHAKEDOWN_TEST_FILTER_TYPES_FOR_ORDER.stream()
                .map(ShakedownTestFilterTypes::getFilterTabColumnName)
                .filter(c -> Objects.equals(c, colToSort))
                .findFirst()
                .map(c -> new ShakedownOrder(c, sortingDirection, lowerSortCol))
                .orElseThrow(() -> new IllegalArgumentException("colToSort not found"));
    }
}
