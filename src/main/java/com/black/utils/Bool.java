package com.black.utils;

import com.black.function.Consumer;
import com.black.core.tools.BeanUtil;

@SuppressWarnings("all")
public class Bool<T> {

    private final Object instance;

    public Bool(Object instance) {
        this.instance = instance;
    }

    public T getInstance() {
        return (T) instance;
    }


    public Branch<T, Bool<T>> instanceOf(Class<T> type){
        boolean result = instance != null;
        if (result){
            result = type.isAssignableFrom(BeanUtil.getPrimordialClass(instance));
        }
        return new Branch<>(this, result);
    }

    public static class Branch<V, B extends Bool<V>>{

        private final B originator;

        private final boolean result;

        Branch(B originator, boolean result) {
            this.originator = originator;
            this.result = result;
        }

        public B then(Consumer<V> consumer){
            try {
                if (result)
                    consumer.accept((V) originator.getInstance());
            } catch (Throwable e) {
                throw new IllegalStateException(e);
            }
            return originator;
        }

        public B or(Consumer<Object> consumer){
            try {
                if (!result)
                    consumer.accept(originator.getInstance());
            } catch (Throwable e) {
                throw new IllegalStateException(e);
            }
            return originator;
        }
    }

    public static <T> Branch<T, Bool<T>> instanceOf(Object bean, Class<T> type){
        Bool<T> bool = new Bool<>(bean);
        return bool.instanceOf(type);
    }


}
