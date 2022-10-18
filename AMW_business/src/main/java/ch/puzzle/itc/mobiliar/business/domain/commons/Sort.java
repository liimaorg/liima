package ch.puzzle.itc.mobiliar.business.domain.commons;

import com.google.common.collect.ImmutableList;
import lombok.Builder;
import lombok.Singular;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.isNullOrEmpty;


@Builder
public final class Sort implements Iterable<Sort.Order> {
    public static final SortingDirectionType DEFAULT_DIRECTION = SortingDirectionType.ASC;

    @Singular
    private final List<Order> orders;

    private Sort(List<Order> orders) {
        checkNotNull(orders, "orders may not be null");
        this.orders = ImmutableList.copyOf(orders);
    }

    public static Sort nothing() {
        return new Sort(Collections.emptyList());
    }

    @Override
    public Iterator<Order> iterator() {
        return this.orders.iterator();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object instanceof Sort) {
            Sort that = (Sort) object;
            return Objects.equals(this.orders, that.orders);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(orders);
    }

    @Override
    public String toString() {
        return orders.toString();
    }

    public enum SortingDirectionType {
        ASC, DESC
    }

    public static abstract class Order {
        private final SortingDirectionType direction;
        private final String property;
        private final boolean ignoreCase;


        protected Order(String property, SortingDirectionType direction, boolean ignoreCase) {
            checkArgument(!isNullOrEmpty(property), "Property must not be null or empty!");

            this.property = property;
            this.direction = direction == null ? DEFAULT_DIRECTION : direction;
            this.ignoreCase = ignoreCase;
        }


        public SortingDirectionType getDirection() {
            return direction;
        }

        public String getProperty() {
            return property;
        }

        public boolean isIgnoreCase() {
            return ignoreCase;
        }


        @Override
        public int hashCode() {
            return Objects.hash(direction, property);
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object instanceof Order) {
                Order that = (Order) object;
                return Objects.equals(this.direction, that.direction) && Objects.equals(this.property, that.property);
            }
            return false;
        }

        @Override
        public String toString() {
            return String.format("%s: %s", property, direction);
        }
    }
}
