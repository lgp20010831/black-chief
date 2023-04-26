package com.black.utils;

import com.black.core.util.StringUtils;
import com.black.syntax.SyntaxResolverManager;
import com.black.throwable.IOSException;
import com.sun.mail.util.MailSSLSocketFactory;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.multipart.MultipartFile;

import javax.activation.DataHandler;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Consumer;

@Log4j2
public class EmailUtils {

    public static void main(String[] args) throws IOException {
        email("1824915361@qq.com", "1824915361@qq.com")
                .pwd("nsxxycyiaixdedbh")
                .subject("测试html")
                .setText(importHtml("autoBuild/api/md.html"))
                //.addPart(EmailPart.file(new File("E:\\ideaSets\\SpringAutoThymeleaf\\src\\main\\resources\\test.jpg")))
                .send();
        //System.out.println(importHtml("autoBuild/api/md.html"));
    }

    public static EmailBuilder email(String from, String to){
        return new EmailBuilder(from, to);
    }

    public static String importTemplatePath(String path, Map<String, Object> env) throws IOException {
        return importTemplate(Thread.currentThread().getContextClassLoader().getResourceAsStream(path), env);
    }

    public static String importTemplate(InputStream in, Map<String, Object> env) throws IOException {
        byte[] readBytes = IoUtils.readBytes(in);
        return importTemplate(new String(readBytes), env);
    }

    public static String importTemplate(String template, Map<String, Object> env){
        return ServiceUtils.parseTxt(template, "#{", "}", item -> {
            Object value = SyntaxResolverManager.resolverItem(item, env, null);
            return value == null ? "" : value.toString();
        });
    }

    public static String importHtml(String rpath) throws IOException {
        return importHtml(Thread.currentThread().getContextClassLoader().getResourceAsStream(rpath));
    }

    public static String importHtml(@NonNull InputStream read) throws IOException {
        byte[] readBytes = IoUtils.readBytes(read);
        return new String(readBytes);
    }

    public static class EmailBuilder{

        private SMTPServer smtpServer = SMTPServer.QQ;

        private final String from;

        private final String to;

        private String password;

        private String subject;

        private String text;

        private Consumer<Multipart> multipartCallback;

        private Consumer<Message> messageCallback;

        private Consumer<Properties> propertiesCallback;

        private List<EmailPart> parts = new ArrayList<>();

        private String host;

        public EmailBuilder(String from, String to) {
            this.from = from;
            this.to = to;
        }

        public EmailBuilder pwd(String pwd){
            password = pwd;
            return this;
        }

        public EmailBuilder subject(String sub){
            subject = sub;
            return this;
        }

        public EmailBuilder addParts(EmailPart... parts){
            this.parts.addAll(Arrays.asList(parts));
            return this;
        }

        public EmailBuilder multiparts(List<MultipartFile> files){
            if (files != null){
                List<EmailPart> parts = new ArrayList<>();
                for (MultipartFile file : files) {
                    try {
                        parts.add(EmailPart.multipart(file));
                    } catch (IOException e) {
                        throw new IllegalStateException(e);
                    }
                }
                addParts(parts);
            }
            return this;
        }

        public EmailBuilder addParts(List<EmailPart> parts){
            if (parts != null){
                addParts(parts.toArray(new EmailPart[0]));
            }
            return this;
        }

        public EmailBuilder setMessageCallback(Consumer<Message> messageCallback) {
            this.messageCallback = messageCallback;
            return this;
        }

        public EmailBuilder setMultipartCallback(Consumer<Multipart> multipartCallback) {
            this.multipartCallback = multipartCallback;
            return this;
        }

        public EmailBuilder setPropertiesCallback(Consumer<Properties> propertiesCallback) {
            this.propertiesCallback = propertiesCallback;
            return this;
        }

        public EmailBuilder setText(String text) {
            this.text = text;
            return this;
        }

        public EmailBuilder setSmtpServer(SMTPServer smtpServer) {
            this.smtpServer = smtpServer;
            return this;
        }

        public void send(){
            if (!StringUtils.hasText(host)){
                host = smtpServer.getHost();
            }
            EmailUtils.sendEmail(from, to, host, password, subject, text, multipartCallback,
                    messageCallback, propertiesCallback, parts.toArray(new EmailPart[0]));
        }
    }

    /***
     * 发送邮件
     * @param from 设置发件人, 例如: xxx@qq.com
     * @param to 设置收件人, 例如: xxx@qq.com
     * @param host 邮件服务器, 例如: smtp.qq.com
     * @param password 发送方第三方登录授权码
     * @param subject 主题
     * @param text 内容
     * @param multipartCallback 消息块回调函数(可以为空)
     * @param messageCallback 消息整体回调函数(可以为空)
     * @param propertiesCallback 配置属性回调函数(可以为空)
     * @param emailParts 文件块数组(一个代表一个附件, 可以为空)
     */
    public static void sendEmail(String from, String to, String host,
                                 String password,
                                 String subject,
                                 String text,
                                 Consumer<Multipart> multipartCallback,
                                 Consumer<Message> messageCallback,
                                 Consumer<Properties> propertiesCallback,
                                 EmailPart... emailParts){
        try {

            //获取系统属性
            Properties properties = System.getProperties();

            //SSL加密
            MailSSLSocketFactory sf = new MailSSLSocketFactory();
            sf.setTrustAllHosts(true);
            properties.put("mail.smtp.ssl.enable", "true");
            properties.put("mail.smtp.ssl.socketFactory", sf);

            //设置系统属性
            properties.setProperty("mail.smtp.host", host);
            properties.put("mail.smtp.auth", "true");

            if (propertiesCallback != null){
                propertiesCallback.accept(properties);
            }
            //获取发送邮件会话、获取第三方登录授权码
            Session session = Session.getDefaultInstance(properties, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(from, password);
                }
            });

            Message message = new MimeMessage(session);

            //防止邮件被当然垃圾邮件处理，披上Outlook的马甲
            message.addHeader("X-Mailer","Microsoft Outlook Express 6.00.2900.2869");
            message.setFrom(new InternetAddress(from));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            //邮件标题
            message.setSubject(subject);
            BodyPart bodyPart = new MimeBodyPart();
            bodyPart.setText(text);
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(bodyPart);
            //附件
            if (emailParts != null){
                for (EmailPart emailPart : emailParts) {
                    MimeBodyPart mimeBodyPart = new MimeBodyPart();
                    mimeBodyPart.setDataHandler(new DataHandler(emailPart.getDataSource()));
                    mimeBodyPart.setFileName(emailPart.getAnnexName());
                    multipart.addBodyPart(mimeBodyPart);
                }
            }
            if (multipartCallback != null){
                multipartCallback.accept(multipart);
            }

            message.setContent(multipart);
            if(messageCallback != null){
                messageCallback.accept(message);
            }
            Transport.send(message);
            log.info("send email to {} successful", to);
        } catch (Throwable e) {
            throw new IOSException(e);
        }
    }


}
