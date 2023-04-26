package com.black.sql_v2.javassist.aop;

import com.black.config.AttributeUtils;
import com.black.pattern.MethodInvoker;
import com.black.pattern.NameAndValue;
import com.black.core.aop.code.AopTaskIntercepet;
import com.black.core.aop.code.HijackObject;
import com.black.core.chain.GroupKeys;
import com.black.core.log.IoLog;
import com.black.core.log.LogFactory;
import com.black.core.query.MethodWrapper;
import com.black.sql_v2.javassist.SqlV2ProxyRegister;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SqlV2AopIntercept implements AopTaskIntercepet {

    private IoLog log = LogFactory.getArrayLog();

    public void setLog(IoLog log) {
        this.log = log;
    }

    private static SqlV2AopIntercept intercept;

    public synchronized static SqlV2AopIntercept getInstance() {
        if (intercept == null){
            intercept = new SqlV2AopIntercept();
        }
        return intercept;
    }

    @Override
    public Object processor(HijackObject hijack) throws Throwable {
        Method method = hijack.getMethod();
        Class<?> clazz = hijack.getClazz();
        Object[] args = hijack.getArgs();
        Object bean = hijack.getInvocation().getThis();
        //判断当前方法是否可以寻求代理
        if (!SqlV2AopUtils.isAgentable(method)){
            return hijack.doRelease(args);
        }
        //解析自身当前方法, 能够知道作用在哪个表上和对应的数据源别名
        GroupKeys groupKeys = SqlV2AopUtils.parseToTableNameAndAlias(method, clazz, bean);
        if (groupKeys == null){
            return hijack.doRelease(args);
        }
        if (groupKeys.size() != 3) {
            throw new IllegalStateException("not find alias, tableName, methodName: " + groupKeys);
        }
        //如果成功获取到表名和数据源信息, 则去寻找代理方法
        SqlV2ProxyRegister register = SqlV2ProxyRegister.getInstance();
        MethodInvoker methodInvoker = register.getMethodInvoker(groupKeys);
        if (methodInvoker != null){
            log.info("[SqlV2AopIntercept] -- Transfer Agent Method: {}", methodInvoker.getMw().getName());
            //准备数据, 构造入参
            Map<Class<?>, List<NameAndValue>> attr = prepare(groupKeys);
            MethodWrapper mw = methodInvoker.getMw();
            Object[] proxyArgs = AttributeUtils.mergeArgs(mw, method, args, attr);
            return methodInvoker.invoke(proxyArgs);
        }else {
            return hijack.doRelease(args);
        }
    }

    protected Map<Class<?>, List<NameAndValue>> prepare(GroupKeys groupKeys){
        Object[] keys = groupKeys.getKeys();
        String alias = String.valueOf(keys[0]);
        String tableName = String.valueOf(keys[1]);
        String methodName = String.valueOf(keys[2]);
        Map<Class<?>, List<NameAndValue>> attribute = SqlV2AopUtils.prepareAttribute(alias);
        List<NameAndValue> nameAndValues = attribute.computeIfAbsent(String.class, t -> new ArrayList<>());
        nameAndValues.add(new NameAndValue("tableName", tableName));
        nameAndValues.add(new NameAndValue("methodName", methodName));
        nameAndValues.add(new NameAndValue("alias", alias));
        return attribute;
    }



}
