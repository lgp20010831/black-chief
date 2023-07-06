package com.black.word;

import com.black.core.sql.code.util.SQLUtils;
import com.black.core.util.StringUtils;
import com.black.utils.ServiceUtils;
import com.deepoove.poi.data.MiniTableRenderData;
import com.deepoove.poi.data.RowRenderData;
import com.deepoove.poi.data.style.TableStyle;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STJc;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@SuppressWarnings("all")
public class Table {

    public static MiniTableRenderData simple(Object param, String headerInfo){
        return make(param, headerInfo).create();
    }

    public static TableBuilder make(Object param, String headerInfo){
        return new TableBuilder(param, headerInfo);
    }

    public static class TableBuilder{
        private final String headerInfo;

        private final List<Object> tableData;

        String commonBgColor;

        String commonAlign = "center";

        String[] headers;

        String noDatadesc;

        float width;

        String headerAlign = "center";

        String headerBgColor;

        Function<Object, List<Object>> function;

        public TableBuilder(Object param, String headerInfo) {
            this.headerInfo = headerInfo;
            tableData = SQLUtils.wrapList(param);
            if (!StringUtils.hasText(headerInfo)){
                throw new IllegalStateException("不能缺少标题信息");
            }
            String[] headerAndMappings = headerInfo.split("\\|");
            Map<String, String> nameWithMapping = new LinkedHashMap<>();
            for (String headerAndMapping : headerAndMappings) {
                if (headerAndMapping.contains("(") && headerAndMapping.contains(")")){
                    int index = headerAndMapping.indexOf("(");
                    String name = headerAndMapping.substring(0, index);
                    String mapping = headerAndMapping.substring(index + 1, headerAndMapping.length() - 1);
                    nameWithMapping.put(name.trim(), mapping.trim());
                }else {
                    nameWithMapping.put(headerAndMapping, headerAndMapping);
                }
            }
            headers = nameWithMapping.keySet().toArray(new String[0]);
            function = new Function<Object, List<Object>>() {
                @Override
                public List<Object> apply(Object t) {
                    ArrayList<Object> list = new ArrayList<>();
                    for (String name : nameWithMapping.keySet()) {
                        String mapping = nameWithMapping.get(name);
                        Object value = ServiceUtils.getByExpression(t, mapping);
                        list.add(value);
                    }
                    return list;
                }
            };
        }



        public TableBuilder headers(String... headers){
            this.headers = headers;
            return this;
        }


        public TableBuilder noDatadesc(String desc){
            noDatadesc = desc;
            return this;
        }

        public TableBuilder width(float width){
            this.width = width;
            return this;
        }

        public TableBuilder headerAlign(String headerAlign){
            this.headerAlign = headerAlign;
            return this;
        }

        public TableBuilder headerBgColor(String headerBgColor){
            this.headerBgColor = headerBgColor;
            return this;
        }

        public MiniTableRenderData create(){
            MiniTableRenderData renderData = WordUtils.simpleCreateMiniTableRenderData(tableData, commonAlign, commonBgColor, function, headers);
            renderData.setNoDatadesc(noDatadesc);
            renderData.setWidth(width);
            RowRenderData header = renderData.getHeader();
            TableStyle headerStyle = new TableStyle();
            headerStyle.setAlign(STJc.Enum.forString(headerAlign));
            headerStyle.setBackgroundColor(headerBgColor);
            header.setRowStyle(headerStyle);
            return renderData;
        }
    }

}
