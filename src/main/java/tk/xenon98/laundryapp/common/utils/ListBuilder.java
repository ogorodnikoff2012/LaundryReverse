
package tk.xenon98.laundryapp.common.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class ListBuilder<T> {

    private final List<T> items = new ArrayList<>();

    public List<T> toList() {
        return items;
    }

    public ListBuilder<T> add(final T item) {
        items.add(item);
        return this;
    }

    public ListBuilder<T> add(final Collection<? extends T> items) {
        this.items.addAll(items);
        return this;
    }

    @SafeVarargs
    public final ListBuilder<T> add(final T... items) {
        this.items.addAll(Arrays.asList(items));
        return this;
    }

}
