package com.black.ldap;

import lombok.NonNull;

public interface LdapListener {


    default void postInsertLdapObject(@NonNull LdapObject ldapObject){

    }

    default void postUpdateLdapObject(@NonNull LdapObject ldapObject){

    }
}
