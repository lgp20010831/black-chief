package com.black.nio.code.util;

import com.black.nio.code.*;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.SocketAddress;
import java.nio.channels.SelectableChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

@Log4j2
public class NioUtils {

    public static NioChannel createNioChannel(SelectableChannel channel, Configuration configuration) throws IOException {
        String clientName = NioUtils.getClientName(channel);
        AbstractNioChannel nioChannel;
        try {

                /*
                    Currently, different niochannel types are common according to this channel type
                 */
            if (channel instanceof SocketChannel){
                nioChannel = new NioSocketChannel((SocketChannel) channel, configuration, clientName);
            }else {
                nioChannel = new NioServerSocketChannel((ServerSocketChannel) channel, configuration, clientName);
            }
        }catch (Throwable e){
            log.error("The channel object cannot be created. " +
                    "The possible reasons are that the pipeline cannot " +
                    "be created, an instantiation exception occurs when " +
                    "allocating the responsibility chain, and an exception " +
                    "occurs when allocating the buffer");
            throw e;
        }
        return nioChannel;
    }

    public static String getClientName(SelectableChannel channel) throws IOException {
        try {
            if (channel instanceof SocketChannel){
                SocketAddress remoteAddress = ((SocketChannel) channel).getRemoteAddress();
                return remoteAddress == null ? "client" : remoteAddress.toString();
            }
            return "server";
        } catch (IOException e) {
            try {
                Field remoteAddress = channel.getClass().getDeclaredField("remoteAddress");
                remoteAddress.setAccessible(true);
                return remoteAddress.get(channel).toString();
            } catch (Exception ex) {
                throw e;
            }
        }
    }

    public static String letString(Object source){
        if (source == null) return "";
        if (source instanceof byte[])
            return getString((byte[]) source);
        if (source instanceof String){
            return (String) source;
        }
        return source.toString();
    }

    public static String getString(byte[] bytes){
        return getString(bytes, null);
    }

    public static String getString(byte[] bytes, Charset charset){
        if (bytes == null) return "";
        return charset == null ? new String(bytes) : new String(bytes, charset);
    }

    public static byte[] getBytes(String str){
        return getBytes(str, null);
    }

    public static byte[] getBytes(String str, Charset charset){
        String s = str == null ? "null" : str;
        return charset == null ? s.getBytes() : s.getBytes(charset);
    }

}
