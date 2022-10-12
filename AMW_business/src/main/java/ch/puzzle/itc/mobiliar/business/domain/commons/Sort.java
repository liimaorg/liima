package ch.puzzle.itc.mobiliar.business.domain.commons;

import com.google.common.collect.ImmutableList;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.isNullOrEmpty;


public final class Sort implements Iterable<Sort.Order>, Serializable {
    public static final SortingDirectionType DEFAULT_DIRECTION = SortingDirectionType.ASC;

    private final List<Order> orders;


    public static Sort of(Order... orders) {
        return of(ImmutableList.copyOf(orders));
    }


    public static Sort of(List<Order> orders) {
        return new Sort(orders);
    }


    public static Sort of(SortingDirectionType direction, String... properties) {
        return new Sort(ImmutableList.copyOf(properties), direction);
    }

    public static Sort asc(String... properties) {
        return new Sort(ImmutableList.copyOf(properties), SortingDirectionType.ASC);
    }


    public static Sort desc(String... properties) {
        return new Sort(ImmutableList.copyOf(properties), SortingDirectionType.DESC);
    }

    private Sort(List<String> properties, SortingDirectionType direction) {
        checkArgument(properties != null && !properties.isEmpty(), "You must provide at least one sort property to sort by");

        ImmutableList.Builder<Order> listBuilder = ImmutableList.builder();

        for (String property : properties) {
            listBuilder.add(new Order(direction, property));
        }

        this.orders = listBuilder.build();
    }

    private Sort(List<Order> orders) {
        checkNotNull(orders, "orders may not be null");
        this.orders = ImmutableList.copyOf(orders);
    }

    public Sort copyOfWithAdditional(Order order) {
        ImmutableList.Builder<Order> listBuilder = ImmutableList.builder();
        listBuilder.addAll(orders).add(order);
        return new Sort(listBuilder.build());
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

    public static final class Order implements Serializable {
        private final SortingDirectionType direction;
        private final String property;
        private final boolean ignoreCase;


        public static Order asc(String property) {
            return new Order(SortingDirectionType.ASC, property);
        }


        public static Order desc(String property) {
            return new Order(SortingDirectionType.DESC, property);
        }


        public static Order of(SortingDirectionType direction, String property) {
            return new Order(direction, property);
        }

        public static Order of(SortingDirectionType direction, String property, boolean ignoreCase) {
            return new Order(direction, property, ignoreCase);
        }

        private Order(SortingDirectionType direction, String property) {
            this(direction, property, false);
        }

        private Order(SortingDirectionType direction, String property, boolean ignoreCase) {
            checkArgument(!isNullOrEmpty(property), "Property must not be null or empty!");

            this.direction = direction == null ? DEFAULT_DIRECTION : direction;
            this.property = property;
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
