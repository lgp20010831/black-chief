package com.black.core.spring.component;

import com.black.core.spring.ChiefExpansivelyApplication;
import com.black.core.spring.annotation.LoadSort;
import com.black.core.spring.OpenComponent;
import com.black.core.spring.driver.SortDriver;
import org.springframework.core.annotation.AnnotationUtils;

import java.util.*;
import java.util.stream.Collectors;

public class SortComponent implements SortDriver {
    @Override
    public void sort(Collection<Object> loadCache, Map<Class<? extends OpenComponent>, Object> springLoadComponent, ChiefExpansivelyApplication chiefExpansivelyApplication) {
        final Set<Class<? extends OpenComponent>> classes = springLoadComponent.keySet();
        final List<Class<? extends OpenComponent>> hasSortList = classes.stream()
                .filter(c -> AnnotationUtils.getAnnotation(c, LoadSort.class) != null).collect(Collectors.toList());
        final List<Class<? extends OpenComponent>> noSortList = classes.stream()
                .filter(c -> !hasSortList.contains(c)).collect(Collectors.toList());

        hasSortList.sort(Comparator.comparingInt(o -> Objects.requireNonNull(AnnotationUtils.getAnnotation(o, LoadSort.class)).value()));

        Map<Class<? extends OpenComponent>, Object> newMap = new HashMap<>();
        for (Class<? extends OpenComponent> clazz : hasSortList) {
            loadCache.add(springLoadComponent.get(clazz));
        }
        for (Class<? extends OpenComponent> clazz : noSortList) {
            loadCache.add(springLoadComponent.get(clazz));
        }
    }
}
