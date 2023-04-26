package com.black.core.aop.servlet.result;

import com.black.core.query.ClassWrapper;
import com.black.core.query.MethodWrapper;
import lombok.Getter;

import java.util.List;

@Getter
public class BeforeWriteSession {


    private final Object[] args;

    private final MethodWrapper mw;

    private final ClassWrapper<?> cw;

    private final List<ChiefBeforeWriteResolver> resolvers;

    public BeforeWriteSession(Object[] args, MethodWrapper mw, ClassWrapper<?> cw, List<ChiefBeforeWriteResolver> resolvers) {
        this.args = args;
        this.mw = mw;
        this.cw = cw;
        this.resolvers = resolvers;
    }
}
