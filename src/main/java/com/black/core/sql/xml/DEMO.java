package com.black.core.sql.xml;

import com.black.core.factory.beans.xml.ElementWrapper;
import com.black.core.factory.beans.xml.XmlBeanFactory;
import com.black.core.factory.beans.xml.XmlWrapper;

import java.io.IOException;

public class DEMO {


    public static void main(String[] args) throws IOException {
//        ResourcePatternResolver resourceResolver = new SpringBootVfsLoader().getResourceResolver();
//        System.out.println(resourceResolver.getResources("classpath*:iu/*.xml").length);
//        GlobalMappingComponent.loads();
//        AviatorEvaluator.addFunction(new AbstractFunction() {
//            @Override
//            public String getName() {
//                return "notNull";
//            }
//
//            @Override
//            public AviatorObject call(Map<String, Object> env, AviatorObject arg1) {
//                return arg1.getValue(env) != null ? AviatorBoolean.TRUE : AviatorBoolean.FALSE;
//            }
//        });
        XmlBeanFactory beanFactory = new XmlBeanFactory();
        XmlWrapper xmlWrapper = (XmlWrapper) beanFactory.get("iu/mapper.xml");
//        ElementWrapper rootElement = xmlWrapper.getRootElement();
//        XmlEngine.addHandler("query", new QueryXmlNodeHandler());
//        XmlEngine.addHandler("for", new ForXmlNodeHandler());
//        XmlEngine.addHandler("if", new IfXmlNodeHandler());
//        String sql = XmlEngine.processorSql(rootElement.getByName("query"), Vfu.js("map", Av0.js("name", "lgp", "list", Av0.as(1, 2, 3))));
        ElementWrapper rootElement = xmlWrapper.getRootElement();
        ElementWrapper wrapper = rootElement.getByName("query");
        System.out.println(wrapper.getStringValue());
        ElementWrapper anIf = wrapper.getByName("if");
        anIf.setText("");
        anIf.clearContent();
        System.out.println(wrapper.getStringValue());
    }

}
