package com.black.core.sql.code.packet;

import com.black.core.aop.servlet.ParameterWrapper;
import com.black.core.sql.annotation.Param;
import com.black.core.sql.code.config.Configuration;
import com.black.core.sql.code.sqls.BoundStatement;
import com.black.core.sql.code.sqls.SqlValueGroup;
import com.black.sql.SqlOutStatement;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter @Setter
public class ExecutePacket {

    private final Configuration configuration;
    private BoundStatement nhStatement;
    private List<SqlValueGroup> valueGroupList;
    private Object attachment;
    private Object[] args;
    private Map<String, Object> originalArgs = null;
    private ResultPacket rp;

    public ExecutePacket(Configuration configuration) {
        this.configuration = configuration;
    }

    public void registerOriginalArg(String paramName, Object value){
        if (paramName != null){
            init();
            originalArgs.put(paramName, value);
        }
    }

    private void init(){
        if (originalArgs == null){
            originalArgs = new HashMap<>();
            for (ParameterWrapper pw : configuration.getMethodWrapper().getParameterWrappersSet()) {
                if ((pw.getAnnotationSize() == 0  || pw.hasAnnotation(Param.class)) && !pw.getType().equals(BoundStatement.class)) {
                    registerOriginalArg(pw.getName(), args[pw.getIndex()]);
                }
            }
        }
    }

    public Map<String, Object> getOriginalArgs() {
        if (originalArgs == null){
            init();
        }
        return originalArgs;
    }

    public void attach(Object key){
        this.attachment = key;
    }

    public ExecutePacket transfer(ExecutePacket packet){
        setArgs(packet.getArgs());
        setOriginalArgs(packet.getOriginalArgs());
        return this;
    }

    public Object attachment(){
        return attachment;
    }

    public SqlOutStatement getStatement(){
        return getNhStatement().getStatement();
    }

    public void setStatement(SqlOutStatement statement){
        getNhStatement().setStatement(statement);
    }

    public ResultPacket getRp() {
        if (rp == null) rp = new ResultPacket(this);
        return rp;
    }
}
