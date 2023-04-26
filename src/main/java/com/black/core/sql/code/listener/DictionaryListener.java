package com.black.core.sql.code.listener;

import com.black.core.aop.servlet.ParameterWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.sql.annotation.Dictionary;
import com.black.core.sql.annotation.DictionaryString;
import com.black.core.sql.annotation.SQLListener;
import com.black.core.sql.code.GlobalSQLRunningListener;
import com.black.core.sql.code.MapArgHandler;
import com.black.core.sql.code.config.AppearanceConfiguration;
import com.black.core.sql.code.config.Configuration;
import com.black.core.sql.code.config.SyntaxConfigurer;
import com.black.core.sql.code.config.SyntaxManager;
import com.black.core.sql.code.impl.statement_impl.AbstractSelectStatementCreator;
import com.black.core.sql.code.mapping.GlobalMapping;
import com.black.core.sql.code.packet.ExecutePacket;
import com.black.core.sql.code.session.SQLMethodType;
import com.black.core.util.StringUtils;
import com.black.sql.SqlOutStatement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;


@SQLListener @SuppressWarnings("all")
public class DictionaryListener implements GlobalSQLRunningListener {


    @Override
    public SqlOutStatement processorStatement(SqlOutStatement outStatement, Configuration configuration, ExecutePacket arg) {
        String alias = configuration.getDatasourceAlias();
        if (!(configuration instanceof AppearanceConfiguration) &&
                configuration.getMethodType() == SQLMethodType.QUERY &&
                (configuration.getMethodWrapper().hasAnnotation(Dictionary.class) || DictionaryControlManager.isOpen(alias))){
            MethodWrapper mw = configuration.getMethodWrapper();
            Dictionary annotation = mw.getAnnotation(Dictionary.class);
            String[] expressions = getExpressions(annotation, configuration, arg);

            //替换新的sql 语句
            outStatement = createNewStatement(configuration);

            //解析表达式
            for (String expression : expressions) {
                parseExpression(expression, outStatement, arg);
            }
            return outStatement;
        }
        return GlobalSQLRunningListener.super.processorStatement(outStatement, configuration, arg);
    }

    private String[] getExpressions(Dictionary annotation, Configuration configuration, ExecutePacket ep){
        //从注解中获取
        String[] value = annotation == null ? new String[0] : annotation.value();
        List<String> result = new ArrayList<>(Arrays.asList(value));

        //从变量参数中获取
        MethodWrapper mw = configuration.getMethodWrapper();
        ParameterWrapper param = mw.getSingleParameterByAnnotation(DictionaryString.class);
        if (param != null){
            Object arg = ep.getArgs()[param.getIndex()];
            if (arg != null){
                result.addAll(Arrays.asList(parseArg(arg)));
            }
        }

        //从参数 syntax 中获取
        List<String> syntaxs = SyntaxManager.callSyntax(configuration, ep, SyntaxConfigurer::getDictSyntaxs);
        if (syntaxs != null){
            result.addAll(syntaxs);
        }

        //从local syntax 中获取
        List<String> localSyntaxs = SyntaxManager.localSyntax(configuration, SyntaxConfigurer::getDictSyntaxs);
        if (localSyntaxs != null){
            result.addAll(localSyntaxs);
        }
        return result.toArray(new String[0]);
    }


    private String[] parseArg(Object arg){
        if (arg instanceof String[]){
            return (String[]) arg;
        }

        if (arg instanceof Collection){
            Collection<?> collection = (Collection<?>) arg;
            return collection.toArray(new String[0]);
        }

        if (arg instanceof String){
            return new String[]{(String) arg};
        }

        String str = arg.toString();
        return new String[]{str};
    }

    private SqlOutStatement createNewStatement(Configuration configuration){
        return AbstractSelectStatementCreator.doCreateSelectStatement(configuration, true);
    }

    private void parseExpression(String expression, SqlOutStatement outStatement, ExecutePacket arg){
        if (arg.getConfiguration().getDynamicParseMapping()) {
            expression = GlobalMapping.dynamicParse(expression, arg.getOriginalArgs());
        }else {
            expression = GlobalMapping.parseAndObtain(expression, true);
        }
        expression = MapArgHandler.parseSql(expression, arg.getOriginalArgs());
        if (StringUtils.hasText(expression)){
            String[] fragments = expression.split("\\|");
            for (int i = 0; i < fragments.length; i++) {
                String fragment = fragments[i];
                switch (i){
                    case 0:
                        outStatement.writeReturnColumns(fragment);
                        break;
                    case 1:
                        outStatement.writePre(fragment);
                        break;
                    case 2:

                        boolean join = true;
                        fragment = StringUtils.removeFrontSpace(fragment);
                        if (fragment.startsWith("and") || fragment.startsWith("or")){
                            join = false;
                        }
                        if (join){
                            outStatement.writeAft("and");
                        }
                        outStatement.writeAft(fragment);
                        break;
                    case 3:
                        outStatement.writeLastSql(fragment);
                        break;
                }
            }
        }
    }
}
