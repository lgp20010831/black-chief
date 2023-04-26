package com.black.ldap;

import com.black.premission.AttributeBean;
import com.black.core.util.Body;

import java.util.*;

public class LdapObject extends AttributeBean<LdapObject> {

    private final String dnPath;

    private List<String> objectClasses = new ArrayList<>();

    public LdapObject(String dnPath) {
        this.dnPath = dnPath;
    }

    @Override
    public void setSource(Map<String, Object> source) {
        Map<String, Object> attributes = attributes();
        attributes.putAll(source);
        if (source.containsKey(Ldaps.OBJECT_CLASS)){
            Object objectClass = source.get(Ldaps.OBJECT_CLASS);
            if (objectClass != null){
                if (objectClass instanceof List){
                    objectClasses.addAll((Collection<? extends String>) objectClass);
                }else {
                    objectClasses.add(objectClass.toString());
                }
            }
        }
    }

    public LdapObject addObjectClass(String objectClass){
        objectClasses.add(objectClass);
        return this;
    }

    public List<String> getObjectClasses() {
        return objectClasses;
    }

    public String getObjectClassString(){
        StringJoiner joiner = new StringJoiner(",");
        for (String objectClass : objectClasses) {
            joiner.add(objectClass);
        }
        return joiner.toString();
    }

    public String getDnPath() {
        return dnPath;
    }

    public Body tobody(){
        Map<String, Object> attributes = attributes();
        if (!attributes.containsKey(Ldaps.OBJECT_CLASS)){
            ArrayList<Object> list = new ArrayList<>(objectClasses);
            attributes.put(Ldaps.OBJECT_CLASS, list);
        }
        return new Body(attributes);
    }
}
