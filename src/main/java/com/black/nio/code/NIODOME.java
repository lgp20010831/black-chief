package com.black.nio.code;


import com.black.io.out.DataByteBufferArrayOutputStream;
import com.black.pattern.Counter;
import com.black.core.spring.util.ApplicationUtil;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.charset.StandardCharsets;


public class NIODOME {


    public static void main(String[] args) throws IOException {
//        client();

        ApplicationUtil.programRunMills(() ->{
            try {
                server();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    static void client() throws IOException {
        Configuration configuration = new Configuration(7474);
        configuration.setChannelInitialization(pp -> {
            pp.addLast(new ChannelHandler() {
                @Override
                public void read(ChannelHandlerContext chc, Object source) {
                    System.out.println(new String((byte[]) source));
                    chc.channel().writeAndFlush("cnm".getBytes());
                }

                @Override
                public void connectComplete(ChannelHandlerContext chc) {
                    chc.channel().writeAndFlush("hello".getBytes());
                    ChannelHandler.super.connectComplete(chc);
                }

                @Override
                public void error(ChannelHandlerContext chc, Throwable e) throws IOException {
                    System.out.println("发生异常" + e.getMessage());
                    ChannelHandler.super.error(chc, e);
                    try {
                        Thread.sleep(2000);
                        if (Counter.increasingAndCreate(chc.channel().nameAddress()) <= 5) {
                            ((NioSocketChannel)chc.channel()).reconnect();
                        }
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            });
        });
        NioClientContext context = new NioClientContext(configuration);
        context.start();
    }

    static void server() throws IOException {

        Configuration configuration = new Configuration(5556);
        configuration.setAcceptBufferSize(16);
        configuration.setOpenWorkPool(true);
        configuration.setWriteInCurrentLoop(false);
        configuration.setChannelInitialization(pp -> {
            pp.addLast((chc, source) -> {
                String sql = new String((byte[]) source, StandardCharsets.ISO_8859_1);
                System.out.println("信息: " + sql);
                SelectableChannel channel = chc.channel().channel();
                DataByteBufferArrayOutputStream out = chc.getOutputStream();
                out.writeUTF("草");
                if (sql.equals("wwww"))
                    out.flush();
            });
        });

        NioContext context = new NioServerContext(configuration);
        context.start();
    }

    static String getHttpRes(String b){
        String r = "HTTP/1.1 200 OK\n" +
                "Bdpagetype: 1\n" +
                "Bdqid: 0x923da43a0004ee35\n" +
                "Cache-Control: private\n" +
                "Connection: keep-alive\n" +
                "Content-Encoding: gzip\n" +
                "Content-Type: text/html;charset=utf-8\n" +
                "Date: Sat, 23 Jul 2022 07:12:12 GMT\n" +
                "Expires: Sat, 23 Jul 2022 07:11:43 GMT\n" +
                "Server: BWS/1.1\n" +
                "Set-Cookie: BDSVRTM=0; path=/\n" +
                "Set-Cookie: BD_HOME=1; path=/\n" +
                "Set-Cookie: H_PS_PSSID=36838_36557_36624_36726_36454_36414_36852_36167_36816_36570_36779_1993_36746_26350_36864_36649; path=/; domain=.baidu.com\n" +
                "Strict-Transport-Security: max-age=172800\n" +
                "Traceid: 1658560332024386996210537759272132210229\n" +
                "X-Frame-Options: sameorigin\n" +
                "X-Ua-Compatible: IE=Edge,chrome=1\n" +
                "Transfer-Encoding: chunked\n\n";
        return r + b;
    }
}
