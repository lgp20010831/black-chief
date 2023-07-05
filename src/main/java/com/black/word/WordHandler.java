package com.black.word;

import com.black.core.util.Assert;
import com.black.throwable.IOSException;
import com.black.utils.ServiceUtils;
import com.deepoove.poi.data.MiniTableRenderData;
import com.deepoove.poi.data.PictureRenderData;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

import static com.black.word.WordV2Utils.getTypeFromFileName;

@SuppressWarnings("all")
public class WordHandler {

    private final Map<String, Object> param;

    private final InputStream templateStream;

    private OutputStream out;

    public WordHandler(InputStream templateStream){
        this(new LinkedHashMap<>(), templateStream);
    }

    public WordHandler(Map<String, Object> param, InputStream templateStream) {
        this.param = param;
        this.templateStream = templateStream;
    }

    public WordHandler(String path){
        this(new LinkedHashMap<>(), path);
    }

    public WordHandler(Map<String, Object> param, String path){
        this.param = param;
        this.templateStream = ServiceUtils.getNonNullResource(path);
    }


    public WordHandler out(OutputStream out) {
        this.out = out;
        return this;
    }

    public Map<String, Object> getParam() {
        return param;
    }

    public WordHandler condition(String k, boolean expression, String k2, Object show){
        return condition(k, expression, k2, show, Type.AUTO);
    }

    public WordHandler condition(String k, boolean expression, String k2, Object show, Type type){
        return put(k, expression).put(k2, WordV2Utils.cast(show, type));
    }

    public WordHandler put(String k, Object v){
        param.put(k, v);
        return this;
    }

    public WordHandler putAll(Map<String, Object> m){
        if (m != null)
            param.putAll(m);
        return this;
    }

    public WordHandler list(String k, Object... params){
        return list(k, Arrays.asList(params));
    }

    public WordHandler list(String k, Object data){
        param.put(k, WordV2Utils.cast(data, Type.LIST));
        return this;
    }

    public WordHandler table(String k, Object data, String headerInfo){
        return table(k, data, headerInfo, null);
    }

    public WordHandler table(String k, Object data, String headerInfo, Consumer<Table.TableBuilder> consumer){
        Table.TableBuilder builder = Table.make(data, headerInfo);
        if (consumer != null){
            consumer.accept(builder);
        }
        MiniTableRenderData renderData = builder.create();
        put(k, renderData);
        return this;
    }

    public WordHandler reimg(String k, String path){
        return reimg(k, path, null);
    }

    public WordHandler reimg(String k, String path, String altMeta){
        try {
            BufferedImage bufferedImage = ImageIO.read(ServiceUtils.getNonNullResource(path));
            int w = bufferedImage.getWidth();
            int h = bufferedImage.getHeight();
            return img(k, w, h, getTypeFromFileName(path), ServiceUtils.getNonNullResource(path), altMeta);
        } catch (IOException e) {
            throw new IOSException(e);
        }

    }

    public WordHandler reimg(String k, int width, int height, String path){
        return reimg(k, width, height, path, null);
    }

    public WordHandler reimg(String k, int width, int height, String path, String altMeta){
        return img(k, width, height, getTypeFromFileName(path), ServiceUtils.getNonNullResource(path), altMeta);
    }

    public WordHandler img(String k, int width, int height, String type, InputStream in){
        return img(k, width, height, type, in, null);
    }

    public WordHandler img(String k, int width, int height, String type, InputStream in, String altMeta){
        PictureRenderData renderData = WordUtils.writeImage(width, height, type, in, altMeta);
        return put(k, renderData);
    }

    public void flush(){
        Assert.notNull(out, "null outputstream");
        try {
            WordUtils.complie(param, templateStream, out);
            out.flush();
        } catch (IOException e) {
            throw new IOSException(e);
        }
    }
}
