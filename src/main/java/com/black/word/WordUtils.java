package com.black.word;

import com.deepoove.poi.XWPFTemplate;
import com.deepoove.poi.data.MiniTableRenderData;
import com.deepoove.poi.data.PictureRenderData;
import com.deepoove.poi.data.RowRenderData;
import com.deepoove.poi.data.TextRenderData;
import com.deepoove.poi.data.style.Style;
import com.deepoove.poi.data.style.TableStyle;
import com.black.core.query.ClassWrapper;
import com.black.core.query.FieldWrapper;
import com.black.core.tools.DefaultValue;
import com.black.core.util.Assert;
import com.black.syntax.SyntaxMetadataListener;
import com.black.syntax.SyntaxResolverManager;
import com.black.utils.ServiceUtils;
import lombok.NonNull;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STJc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.function.Function;

import static com.black.utils.ServiceUtils.*;

@SuppressWarnings("all")
public class WordUtils {

    public static InputStream getResource(String path){
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
    }

    public static void complie(Object source, String path, OutputStream out) throws IOException{
        complie(source, Thread.currentThread().getContextClassLoader().getResourceAsStream(path), out);
    }

    public static void complie(Object source, @NonNull InputStream in, @NonNull OutputStream out) throws IOException {
        XWPFTemplate render = XWPFTemplate.compile(in).render(source);
        render.write(out);
        render.close();
    }

    public static TextRenderData createTextRenderData(String text, Style style){
        return new TextRenderData(text, style);
    }

    public static Map<String, Object> createTxtData(Object bean) throws Exception {
        Class<?> beanClass = bean.getClass();
        ClassWrapper<?> cw = ClassWrapper.get(beanClass);
        Map<String, Object> result = new HashMap<>();
        for (FieldWrapper fw : cw.getFields()) {
            Object value = fw.getValue(bean);
            DefaultValue annotation = fw.getAnnotation(DefaultValue.class);
            TextRenderData data = createData(value, fw.getAnnotation(WordStyle.class), annotation == null ? "" : annotation.value());
            result.put(fw.getName(), data);
        }
        return result;
    }

    public static Object createSimpleData(String txt, int fontSize){
        Style style = new Style();
        style.setFontSize(fontSize);
        return new TextRenderData(txt, style);
    }

    public static TextRenderData createData(Object value, WordStyle wordStyle){
        return createData(value, wordStyle, "");
    }

    public static TextRenderData createData(Object value, WordStyle wordStyle, String defaultValue){
        TextRenderData renderData = new TextRenderData();
        renderData.setText(value == null ? defaultValue : value.toString());
        if (wordStyle != null){
            Style style = new Style();
            style.setFontSize(wordStyle.fontSize());
            style.setBold(wordStyle.bold());
            style.setColor(wordStyle.color());
            style.setFontFamily(wordStyle.fontFamily());
            style.setUnderLine(wordStyle.underLine());
            style.setStrike(wordStyle.strike());
            style.setItalic(wordStyle.italic());
            renderData.setStyle(style);
        }
        return renderData;
    }

    public static String fillTemplate(String template, List<Map<String, Object>> paramMap, String nullValue){
        return fillTemplate(template, paramMap, new StringJoiner(""), nullValue);
    }

    public static String fillTemplate(String template, List<Map<String, Object>> paramMap, String delimiter, String nullValue){
        return fillTemplate(template, paramMap, new StringJoiner(delimiter), nullValue);
    }

    public static String fillTemplate(String template, List<Map<String, Object>> paramMap, StringJoiner joiner, String nullValue){
        for (Map<String, Object> map : paramMap) {
            String layer = ServiceUtils.parseTxt(template, "#{", "}", key -> {
                return ServiceUtils.getString(findValue(map, key), nullValue);
            });
            joiner.add(layer);
        }
        return joiner.toString();
    }

    public static class WordSyntaxBuilder{

        final String template;

        final List<Map<String, Object>> paramMaps;

        String nullValue = "";

        SyntaxMetadataListener syntaxMetadataListener;

        StringJoiner joiner;

        Function<Map<String, Object>, String> templateReaplacer;

        public WordSyntaxBuilder(String template, List<Map<String, Object>> paramMaps) {
            this.template = template;
            this.paramMaps = paramMaps;
        }

        public WordSyntaxBuilder nullValue(String nullValue) {
            this.nullValue = nullValue;
            return this;
        }

        public WordSyntaxBuilder syntaxMetadataListener(SyntaxMetadataListener syntaxMetadataListener) {
            this.syntaxMetadataListener = syntaxMetadataListener;
            return this;
        }

        public WordSyntaxBuilder joiner(StringJoiner joiner) {
            this.joiner = joiner;
            return this;
        }

        public WordSyntaxBuilder joiner(String delimiter){
            joiner = new StringJoiner(delimiter);
            return this;
        }

        public WordSyntaxBuilder templateReaplacer(Function<Map<String, Object>, String> templateReaplacer) {
            this.templateReaplacer = templateReaplacer;
            return this;
        }

        public String doSyntaxTemplate(){
            Assert.notNull(joiner, "joiner is null");
            for (Map<String, Object> paramMap : paramMaps) {
                String t = template;
                if (templateReaplacer != null){
                    t = templateReaplacer.apply(paramMap);
                }
                joiner.add(syntaxTemplate(t, paramMap, nullValue, syntaxMetadataListener));
            }
            return joiner.toString();
        }
    }

    public static String syntaxTemplate(String template, Map<String, Object> paramMap, String nullValue){
        return syntaxTemplate(template, paramMap, nullValue, null);
    }

    //template = #{bj}班学生共有#{num}人....
    public static String syntaxTemplate(String template, Map<String, Object> paramMap, String nullValue, SyntaxMetadataListener syntaxMetadataListener){
        String txt = parseTxt(template, "#{", "}", key -> {
            Object result = SyntaxResolverManager.resolverItem(key, paramMap, syntaxMetadataListener);
            return getString(result, nullValue);
        });

        //解析动态方法
        txt = parseTxt(txt, "${", "}", key -> {
            return parseMethodTxt(key, paramMap, nullValue);
        });
        return txt;
    }

    public static <T> MiniTableRenderData simpleCreateMiniTableRenderData(List<T> pojoList, Function<T, List<Object>> function, String... headers){
        return simpleCreateMiniTableRenderData(pojoList, "center", null, function, headers);
    }

    public static <T> MiniTableRenderData simpleCreateMiniTableRenderData(@NonNull List<T> pojoList, String align, String bgcolor, @NonNull Function<T, List<Object>> function, String... headers){

        RowRenderData header = RowRenderData.build(headers);
        List<RowRenderData> rows = new ArrayList<>();
        for (T t : pojoList) {
            List<Object> apply = function.apply(t);
            if (apply.size() != header.size()){
                throw new IllegalStateException("表格头尺寸应该与数据单元保持一致");
            }
            RowRenderData renderData = RowRenderData.build(apply.stream().map(val -> {
                return val == null ? "" : val.toString();
            }).toArray(String[]::new));
            TableStyle style = new TableStyle();
            style.setAlign(STJc.Enum.forString(bgcolor));
            style.setBackgroundColor(bgcolor);
            renderData.setRowStyle(style);
            rows.add(renderData);
        }
        return new MiniTableRenderData(header, rows);
    }

    public static PictureRenderData writeImage(int width, int height, String type, InputStream in){
        return writeImage(width, height, type, in, "图片加载失败");
    }

    public static PictureRenderData writeImage(int width, int height, String type, InputStream in, String altMeta){
        PictureRenderData renderData = new PictureRenderData(width, height, type, in);
        renderData.setAltMeta(altMeta == null ? "图片加载失败" : altMeta);
        return renderData;
    }


}
