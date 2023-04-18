package com.epam.healenium.treecomparing;

import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

@UtilityClass
public class StreamUtils {

    public static Function<List<String>, String> joining(String delimiter, String lastDelimiter) {
        return list -> {
            int last = list.size() - 1;
            if (last < 1) {
                return String.join(delimiter, list);
            }
            return String.join(lastDelimiter,
                    String.join(delimiter, list.subList(0, last)),
                    list.get(last));
        };
    }


    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }


    public static <T> Predicate<T> logFiltered(Predicate<T> predicate, Consumer<T> action) {
        Objects.requireNonNull(predicate);
        Objects.requireNonNull(action);

        return value -> {
            if (predicate.test(value)) {
                return true;
            } else {
                action.accept(value);
                return false;
            }
        };
    }

}
