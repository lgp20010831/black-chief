package com.black.core.cache;

import java.util.Set;

public interface ClassScanner {


    Set<Class<?>> scan(String packageName);


}
