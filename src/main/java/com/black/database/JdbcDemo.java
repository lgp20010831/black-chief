package com.black.database;

import com.black.core.util.Av0;
import org.apache.calcite.DataContext;
import org.apache.calcite.adapter.java.JavaTypeFactory;
import org.apache.calcite.config.CalciteConnectionConfig;
import org.apache.calcite.config.CalciteConnectionProperty;
import org.apache.calcite.config.NullCollation;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.linq4j.AbstractEnumerable;
import org.apache.calcite.linq4j.Enumerable;
import org.apache.calcite.linq4j.Enumerator;

import org.apache.calcite.linq4j.MemoryEnumerator;
import org.apache.calcite.plan.ConventionTraitDef;
import org.apache.calcite.rel.RelDistributionTraitDef;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.schema.*;
import org.apache.calcite.schema.impl.AbstractSchema;
import org.apache.calcite.schema.impl.AbstractTable;
import org.apache.calcite.sql.SqlCall;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.type.SqlTypeName;
import org.apache.calcite.sql.type.SqlTypeUtil;
import org.apache.calcite.tools.FrameworkConfig;
import org.apache.calcite.tools.Frameworks;
import org.apache.calcite.util.Pair;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author 李桂鹏
 * @create 2023-06-26 9:50
 */
@SuppressWarnings("all")
public class JdbcDemo {



    static void parse(){
        SchemaPlus schemaPlus = createSchema();
        FrameworkConfig config = Frameworks.newConfigBuilder()
                .parserConfig(SqlParser.Config.DEFAULT)
                .defaultSchema(schemaPlus)
                .traitDefs(ConventionTraitDef.INSTANCE, RelDistributionTraitDef.INSTANCE)
                .build();
        SqlParser parser = SqlParser.create("select * from user");
    }



    static SchemaPlus createSchema(){
        SchemaPlus rootSchema = Frameworks.createRootSchema(true);
        rootSchema.add("USER", new AbstractTable() {
            @Override
            public RelDataType getRowType(RelDataTypeFactory relDataTypeFactory) {
                RelDataTypeFactory.Builder builder = relDataTypeFactory.builder();
                builder.add("NAME", SqlTypeName.VARCHAR);
                builder.add("ID", SqlTypeName.VARCHAR);
                builder.add("AGE", SqlTypeName.INTEGER);
                return builder.build();
            }
        });

        return rootSchema;
    }


    public static void main(String[] args) throws SQLException {
        List<MemoryColumn> meta = Av0.as(new MemoryColumn<>("id", String.class), new MemoryColumn<>("name", String.class));
        List<List<Object>> data = Av0.as(
                Av0.as("1", "lgp"),
                Av0.as("2", "zs"),
                Av0.as("3", "ww")
        );
        testConnection(meta, data);
    }


    static void testConnection(List<MemoryColumn> meta, List<List<Object>> source) throws SQLException {
        // 构造Schema
        Schema memory = new MemorySchema(meta, source);
        // 设置连接参数
        Properties info = new Properties();
        info.setProperty(CalciteConnectionProperty.DEFAULT_NULL_COLLATION.camelName(), NullCollation.LAST.name());
        info.setProperty(CalciteConnectionProperty.CASE_SENSITIVE.camelName(), "false");
        // 建立连接
        Connection connection = DriverManager.getConnection("jdbc:calcite:", info);
        // 执行查询
        Statement statement = connection.createStatement();
        // 取得Calcite连接
        CalciteConnection calciteConnection = connection.unwrap(CalciteConnection.class);
        calciteConnection.setSchema("memory");
        // 取得RootSchema RootSchema是所有Schema的父Schema
        SchemaPlus rootSchema = calciteConnection.getRootSchema();
        // 添加schema
        rootSchema.add("memory", memory);
        // 编写SQL
        String sql = "select * from memory where COALESCE (id, 0) <> 2 order by id asc";
        ResultSet resultSet = statement.executeQuery(sql);

        while (resultSet.next()){
            System.out.println(resultSet.getString(1)+":"+resultSet.getString(2));
        }

        resultSet.close();
        statement.close();
        connection.close();
    }

    public static class MemorySchema extends AbstractSchema  {

        private Map<String, Table> tableMap;
        private List<MemoryColumn> meta;
        private List<List<Object>> source;

        public MemorySchema(List<MemoryColumn> meta, List<List<Object>> source){
            this.meta = meta;
            this.source = source;
        }

        @Override
        protected Map<String, Table> getTableMap() {
            return Av0.of("memory", new MemoryTable(meta, source));
        }

    }

    public static class MemoryTable extends AbstractTable implements ScannableTable{


        private List<MemoryColumn> meta;
        private List<List<Object>> source;

        public MemoryTable(List<MemoryColumn> meta, List<List<Object>> source){
            this.meta = meta;
            this.source = source;
        }

        @Override
        public Enumerable<Object[]> scan(DataContext dataContext) {
            return new AbstractEnumerable<Object[]>() {
                @Override
                public Enumerator<Object[]> enumerator() {
                    return new MemoryEnumerator(source);
                }
            };
        }

        @Override
        public RelDataType getRowType(RelDataTypeFactory relDataTypeFactory) {
            JavaTypeFactory typeFactory = (JavaTypeFactory) relDataTypeFactory;
            //字段名
            List<String> names = new ArrayList<>();
            //类型
            List<RelDataType> types = new ArrayList<>();
            for(MemoryColumn col : meta){
                names.add(col.getName());
                RelDataType relDataType = typeFactory.createJavaType(col.getType());
                relDataType = SqlTypeUtil.addCharsetAndCollation(relDataType, typeFactory);
                types.add(relDataType);
            }
            return typeFactory.createStructType(Pair.zip(names,types));
        }
    }

    public static class MemoryEnumerator implements Enumerator<Object[]>{

        private List<List<Object>> source;

        private int i = -1;

        private int length;

        public MemoryEnumerator(List<List<Object>> source){
            this.source = source;
            length = source.size();
        }

        @Override
        public Object[] current() {
            List<Object> list = source.get(i);
            return list.toArray();
        }

        @Override
        public boolean moveNext() {
            if(i < length - 1){
                i++;
                return true;
            }
            return false;
        }

        @Override
        public void reset() {
            i = 0;
        }

        @Override
        public void close() {

        }
    }

    public static class MemoryColumn<T> {
        private String name;
        private Class<T> type;

        public MemoryColumn(String name, Class<T> type){
            this.name = name;
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public Class<T> getType() {
            return type;
        }

        public void setType(Class<T> type) {
            this.type = type;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

}
