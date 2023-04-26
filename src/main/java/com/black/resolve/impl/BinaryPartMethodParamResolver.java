package com.black.resolve.impl;

import com.black.io.in.JHexByteArrayInputStream;
import com.black.io.out.BinaryPartElement;
import com.black.resolve.annotation.BinaryPart;
import com.black.resolve.annotation.ResolveSort;
import com.black.resolve.param.ParamUtils;
import com.black.core.aop.servlet.ParameterWrapper;
import com.black.core.chain.GroupUtils;
import com.black.core.query.MethodWrapper;
import com.black.core.util.StringUtils;
import com.black.utils.ServiceUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@ResolveSort(30)
@SuppressWarnings("all")
public class BinaryPartMethodParamResolver extends AbstractMethodParameterHandlerAdaptationResolver{

    @Override
    protected boolean concreteMethodSupport(MethodWrapper mw) {
        return mw.parameterHasAnnotation(BinaryPart.class);
    }

    @Override
    protected Object resolveMethod(MethodWrapper mw, JHexByteArrayInputStream inputStream) throws Throwable {
        List<ParameterWrapper> partParams = mw.getParameterByAnnotation(BinaryPart.class);
        List<BinaryPartElement> elements = readAllPartElements(inputStream);
        Map<String, BinaryPartElement> elementMap = GroupUtils.singleGroupArray(elements, BinaryPartElement::getName);
        Object[] args = new Object[mw.getParameterCount()];
        for (ParameterWrapper partParam : partParams) {
            Object arg = null;
            Class<?> type = partParam.getType();
            if (ParamUtils.isCollection(partParam)) {
                Class<?> generic = ParamUtils.getGenericByCollection(partParam);
                Collection<Object> collection = ServiceUtils.createCollection(type);
                for (BinaryPartElement element : elements) {
                    if (generic.isAssignableFrom(element.getClass())){
                        collection.add(element);
                    }
                }
                arg = collection;
            }else {
                BinaryPart annotation = partParam.getAnnotation(BinaryPart.class);
                String name = StringUtils.hasText(annotation.value()) ? annotation.value() : partParam.getName();
                arg = elementMap.get(name);
            }
            args[partParam.getIndex()] = arg;
        }
        return args;
    }

    private List<BinaryPartElement> readAllPartElements(JHexByteArrayInputStream inputStream){
        List<BinaryPartElement> elements = new ArrayList<>();
        for (;;){
            BinaryPartElement partElement;
            try {
                partElement = inputStream.readBinaryBytes();
            }catch (IOException e){
                break;
            }
            elements.add(partElement);
        }
        return elements;
    }
}
