package com.black.ldap;

import com.black.function.Function;
import com.black.holder.SpringHodler;
import com.black.core.log.CommonLog4jLog;
import com.black.core.log.IoLog;
import com.black.core.query.ClassWrapper;
import com.black.core.query.FieldWrapper;
import com.black.core.sql.code.util.SQLUtils;
import com.black.core.tools.BeanUtil;
import com.black.core.util.Assert;
import com.black.core.util.Body;
import com.black.core.util.Utils;
import com.black.utils.ServiceUtils;
import lombok.NonNull;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;
import org.springframework.ldap.query.ConditionCriteria;
import org.springframework.ldap.query.ContainerCriteria;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.query.LdapQueryBuilder;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;
import java.util.*;

@SuppressWarnings("all")
public class Ldaps {

    public static IoLog log = new CommonLog4jLog();

    public static String DN_NAME_SET_MAP = "dnPath";

    public static final String OBJECT_CLASS = "objectClass";

    private static BeanFactory beanFactory;

    public static ContextMapper<LdapObject> contextMapper = new ContextMapper<LdapObject>() {
        @Override
        public LdapObject mapFromContext(Object object) throws NamingException {
            if (!(object instanceof DirContextAdapter)){
                throw new UnsupportedOperationException("无法处理 context of:" + object);
            }
            DirContextAdapter contextAdapter = (DirContextAdapter) object;
            Attributes attributes = contextAdapter.getAttributes();
            NamingEnumeration<? extends Attribute> all = attributes.getAll();
            final String dnString = contextAdapter.getDn().toString();
            LdapObject ldapObject = new LdapObject(dnString);
            Body map = new Body();
            while (all.hasMore()) {
                Attribute next = all.next();
                String id = next.getID();
                Object nextValue = next.get();
                if (map.containsKey(id)){
                    Object val = map.get(id);
                    if (val instanceof List){
                        ((List<Object>) val).add(nextValue);
                    }else {
                        Object firstValue = map.remove(id);
                        List<Object> list;
                        map.put(id, list = new ArrayList<>());
                        list.add(firstValue);
                        list.add(nextValue);
                    }
                }else {
                    map.put(id, nextValue);
                }
            }

            if (!map.containsKey(DN_NAME_SET_MAP)){
                map.put(DN_NAME_SET_MAP, dnString);
            }
            ldapObject.setSource(map);
            return ldapObject;
        }
    };

    private static LdapTemplate template;

    public static LdapHandler getHandler(){
        return LdapHandler.getInstance();
    }

    public static void registerListener(LdapListener listener){
        getHandler().registerListener(listener);
    }

    public static LdapObject insertBean(Object bean){
        LdapObject ldapObject = parseEntryToLdapObject(bean);
        return insert(ldapObject);
    }

    public static LdapObject insert(Map<String, Object> attributeMap){
        return getHandler().insert(attributeMap);
    }

    public static LdapObject insert(String dn, Map<String, Object> attributeMap){
        return getHandler().insert(dn, attributeMap);
    }

    public static LdapObject insert(String dn, Map<String, Object> attributeMap, String... objectClasses){
        return getHandler().insert(dn, attributeMap, objectClasses);
    }

    public static LdapObject insert(LdapObject ldapObject){
        return getHandler().insert(ldapObject);
    }

    public static void updateBean(Object bean){
        update(parseEntryToLdapObject(bean));
    }

    public static void update(@NonNull LdapObject ldapObject){
        getHandler().update(ldapObject);
    }

    public static void deleteBean(Object bean){
        delete(parseEntryToLdapObject(bean));
    }

    public static void delete(LdapObject ldapObject){
        getHandler().delete(ldapObject);
    }

    public static void delete(String dn){
        getHandler().delete(dn);
    }

    public static Body simpleQueryBody(Map<String, Object> condition){
        return simpleQueryBody(getEntry(condition));
    }
    public static Body simpleQueryBody(String syntax){
        return getHandler().simpleQueryBody(syntax);
    }

    public static LdapObject simpleQuery(Map<String, Object> condition){
        return simpleQuery(getEntry(condition));
    }

    public static LdapObject simpleQuery(String syntax){
        return getHandler().simpleQuery(syntax);
    }

    public static List<Body> simpleQueryBodyList(Map<String, Object> condition){
        return simpleQueryBodyList(getEntry(condition));
    }

    public static List<Body> simpleQueryBodyList(String syntax){
        return getHandler().simpleQueryBodyList(syntax);
    }

    public static List<LdapObject> simpleQueryList(Map<String, Object> condition){
        return simpleQueryList(getEntry(condition));
    }
    public static List<LdapObject> simpleQueryList(String syntax){
        return getHandler().simpleQueryList(syntax);
    }

    public static LdapHandler.LdapSearchQueryBuilder query(Function<LdapQueryBuilder, LdapQuery> function){
        return getHandler().query(function);
    }

    public static LdapHandler.LdapSearchFilterQuery query(){
        return getHandler().query();
    }

    public static LdapHandler.LdapSearchFilterQuery query(String baseDn){
        return getHandler().query(baseDn);
    }

    public static ContainerCriteria criteriaMap(@NonNull LdapQueryBuilder builder, Map<String, Object> conditionMap){
        if (Utils.isEmpty(conditionMap)){
            throw new IllegalStateException("condition map is null");
        }
        ConditionCriteria criteria = null;
        ContainerCriteria containerCriteria = null;
        for (String id : conditionMap.keySet()) {
            Object value = conditionMap.get(id);
            if (criteria == null && containerCriteria == null){
                criteria = builder.where(id);
                containerCriteria = criteria.is(ServiceUtils.getString(value));
            }else if (criteria != null && containerCriteria != null){
                criteria = containerCriteria.and(id);
                containerCriteria = criteria.is(ServiceUtils.getString(value));
            }else {
                throw new IllegalStateException("ill state:" + criteria + " | " + containerCriteria);
            }
        }
        Assert.notNull(containerCriteria, "ill state of null container criteria");
        return containerCriteria;
    }

    public static void setLog(IoLog log) {
        Ldaps.log = log;
    }

    public static IoLog getLog() {
        return log;
    }

    public static LdapTemplate findTemplate(){
        if (template != null) return template;
        if (beanFactory == null){
            beanFactory = SpringHodler.getBeanFactory();
        }
        Assert.notNull(beanFactory, "not find spring bean factory");
        template = beanFactory.getBean(LdapTemplate.class);
        return template;
    }

    public static LdapObject parseEntryToLdapObject(@NonNull Object entry){
        Class<Object> primordialClass = BeanUtil.getPrimordialClass(entry);
        ClassWrapper<Object> classWrapper = ClassWrapper.get(primordialClass);
        Entry entryAnn = classWrapper.getAnnotation(Entry.class);
        Assert.notNull(entryAnn, "entry object must annotation @Entry");
        FieldWrapper idfw = classWrapper.getSingleFieldByAnnotation(Id.class);
        Assert.notNull(idfw, "can not find field annotation @Id");
        Object value = idfw.getValue(entry);
        LdapObject ldapObject = new LdapObject(ServiceUtils.getString(value));
        String[] ocs = entryAnn.objectClasses();
        for (String oc : ocs) {
            ldapObject.addObjectClass(oc);
        }
        Map<String, Object> map = parseEntryToMap(entry);
        ldapObject.setSource(map);
        return ldapObject;
    }

    public static Map<String, Object> parseEntryToMap(@NonNull Object entry){
        Class<Object> primordialClass = BeanUtil.getPrimordialClass(entry);
        ClassWrapper<Object> classWrapper = ClassWrapper.get(primordialClass);
        Collection<FieldWrapper> fields = classWrapper.getFields();
        Map<String, Object> map = new HashMap<>();
        for (FieldWrapper fw : fields) {
            org.springframework.ldap.odm.annotations.Attribute annotation = fw.getAnnotation(org.springframework.ldap.odm.annotations.Attribute.class);
            if (annotation != null){
                map.put(annotation.name(), fw.getValue(entry));
            }

        }
        return map;
    }

    public static Attributes castMapToAttributes(@NonNull Map<String, Object> map){
        if (!map.containsKey(OBJECT_CLASS)){
            throw new IllegalStateException("map must has objectClass");
        }
        BasicAttributes basicAttributes = new BasicAttributes();
        BasicAttribute basicAttribute = new BasicAttribute(OBJECT_CLASS);
        Object oc = map.get(OBJECT_CLASS);
        List<Object> list = SQLUtils.wrapList(oc);
        for (Object o : list) {
            basicAttribute.add(o);
        }

        basicAttributes.put(basicAttribute);
        if (map.containsKey(DN_NAME_SET_MAP)){
            map.remove(DN_NAME_SET_MAP);
        }

        if (map.containsKey(OBJECT_CLASS)){
            map.remove(OBJECT_CLASS);
        }
        return putMapOnAttributes(basicAttributes, map);
    }

    public static Attributes putMapOnAttributes(@NonNull Attributes attributes, Map<String, Object> map){
        if (map == null) return attributes;
        map.forEach(attributes::put);
        return attributes;
    }

    public static ModificationItem[] castObjectToModificationItems(Object obj){
        if (obj == null){
            return new ModificationItem[0];
        }
        Map<String, Object> valueMap = new HashMap<>();
        if (obj instanceof Map){
            valueMap.putAll((Map<? extends String, ?>) obj);
            if (valueMap.containsKey(DN_NAME_SET_MAP)){
                valueMap.remove(DN_NAME_SET_MAP);
            }

            if (valueMap.containsKey(OBJECT_CLASS)){
                valueMap.remove(OBJECT_CLASS);
            }
        }else {
            Class<Object> primordialClass = BeanUtil.getPrimordialClass(obj);
            ClassWrapper<Object> classWrapper = ClassWrapper.get(primordialClass);
            Collection<FieldWrapper> fields = classWrapper.getFields();
            for (FieldWrapper field : fields) {
                org.springframework.ldap.odm.annotations.Attribute annotation = field.getAnnotation(org.springframework.ldap.odm.annotations.Attribute.class);
                String name = annotation == null ? field.getName() : annotation.name();
                Object value = field.getValue(obj);
                valueMap.put(name, value);
            }
        }
        ModificationItem[] items = new ModificationItem[valueMap.size()];
        int i = 0;
        for (String name : valueMap.keySet()) {
            Object val = valueMap.get(name);
            items[i++] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute(name, val));
        }
        return items;
    }

    public static String getEntry(Map<String, Object> condition){
        if (Utils.isEmpty(condition)){
            return null;
        }
        StringJoiner joiner = new StringJoiner("", "(&", ")");
        for (String key : condition.keySet()) {
            Object val = condition.get(key);
            String value = val == null ? "" : val.toString();
            joiner.add("(" + key + "=" + value + ")");
        }
        return joiner.toString();
    }
}
