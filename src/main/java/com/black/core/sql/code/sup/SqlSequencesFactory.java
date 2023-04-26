package com.black.core.sql.code.sup;

import com.black.core.factory.beans.BeanFactory;
import com.black.core.factory.manager.FactoryManager;
import com.black.core.json.ReflexUtils;
import com.black.core.sql.code.sup.impl.*;
import com.black.core.sql.unc.OperationType;
import com.black.core.tools.BeanUtil;
import com.black.core.util.Av0;
import com.black.sql.SqlOutStatement;
import com.black.sql.SqlWriter;
import com.black.table.TableMetadata;
import com.black.vfs.VFS;
import com.black.vfs.VfsScanner;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class SqlSequencesFactory {


    private static final Map<String, Map<OperationType, SqlSeqParser>> cache = new ConcurrentHashMap<>();

    private static Collection<Class<?>> implClasses;

    private static LinkedBlockingQueue<SqlSeqParser> parsers;

    private static VfsScanner vfsScanner;

    private static BeanFactory factory;

    public static boolean scan = false;

    private static void init(){
        if (vfsScanner == null){
            vfsScanner = VFS.findVfsScanner();
        }

        if (implClasses == null){
            implClasses = getImplClasses0();
        }

        if (factory == null){
            FactoryManager.init();
            factory = FactoryManager.getBeanFactory();
        }

        if(parsers == null){
            parsers = new LinkedBlockingQueue<>();
            for (Class<?> implClass : implClasses) {
                if (BeanUtil.isSolidClass(implClass)){
                    parsers.add((SqlSeqParser) ReflexUtils.instance(implClass));
                }
            }
        }
    }

    public static Collection<Class<?>> getImplClasses0(){
        if (scan && vfsScanner != null){
            return vfsScanner.load("com.black.core.sql.code.sup.impl");
        }else {
            ArrayList<Class<?>> list = new ArrayList<>();
            list.add(DYDYSeqParser.class);
            list.add(XYDYSeqParser.class);
            list.add(DYXYSeqParser.class);
            list.add(DYSeqParser.class);
            list.add(XYSeqParser.class);
            list.add(EqSeqParser.class);
            list.add(LikeSeqParser.class);
            list.add(InSeqParser.class);
            list.add(NotInSeqParser.class);
            list.add(IsNullSeqParser.class);
            list.add(IsNotNullSeqParser.class);
            return list;
        }
    }

    public static void parseSeq(SqlOutStatement statement,
                                String seq, OperationType type,
                                Map<String, Object> argMap,
                                TableMetadata metadata){
        init();
        Map<OperationType, SqlSeqParser> typeSqlSeqParserMap = cache.computeIfAbsent(seq, s -> {
            return new ConcurrentHashMap<>();
        });
        SqlSeqParser seqParser = typeSqlSeqParserMap.computeIfAbsent(type, t -> {
            for (SqlSeqParser parser : parsers) {
                if (parser.support(seq, type)) {
                    return parser;
                }
            }
            return null;
        });
        if (seqParser != null){
            seqParser.doParse(seq, statement, type, argMap, metadata);
        }
    }

    public static void parseSeq(SqlOutStatement statement, String seq, OperationType type, Map<String, Object> argMap){
        parseSeq(statement, seq, type, argMap, null);
    }

    public static void main(String[] args) {
        String seq = "time in #{!!map.startTime}";
        Map<String, Object> param = Av0.of("map", Av0.of("startTime", Av0.ar(1, 2, 3)));
        SqlOutStatement statement = SqlWriter.select("ayc");
        parseSeq(statement, seq, OperationType.SELECT, param);
        statement.flush();
        System.out.println(statement);
    }

}
