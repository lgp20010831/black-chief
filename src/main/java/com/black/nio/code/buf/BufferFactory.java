package com.black.nio.code.buf;

import com.black.nio.code.AttysNioException;
import com.black.nio.code.Configuration;
import com.black.core.query.ClassWrapper;
import com.black.core.query.ConstructorWrapper;
import com.black.core.tools.BeanUtil;

public final class BufferFactory {

    public static NioByteBuffer createBuffer(Configuration configuration){
        Class<? extends NioByteBuffer> bufferType = configuration.getBufferType();
        if (!BeanUtil.isSolidClass(bufferType)) {
            throw new AttysNioException("buffer type is not solid");
        }
        ClassWrapper<? extends NioByteBuffer> wrapper = ClassWrapper.get(bufferType);
        ConstructorWrapper<?> constructor = wrapper.getConstructor(Configuration.class);
        if (constructor == null){
            throw new AttysNioException("buffer need has constructor with param is configuration");
        }
        return (NioByteBuffer) constructor.newInstance(configuration);
    }

}
