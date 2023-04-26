package com.black.blent;

import java.util.ArrayList;
import java.util.List;

public class DefaultBlent implements Blent{

    //平面 = supplier, driver
    protected List<String> planes = new ArrayList<>();

    // = [] / {}
    protected boolean json = true;

    //子数据
    protected List<Blent> blendObjects = new ArrayList<>();

    //别名
    protected String alias;

    @Override
    public List<String> getPlanes() {
        return planes;
    }

    @Override
    public boolean isJson() {
        return json;
    }

    @Override
    public List<Blent> getBlentChilds() {
        return blendObjects;
    }

    @Override
    public String getAlias() {
        return alias;
    }

    @Override
    public void addBlent(Blent blent) {
        blendObjects.add(blent);
    }
}
