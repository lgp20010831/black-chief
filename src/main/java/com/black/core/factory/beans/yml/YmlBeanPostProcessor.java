package com.black.core.factory.beans.yml;


import com.black.core.cache.TypeConvertCache;
import com.black.core.config.ApplicationConfigurationReaderHolder;
import com.black.core.convert.TypeHandler;
import com.black.core.factory.beans.BeanFactory;
import com.black.core.factory.beans.process.inter.BeanInitializationHandler;
import com.black.core.query.FieldWrapper;
import com.black.core.spring.annotation.WriedYmlConfigurationProperties;
import com.black.core.tools.BeanUtil;
import com.black.core.util.SetGetUtils;
import com.black.core.util.StringUtils;
import com.black.core.yml.YmlConfigurationProperties;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.annotation.AnnotationUtils;

@Log4j2
public class YmlBeanPostProcessor implements BeanInitializationHandler {
    @Override
    public boolean support(FieldWrapper fw, BeanFactory factory, Object bean) {
        Class<?> primordialClass = BeanUtil.getPrimordialClass(bean);
        Class<?> declaringClass = fw.getDeclaringClass().getPrimordialClass();
        WriedYmlConfigurationProperties properties = AnnotationUtils.getAnnotation(primordialClass, WriedYmlConfigurationProperties.class);
        WriedYmlConfigurationProperties declaringProperties = AnnotationUtils.getAnnotation(declaringClass, WriedYmlConfigurationProperties.class);
        if (properties != null || declaringProperties != null){
            if (fw.isNull(bean)) {
                return SetGetUtils.hasSetMethod(fw.getField());
            }
        }
        return false;
    }

    @Override
    public void doHandler(FieldWrapper fw, BeanFactory factory, Object bean) {

        String name = fw.getName();
        if (fw.hasAnnotation(YmlPropertiesValue.class)) {
            name = fw.getAnnotation(YmlPropertiesValue.class).value();
        }
        Class<?> primordialClass = fw.getDeclaringClass().getPrimordialClass();
        YmlConfigurationProperties properties = AnnotationUtils.getAnnotation(primordialClass, YmlConfigurationProperties.class);
        if (properties != null && StringUtils.hasText(properties.value())){
            name = StringUtils.linkStr(properties.value(), ".", name);
        }
        if (log.isDebugEnabled()) {
            log.debug("Object needs to configure the fill value of the property: [{}]", name);
        }
        String attribute = ApplicationConfigurationReaderHolder.getReader().selectAttribute(name);
        if (attribute != null){
            Class<?> fwType = fw.getType();
            TypeHandler handler = TypeConvertCache.initAndGet();
            if (handler == null){
                if (log.isWarnEnabled()) {
                    log.warn("An attempt was made to fill the attribute value into " +
                            "the object, but the field is not of string type, and the " +
                            "type converter does not exist. The type conversion cannot " +
                            "be realized, and the configuration attribute cannot be " +
                            "injected into the object, bean: [{}], attribute: [{}]", bean, name);
                }
                return;
            }
            Object value = handler.convert(fwType, attribute);
            SetGetUtils.invokeSetMethod(fw.getField(), value, bean);
        }
    }
}
