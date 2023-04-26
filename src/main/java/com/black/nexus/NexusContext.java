package com.black.nexus;

import com.black.core.builder.HttpBuilder;
import com.black.core.util.StringUtils;
import lombok.extern.log4j.Log4j2;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

@Log4j2
public class NexusContext {

    private final Configuration configuration;

    public NexusContext(Configuration configuration) {
        this.configuration = configuration;
    }

    public void start(){
        try {
            String htmlUrl = configuration.getHtmlUrl();
            String downUrl = configuration.getDownUrl();
            if (configuration.downloadAll()) {
                cloneNexusTree(htmlUrl, downUrl);
            }else {
                List<String> downloadRoot = configuration.getDownloadRoot();
                for (String root : downloadRoot) {
                    String htmlUrlTemp = htmlUrl + StringUtils.removeIfEndWith(root, "/") + "/";
                    String downUrlTemp = downUrl + StringUtils.removeIfEndWith(root, "/") + "/";
                    cloneNexusTree(htmlUrlTemp, downUrlTemp);
                }
            }
        }catch (Throwable e){
            throw new IllegalStateException(e);
        }

    }

    private void cloneNexusTree(String cureentNextNode, String downLoadPath) throws Throwable {
        boolean isHttps = StringUtils.startsWithIgnoreCase(cureentNextNode, "https");
        String html = HttpBuilder.get(cureentNextNode).useSSl(isHttps).executeAndGetBody();
        Document doc = Jsoup.parse(html);
        if (doc != null) {
            Elements elements = doc.select("table>tbody>tr>td>a");
            if (elements != null && elements.size() > 0) {
                for (Element element : elements) {
                    //判断是文件或文件夹
                    String nodeName = element.text();

                    String href = element.attr("href");
                    if (!nodeName.equalsIgnoreCase("Parent Directory")) {
                        if (href.endsWith("/")) {
                            // 非叶子节点.
                            String nextNodePath = cureentNextNode + href;
                            cloneNexusTree(nextNodePath, downLoadPath + href);
                        } else {
                            // 叶子节点,下载文件.
                            download(downLoadPath + nodeName, nodeName);
                        }
                    }
                }
            }
        }
    }

    public void download(String downloadUrl, String fileName) throws Throwable{
        log.info("获取文件流: {}", downloadUrl);
        URL url = new URL(downloadUrl);
        URLConnection connection = url.openConnection();
        connection.setConnectTimeout(5 * 1000);
        InputStream inputStream = connection.getInputStream();
        FileStreamReader streamReader = configuration.getFileStreamReader();
        if (streamReader != null){
            streamReader.handle(inputStream, fileName);
        }
        inputStream.close();
    }
}
