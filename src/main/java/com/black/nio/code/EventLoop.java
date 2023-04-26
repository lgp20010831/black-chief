package com.black.nio.code;



import com.black.nio.code.run.Future;

import java.nio.channels.Selector;
import java.util.Map;
import java.util.concurrent.Callable;

/***
 * 一个 channel 绑定到一个 event loop 中
 * 所以一个 event loop 能够保存其关联的 channel 的引用
 * channel 也含有唯一 event loop 的引用
 * 关于 buffer:
 * 一个 event loop 存有一个 rece buffer, 用来缓存所有 channel read 的数据
 * 每一轮 read 处理结束以后, 清空缓存
 *
 * 每一个 channel 维护一个 write buffer
 */
public interface EventLoop {

    Map<String, NioChannel> getChannelQuotes();

    Future<?> removeAndCloseChannel(NioChannel channel);

    Thread getRunnableThread();

    Selector getSelector();

    EventLoopGroup getGroup();

    Configuration getConfiguration();

    Future<?> close();

    Future<?> addTask(Runnable task);

    <V> Future<V> addTask(Callable<V> callable);

    Future<NioChannel> registerChannel(NioChannel channel, int keyOps);
}
