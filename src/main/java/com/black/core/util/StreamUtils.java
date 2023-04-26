package com.black.core.util;

import lombok.NonNull;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

public class StreamUtils {

    public static <T> List<T> collectList(Stream<T> stream){
        return stream.collect(Collectors.toList());
    }

    public static <T> Set<T> collectSet(Stream<T> stream){
        return stream.collect(Collectors.toSet());
    }

    public static <T> Stream<T> filterStream(Collection<T> collection, Predicate<? super T> predicate){
        if (collection == null){
            return null;
        }
        return collection.stream().filter(predicate);
    }

    public static <T> List<T> filterList(Collection<T> collection, Predicate<? super T> predicate){
        return filterStream(collection, predicate).collect(Collectors.toList());
    }

    public static <T> Set<T> filterSet(Collection<T> collection, Predicate<? super T> predicate){
        return filterStream(collection, predicate).collect(Collectors.toSet());
    }

    public static <T, R> Stream<R> mapStream(Collection<T> collection,  @NonNull Function<? super T, ? extends R> mapper){
        if (collection == null){
            return null;
        }
        return collection.stream().map(mapper);
    }

    public static <T, R> List<R> mapList(Collection<T> collection, @NonNull Function<? super T, ? extends R> mapper){
        if (collection == null){
            return new ArrayList<>();
        }
        return mapStream(collection, mapper).collect(Collectors.toList());
    }

    public static <T, R> Set<R> mapSet(Collection<T> collection, @NonNull Function<? super T, ? extends R> mapper){
        if (collection == null){
            return new HashSet<>();
        }
        return collection.stream().map(mapper).collect(Collectors.toSet());
    }

    public static <T> DoubleStream getDoubleStream(@NonNull Collection<T> collection, ToDoubleFunction<? super T> mapper){
        return collection.stream().mapToDouble(mapper);
    }

    public static <T> IntStream getIntStream(@NonNull Collection<T> collection, ToIntFunction<? super T> mapper){
        return collection.stream().mapToInt(mapper);
    }

    public static <T> LongStream getLongStream(@NonNull Collection<T> collection, ToLongFunction<? super T> mapper){
        return collection.stream().mapToLong(mapper);
    }

    public static <T> double doubleSum(Collection<T> collection, ToDoubleFunction<? super T> mapper){
        if (collection == null){
            return 0;
        }
        DoubleStream doubleStream = getDoubleStream(collection, mapper);
        return doubleStream.sum();
    }
}
