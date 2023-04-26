package com.black.ldap;

import com.black.function.Function;
import com.black.core.log.IoLog;
import com.black.core.sql.code.util.SQLUtils;
import com.black.core.util.Assert;
import com.black.core.util.Body;
import com.black.core.util.StreamUtils;
import com.black.core.util.Utils;
import com.black.utils.ServiceUtils;
import lombok.NonNull;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.ConditionCriteria;
import org.springframework.ldap.query.ContainerCriteria;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.query.LdapQueryBuilder;

import javax.naming.directory.Attributes;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import static com.black.ldap.Ldaps.*;

@SuppressWarnings("all")
public class LdapHandler {

    private static LdapHandler handler;

    public static synchronized LdapHandler getInstance() {
        if (handler == null){
            handler = new LdapHandler();
        }
        return handler;
    }

    private Collection<LdapListener> ldapListeners = new LinkedBlockingQueue<>();

    public void registerListener(LdapListener listener){
        ldapListeners.add(listener);
    }

    public LdapObject insert(Map<String, Object> attributeMap){
        if (!attributeMap.containsKey(DN_NAME_SET_MAP)){
            throw new IllegalArgumentException("can not find arg of:" + DN_NAME_SET_MAP);
        }
        return insert(ServiceUtils.getString(attributeMap.get(DN_NAME_SET_MAP)), attributeMap);
    }

    public LdapObject insert(String dn, Map<String, Object> attributeMap){
        return insert(dn, attributeMap, new String[0]);
    }

    public LdapObject insert(String dn, Map<String, Object> attributeMap, String... objectClasses){
        LdapObject ldapObject = new LdapObject(dn);
        for (String objectClass : objectClasses) {
            ldapObject.addObjectClass(objectClass);
        }
        ldapObject.setSource(attributeMap);
        return insert(ldapObject);
    }

    public LdapObject insert(@NonNull LdapObject ldapObject){
        LdapTemplate template = findTemplate();
        for (LdapListener ldapListener : ldapListeners) {
            ldapListener.postInsertLdapObject(ldapObject);
        }
        Body body = ldapObject.tobody();
        IoLog log = getLog();
        log.info("insert ldap object: {}", body);
        Attributes attributes = castMapToAttributes(body);
        template.bind(ldapObject.getDnPath(), null, attributes);
        return ldapObject;
    }

    public void update(@NonNull LdapObject ldapObject){
        LdapTemplate template = findTemplate();
        for (LdapListener ldapListener : ldapListeners) {
            ldapListener.postUpdateLdapObject(ldapObject);
        }
        getLog().info("update ldap: {}", ldapObject.getDnPath());
        template.modifyAttributes(ldapObject.getDnPath(), castObjectToModificationItems(ldapObject));
    }

    public void delete(LdapObject ldapObject){
        if (ldapObject != null){
            delete(ldapObject.getDnPath());
        }
    }

    public void delete(String dn){
        LdapTemplate template = findTemplate();
        getLog().info("delete ldap object: {}", dn);
        template.unbind(dn);
    }

    public Body simpleQueryBody(String syntax){
        return simpleQuery(syntax).tobody();
    }

    public LdapObject simpleQuery(String syntax){
        return query().filterEntry(syntax).singleLdap();
    }

    public List<Body> simpleQueryBodyList(String syntax){
        return query().filterEntry(syntax).list();
    }

    public List<LdapObject> simpleQueryList(String syntax){
        return query().filterEntry(syntax).listLdap();
    }

    public LdapSearchQueryBuilder query(Function<LdapQueryBuilder, LdapQuery> function){
        return new LdapSearchQueryBuilder(function);
    }

    public LdapSearchFilterQuery query(){
        return new LdapSearchFilterQuery(null);
    }

    public LdapSearchFilterQuery query(String baseDn){
        return new LdapSearchFilterQuery(baseDn);
    }

    public ContainerCriteria criteriaMap(@NonNull LdapQueryBuilder builder, Map<String, Object> conditionMap){
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

    protected static abstract class LdapSearchQuery{
        public Body single(){
            return SQLUtils.getSingle(list());
        }

        public LdapObject singleLdap(){
            return SQLUtils.getSingle(listLdap());
        }

        public List<Body> list() {
            return StreamUtils.mapList(listLdap(), ldapObject -> ldapObject.tobody());
        }

        public abstract List<LdapObject> listLdap();
    }


    protected static class LdapSearchQueryBuilder extends LdapSearchQuery {

        LdapQuery ldapQuery;

        final Function<LdapQueryBuilder, LdapQuery> function;

        public LdapSearchQueryBuilder(@NonNull Function<LdapQueryBuilder, LdapQuery> function) {
            this.function = function;
        }

        @Override
        public List<LdapObject> listLdap() {
            LdapQueryBuilder builder = LdapQueryBuilder.query();
            LdapQuery query;
            try {
                query = function.apply(builder);
            } catch (Throwable e) {
                throw new IllegalStateException(e);
            }
            LdapTemplate template = findTemplate();
            getLog().info("search ldap: [base dn: {}, filter: {}]", query.base(), query.filter().encode());
            return template.search(query, contextMapper);
        }
    }

    protected static class LdapSearchFilterQuery extends LdapSearchQuery {
        private String baseDn;
        private String entry;

        public LdapSearchFilterQuery(String baseDn) {
            this.baseDn = baseDn;
            if (this.baseDn == null){
                this.baseDn = "";
            }
        }

        public LdapSearchFilterQuery filterEntry(String e){
            entry = e;
            return this;
        }

        @Override
        public List<LdapObject> listLdap() {
            LdapTemplate template = findTemplate();
            getLog().info("search ldap: [base dn: {}, filter: {}]", baseDn, entry);
            return template.search(baseDn, entry, contextMapper);
        }
    }
    
}
