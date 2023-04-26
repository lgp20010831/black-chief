package com.black.core.chain;

import com.black.core.util.Av0;

import java.util.*;


public final class GroupKeys {

    private final Object[] keys;

    public GroupKeys(Object... keys) {
        this.keys = keys;
    }

    public Object[] getKeys() {
        return keys;
    }

    public Set<?> getKeySet(){
        return new HashSet<>(Av0.as(getKeys()));
    }

    public List<?> getKeyList(){
        return Av0.as(getKeys());
    }

    public int size(){
        return keys.length;
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner(",", "[", "]");
        for (Object key : keys) {
            joiner.add(String.valueOf(key));
        }
        return joiner.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null)return false;
        if (o instanceof GroupKeys){
            GroupKeys gk = (GroupKeys) o;
            Object[] keys = gk.getKeys();
            if (keys.length != this.keys.length){
                return false;
            }
            for (int i = 0; i < this.keys.length; i++) {
                Object ykey = this.keys[i];
                Object nkey = keys[i];
                if (ykey == null && nkey == null){
                    continue;
                }
                if (ykey != null && nkey == null){
                    return false;
                }
                if (nkey != null && ykey == null){
                    return false;
                }
                if (!nkey.equals(ykey)){
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = 56;
        for (Object key : keys) {
            if (key != null){
                result = 1 << 3 * result + Objects.hash(key);
            }
        }
        return result;
    }
}
