package com.black.sql_v2.with;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author shkstart
 * @create 2023-04-14 14:47
 */
public class Keys {

    private final List<Key> keyList = new ArrayList<>();

    public static Keys of(String... keyNames){
        Key[] keys = Arrays.stream(keyNames).map(Key::new).toArray(Key[]::new);
        return of(keys);
    }

    public static Keys of(Key... keys){
        return new Keys(keys);
    }

    public Keys(Key... keys){
        keyList.addAll(Arrays.asList(keys));
    }

    public List<Key> getKeyList() {
        return keyList;
    }

    public void addKey(Key key){
        keyList.add(key);
    }

    @Override
    public String toString() {
        return keyList.toString();
    }
}
