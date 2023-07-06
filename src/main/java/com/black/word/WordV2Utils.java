package com.black.word;

import com.black.core.sql.code.util.SQLUtils;
import com.black.throwable.IOSException;
import com.black.utils.ServiceUtils;
import com.deepoove.poi.data.*;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

@SuppressWarnings("all")
public class WordV2Utils {

    public static RenderData cast(Object param, Type type){
        if (param == null){
            return new TextRenderData();
        }
        if (param instanceof RenderData){
            return (RenderData) param;
        }
        switch (type){
            case AUTO:
                if (param instanceof String){
                    String str = param.toString();
                    if (isURL(str)){
                        return new HyperLinkTextRenderData(str, str);
                    }
                    URL url = Thread.currentThread().getContextClassLoader().getResource(str);
                    if (url != null){
                        String file = url.getFile();
                        if (file != null && isImage(new File(file))){
                            return getImg(str);
                        }
                    }
                    return new TextRenderData(str);
                }
                Class<?> paramClass = param.getClass();
                if (param instanceof Collection || paramClass.isArray()){
                    List<Object> list = SQLUtils.wrapList(param);
                    List<RenderData> renderDataList = new ArrayList<>();
                    for (Object o : list) {
                        RenderData data = cast(o, Type.AUTO);
                        renderDataList.add(data);
                    }
                    return NumbericRenderData.build(renderDataList.toArray(new RenderData[0]));
                }

                if (param instanceof File){
                    if (isImage((File) param)) {
                        return getImg((File) param);
                    }
                }
                return new TextRenderData();
            case TEXT:
                String pstr = param == null ? "" : param.toString();
                return new TextRenderData(pstr);
            case LINK:
                String pstr2 = param == null ? "" : param.toString();
                return new HyperLinkTextRenderData(pstr2, pstr2);
            case LIST:
                List<Object> list = SQLUtils.wrapList(param);
                List<RenderData> renderDataList = new ArrayList<>();
                for (Object o : list) {
                    RenderData data = cast(o, Type.AUTO);
                    renderDataList.add(data);
                }
                return NumbericRenderData.build(renderDataList.toArray(new RenderData[0]));
            case TABLE:
                throw new UnsupportedOperationException("缺少标题信息无法转换成列表");
            case IMG:
                if (param instanceof String){
                    String string = param.toString();
                    URL url = Thread.currentThread().getContextClassLoader().getResource(string);
                    if (url != null){
                        String file = url.getFile();
                        if (file != null && isImage(new File(file))){
                            return getImg(string);
                        }
                    }
                }
                if (param instanceof File){
                    if (isImage((File) param)) {
                        return getImg((File) param);
                    }
                }
                throw new IllegalStateException("无法转换: " + param + " --> IMG");
            default:
                throw new IllegalStateException("ill type: " + type);
        }
    }


    public static PictureRenderData getImg(File file){
        return getImg(file, null);
    }

    public static PictureRenderData getImg(File file, String altMeta){
        try {
            String path = file.getName();
            FileInputStream inputStream = new FileInputStream(file);
            BufferedImage bufferedImage = ImageIO.read(inputStream);
            int w = bufferedImage.getWidth();
            int h = bufferedImage.getHeight();
            return WordUtils.writeImage(w, h, getTypeFromFileName(path), new FileInputStream(file), altMeta);
        } catch (IOException e) {
            throw new IOSException(e);
        }
    }

    public static PictureRenderData getImg(String path){
        return getImg(path, null);
    }

    public static PictureRenderData getImg(String path, String altMeta){
        try {
            BufferedImage bufferedImage = ImageIO.read(ServiceUtils.getNonNullResource(path));
            int w = bufferedImage.getWidth();
            int h = bufferedImage.getHeight();
            return WordUtils.writeImage(w, h, getTypeFromFileName(path), ServiceUtils.getNonNullResource(path), altMeta);
        } catch (IOException e) {
            throw new IOSException(e);
        }

    }


    public static String getTypeFromFileName(String name){
        int index = name.lastIndexOf(".");
        return name.substring(index);
    }


    public static boolean isURL(String str) {
        //转换为小写
        str = str.toLowerCase();
        String regex = "^((https|http|ftp|rtsp|mms)?://)"  //https、http、ftp、rtsp、mms
                + "?(([0-9a-z_!~*'().&=+$%-]+: )?[0-9a-z_!~*'().&=+$%-]+@)?" //ftp的user@
                + "(([0-9]{1,3}\\.){3}[0-9]{1,3}" // IP形式的URL- 例如：199.194.52.184
                + "|" // 允许IP和DOMAIN（域名）
                + "([0-9a-z_!~*'()-]+\\.)*" // 域名- www.
                + "([0-9a-z][0-9a-z-]{0,61})?[0-9a-z]\\." // 二级域名
                + "[a-z]{2,6})" // first level domain- .com or .museum
                + "(:[0-9]{1,5})?" // 端口号最大为65535,5位数
                + "((/?)|" // a slash isn't required if there is no file name
                + "(/[0-9a-z_!~*'().;?:@&=+$,%#-]+)+/?)$";
        return str.matches(regex);
    }

    /**
     * @param file 文件路径
     * @return 是否是图片
     */
    public static boolean isImage(File file) {
        if (file!=null && file.exists() && file.isFile()) {
            ImageInputStream iis = null;
            try {
                iis = ImageIO.createImageInputStream(file);
            } catch (IOException e) {
                return false;
            }
            Iterator iter = ImageIO.getImageReaders(iis);
            if (iter.hasNext()) {
                return true;
            }
        }
        return false;
    }
}
