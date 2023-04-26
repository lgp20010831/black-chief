package com.black.core.factory.beans.xml;

import com.black.core.query.Wrapper;
import lombok.NonNull;
import org.dom4j.Attribute;
import org.dom4j.Element;

import java.util.*;

public class ElementWrapper implements Wrapper<Element> {

    private final String name;
    private final Element element;
    private final Map<String, List<ElementWrapper>> elements = new LinkedHashMap<>();
    private final Map<String, Attribute> attrs = new LinkedHashMap<>();

    public ElementWrapper(@NonNull Element element) {
        this.element = element;
        for (Object ele : element.elements()) {
            Element elem = (Element) ele;
            List<ElementWrapper> wrappers = elements.computeIfAbsent(elem.getName(), n -> new ArrayList<>());
            wrappers.add(new ElementWrapper(elem));
        }
        for (Object attribute : element.attributes()) {
            Attribute attr = (Attribute) attribute;
            attrs.put(attr.getName(), attr);
        }
        name = element.getName();
    }


    @Override
    public String toString() {
        return "[" +
                "name=" + name +
                ", attrs=" + toAttrsString() +
                ']';
    }

    public String toAttrsString(){
        Map<String, String> kv = new HashMap<>();
        attrs.forEach((k, v) ->{
            kv.put(k, v.getValue());
        });
        return kv.toString();
    }

    public String getTextTrim(){
        return element.getTextTrim();
    }

    public String getStringValue(){
        return element.getStringValue();
    }

    public String getName(){
        return name;
    }

    public ElementWrapper selectSingleNode(String path){
        List<ElementWrapper> elements = selectNodes(path);
        return elements == null || elements.isEmpty() ? null : elements.get(0);
    }

    public boolean hasText(){
        String text = getText();
        return text == null || "".equals(text);
    }

    public String getText(){
        return element.getText();
    }

    public ElementWrapper clearContent(){
        element.clearContent();
        return this;
    }

    public ElementWrapper createCopy(){
        return new ElementWrapper(element.createCopy());
    }

    public ElementWrapper setText(String text){
        element.setText(text);
        return this;
    }

    public Collection<Attribute> attrList(){
        return attrs.values();
    }

    public ElementWrapper removeAttr(String name){
        Attribute attribute = attrs.get(name);
        if (attribute != null){
            if (element.remove(attribute)) {
                attrs.remove(name);
            }
        }
        return this;
    }

    public ElementWrapper addAttr(String name, String val){
        element.addAttribute(name, val);
        Attribute attribute = element.attribute(name);
        attrs.put(name, attribute);
        return this;
    }

    public ElementWrapper renameAttr(String oldName, String newName){
        Attribute attribute = attrs.get(oldName);
        if (attribute != null){
            attribute.setName(newName);
        }
        return this;
    }

    public ElementWrapper setAttrVal(String name, String value){
        Attribute attribute = attrs.get(name);
        if (attribute != null){
            attribute.setValue(value);
        }
        return this;
    }

    public String getAttrVal(String name){
        Attribute attribute = attrs.get(name);
        return attribute == null ? null : attribute.getValue();
    }

    public ElementWrapper find(String name){
        List<ElementWrapper> finds = finds(name);
        return finds.isEmpty() ? null : finds.get(0);
    }

    public List<ElementWrapper> finds(String name){
        List<ElementWrapper> list = getsByName(name);
        List<ElementWrapper> wrappers = new ArrayList<>();
        if (list != null){
            wrappers.addAll(list);
        }
        Map<String, List<ElementWrapper>> elements = getElements();
        for (List<ElementWrapper> sons : elements.values()) {
            for (ElementWrapper son : sons) {
                List<ElementWrapper> sonResult = son.finds(name);
                wrappers.addAll(sonResult);
            }
        }
        return wrappers;
    }

    public ElementWrapper getByName(String name){
        List<ElementWrapper> wrappers = getsByName(name);
        return wrappers == null || wrappers.isEmpty() ? null : wrappers.get(0);
    }

    public List<ElementWrapper> getsByName(String name){
        return elements.get(name);
    }

    public List<ElementWrapper> selectNodes(String path){
        List<ElementWrapper> wrappers = new ArrayList<>();
        for (Object node : element.selectNodes(path)) {
            Element elem = (Element) node;
            wrappers.add(new ElementWrapper(elem));
        }
        return wrappers;
    }

    public ElementWrapper remove(){
        Element parent = element.getParent();
        if (parent != null){
            parent.remove(element);
        }
        return this;
    }

    public Element getElement() {
        return element;
    }

    public Map<String, Attribute> getAttrs() {
        return attrs;
    }

    public Map<String, List<ElementWrapper>> getElements() {
        return elements;
    }

    public Collection<ElementWrapper> list(){
        Collection<ElementWrapper> elementWrappers = new ArrayList<>();
        for (List<ElementWrapper> wrappers : elements.values()) {
            elementWrappers.addAll(wrappers);
        }
        return elementWrappers;
    }

    @Override
    public Element get() {
        return element;
    }
}
