//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package springfox.documentation.spring.web.scanners;

import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.TypeResolver;
import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import springfox.documentation.schema.Model;
import springfox.documentation.schema.ModelProperty;
import springfox.documentation.schema.ModelProvider;
import springfox.documentation.spi.schema.contexts.ModelContext;
import springfox.documentation.spi.service.contexts.RequestMappingContext;
import springfox.documentation.spring.web.plugins.DocumentationPluginsManager;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


@Component
public class ApiModelReader {
    private static final Logger LOG = LoggerFactory.getLogger(ApiModelReader.class);
    private final ModelProvider modelProvider;
    private final TypeResolver typeResolver;
    private final DocumentationPluginsManager pluginsManager;

    @Autowired
    public ApiModelReader(@Qualifier("cachedModels") ModelProvider modelProvider, TypeResolver typeResolver, DocumentationPluginsManager pluginsManager) {
        this.modelProvider = modelProvider;
        this.typeResolver = typeResolver;
        this.pluginsManager = pluginsManager;
    }

    public Map<String, Model> read(RequestMappingContext context) {
        Set<Class> ignorableTypes = Sets.newHashSet(context.getIgnorableParameterTypes());
        Set<ModelContext> modelContexts = this.pluginsManager.modelContexts(context);
        Map<String, Model> modelMap = Maps.newHashMap(context.getModelMap());

        ModelContext each;
        for(Iterator var5 = modelContexts.iterator(); var5.hasNext(); this.populateDependencies(each, modelMap)) {
            each = (ModelContext)var5.next();
            this.markIgnorablesAsHasSeen(this.typeResolver, ignorableTypes, each);
            Optional<Model> pModel = this.modelProvider.modelFor(each);
            if (pModel.isPresent()) {
                LOG.debug("Generated parameter model id: {}, name: {}, schema: {} models", ((Model)pModel.get()).getId(), ((Model)pModel.get()).getName());
                this.mergeModelMap(modelMap, (Model)pModel.get());
            } else {
                LOG.debug("Did not find any parameter models for {}", each.getType());
            }
        }

        return modelMap;
    }



    private void mergeModelMap(Map<String, Model> target, Model source) {
        String sourceModelKey = source.getId();
        if (!target.containsKey(sourceModelKey)) {
            LOG.debug("Adding a new model with key {}", sourceModelKey);
            target.put(sourceModelKey, source);
        } else {
            Model targetModelValue = (Model)target.get(sourceModelKey);
            Model model = doMergeModel(targetModelValue, source);
            target.put(sourceModelKey, model);
//            Map<String, ModelProperty> targetProperties = targetModelValue.getProperties();
//            Map<String, ModelProperty> sourceProperties = source.getProperties();
//            //Set<String> newSourcePropKeys = Sets.newHashSet(sourceProperties.keySet());
//            Set<String> newSourcePropKeys = Sets.newHashSet(targetProperties.keySet());
//            newSourcePropKeys.removeAll(targetProperties.keySet());
//            //Map<String, ModelProperty> mergedTargetProperties = Maps.newHashMap(targetProperties);
//            Map<String, ModelProperty> mergedTargetProperties = Maps.newHashMap(sourceProperties);
//            Iterator var9 = newSourcePropKeys.iterator();
//
//            while(var9.hasNext()) {
//                String newProperty = (String)var9.next();
//                LOG.debug("Adding a missing property {} to model {}", newProperty, sourceModelKey);
//                mergedTargetProperties.put(newProperty, sourceProperties.get(newProperty));
//            }
//
//            Model mergedModel = (new ModelBuilder()).id(targetModelValue.getId()).name(targetModelValue.getName()).type(targetModelValue.getType()).qualifiedType(targetModelValue.getQualifiedType()).properties(mergedTargetProperties).description(targetModelValue.getDescription()).baseModel(targetModelValue.getBaseModel()).discriminator(targetModelValue.getDiscriminator()).subTypes(targetModelValue.getSubTypes()).example(targetModelValue.getExample()).build();
//            target.put(sourceModelKey, mergedModel);
        }

    }

    private Model doMergeModel(Model target, Model source){
        Map<String, ModelProperty> targetProperties = target.getProperties();
        Map<String, ModelProperty> sourceProperties = source.getProperties();
        for (String name : new HashSet<>(targetProperties.keySet())) {
            ModelProperty targetPro = targetProperties.get(name);
            ModelProperty sourcePro = sourceProperties.get(name);
            if (sourcePro == null){
                continue;
            }

            if (isGeneric(targetPro)){
                continue;
            }

            if (isGeneric(sourcePro)){
                targetProperties.put(name, sourcePro);
            }

        }
        return target;
    }

    public boolean isGeneric(ModelProperty modelProperty){
        String qualifiedType = modelProperty.getQualifiedType();
        ResolvedType type = modelProperty.getType();
        return !qualifiedType.equals(type.toString());
    }

    private void markIgnorablesAsHasSeen(TypeResolver typeResolver, Set<Class> ignorableParameterTypes, ModelContext modelContext) {
        Iterator var4 = ignorableParameterTypes.iterator();

        while(var4.hasNext()) {
            Class ignorableParameterType = (Class)var4.next();
            modelContext.seen(typeResolver.resolve(ignorableParameterType, new Type[0]));
        }

    }

    private void populateDependencies(ModelContext modelContext, Map<String, Model> modelMap) {
        Map<String, Model> dependencies = this.modelProvider.dependencies(modelContext);
        Iterator var4 = dependencies.values().iterator();

        while(var4.hasNext()) {
            Model each = (Model)var4.next();
            this.mergeModelMap(modelMap, each);
        }

    }
}
