package com.black.core.io.bio;

public class BioSessionImpl implements BioSession{

    //维护的连接实例
    private Connection connection;

    @Override
    public void write(String message) {
        connection.write(message);
    }

    @Override
    public void writeAndFlush(String message) {
        connection.writeAndFlush(message);
    }

    @Override
    public void close() {
        connection.close();
    }

    @Override
    public Connection getConnection() {
        return connection;
    }

    @Override
    public void restart() {

    }

    @Override
    public boolean isVaild() {
        return connection.isVaild();
    }
}
