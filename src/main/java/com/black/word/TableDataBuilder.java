package com.black.word;

import com.deepoove.poi.data.MiniTableRenderData;
import com.deepoove.poi.data.RowRenderData;
import com.deepoove.poi.data.style.TableStyle;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STJc;

import java.util.List;
import java.util.function.Function;

public class TableDataBuilder {


    public static <T> BuilderImpl<T> builder(List<T> pojoList, Function<T, List<Object>> function){
        return new BuilderImpl<>(pojoList, function);
    }

    public static class BuilderImpl<T>{

        String commonBgColor;

        String commonAlign = "center";

        final List<T> pojoList;

        final Function<T, List<Object>> function;

        String[] headers;

        String noDatadesc;

        float width;

        String headerAlign = "center";

        String headerBgColor;

        public BuilderImpl(List<T> pojoList, Function<T, List<Object>> function) {
            this.pojoList = pojoList;
            this.function = function;
        }

        public BuilderImpl<T> headers(String... headers){
            this.headers = headers;
            return this;
        }


        public BuilderImpl<T> noDatadesc(String desc){
            noDatadesc = desc;
            return this;
        }

        public BuilderImpl<T> width(float width){
            this.width = width;
            return this;
        }

        public BuilderImpl<T> headerAlign(String headerAlign){
            this.headerAlign = headerAlign;
            return this;
        }

        public BuilderImpl<T> headerBgColor(String headerBgColor){
            this.headerBgColor = headerBgColor;
            return this;
        }

        public MiniTableRenderData create(){
            MiniTableRenderData renderData = WordUtils.simpleCreateMiniTableRenderData(pojoList, commonAlign, commonBgColor, function, headers);
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
