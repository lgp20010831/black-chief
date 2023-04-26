package com.black.core.log;

public class System9vLog extends SystemLoggor implements Log9v{

    public System9vLog(Class<?> clazz) {
        super(clazz);
    }

    @Override
    public String getName() {
        return className;
    }

    @Override
    public void print(String txt, int lazyTimeOut, Object... params) {

    }
}
