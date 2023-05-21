package com.black.core.factory.beans.vsf;

import com.black.core.factory.beans.BeanFactorysException;
import com.black.core.factory.beans.xml.XmlBeanFactory;
import com.black.core.tools.BeanUtil;
import com.black.core.util.StreamUtils;
import com.black.scan.ChiefScanner;
import com.black.scan.ScannerManager;
import com.black.vfs.VfsLoadException;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.annotation.AnnotationUtils;

import java.util.Set;

@Log4j2
public class VfsBeanFactory extends XmlBeanFactory {

    private final ChiefScanner loader;

    private VfsBeanFactory(){
        this(null);
    }

    public VfsBeanFactory(DefaultListableBeanFactory springFactory){
        super(springFactory);
        loader = ScannerManager.getScanner();
    }

    public ChiefScanner getLoader() {
        return loader;
    }

    public void load(String path){
        Set<Class<?>> classes = scannerClasses(path);
        if (log.isDebugEnabled()) {
            log.debug("exist in scan range - {}, has {} class", path, classes.size());
        }
        Set<Class<?>> eligiables = filterEligiables(classes);
        if (log.isDebugEnabled()) {
            log.debug("eligible objects in scan range:{}, num:{}", path, eligiables.size());
        }
        for (Class<?> eligiable : eligiables) {
            try {

                getBean(eligiable);
            }catch (BeanFactorysException e){
                throw new BeanFactorysException("An exception occurred while loading " +
                        "the object while loading the project bean: " + eligiable.getSimpleName(), e);
            }
        }
    }

    protected Set<Class<?>> scannerClasses(String path){
        try {

            return loader.load(path);
        }catch (VfsLoadException ve){
            throw new BeanFactorysException(ve);
        }
    }

    protected Set<Class<?>> filterEligiables(@NonNull Set<Class<?>> classes){
        return StreamUtils.filterSet(classes, bc ->{
            AsBean asBean = AnnotationUtils.getAnnotation(bc, AsBean.class);
            if (asBean != null){
                if (!BeanUtil.isSolidClass(bc)) {
                    if (log.isDebugEnabled()) {
                        log.debug("After the annotation as the injection object " +
                                "is marked, the object should be able to become an " +
                                "instantiatable object, Ineligible object:{}", bc.getSimpleName());
                    }
                    return false;
                }
                return true;
            }
            return false;
        });
    }
}
