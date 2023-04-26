package com.black.core.sql.code.cascade;

import com.black.core.sql.code.SyntaxFactory;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
public class CascadeExecutor extends SyntaxFactory.SyntaxExecutor {

    private String targetName;

    private String targetKey;

    private String itselfKey;

    private boolean detach = false;

    private CascadeGroup parentGroup;

    public CascadeExecutor() {
        super();
    }

    public CascadeExecutor addCondition(String key, Object value){
        getCondition().put(key, value);
        return this;
    }

    public CascadeExecutor targetName(String targetName) {
        this.targetName = targetName;
        return this;
    }

    public CascadeExecutor targetKey(String targetKey) {
        this.targetKey = targetKey;
        return this;
    }

    public CascadeExecutor itselfKey(String itselfKey) {
        this.itselfKey = itselfKey;
        return this;
    }

    public CascadeExecutor setParentGroup(CascadeGroup parentGroup) {
        this.parentGroup = parentGroup;
        return this;
    }

    @Override
    public List<Map<String, Object>> list() {
        if (!detach && parentGroup != null && parentGroup.isDownward() && parentGroup.getModel() == Model.AUTO){
            detach = true;
            CascadeGroup group = new CascadeGroup(Model.AUTO);
            return group.pointParent(this).list();
        }else {
            return super.list();
        }
    }
}
