package com.black.core.work.w2.connect.builder;

import com.black.core.json.ReflexUtils;
import com.black.core.work.w2.connect.config.WorkflowConfiguration;
import com.black.core.work.w2.connect.node.WorkflowNodeDefinitional;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;

public class NodeBuilderManager {

    private final Map<String, WorkflowNodeDefinitional> definitionalMap = new HashMap<>();

    private final Collection<Class<? extends NodeBuilder>> builderTypes = new HashSet<>();

    private final WorkflowConfiguration configuration;

    public NodeBuilderManager(WorkflowConfiguration configuration){
        this.configuration = configuration;
        Collection<Class<? extends NodeBuilder>> builderTypes = configuration.getBuilderTypes();
        if (builderTypes != null){
            this.builderTypes.addAll(builderTypes);
        }
        definitionalMap.putAll(configuration.getDispatcher().getNodeDefinitionals());
    }


    public NodeBuilderLeader getLeader(String alias, String status){
        return new NodeBuilderLeader(alias, status, definitionalMap, this, instanceBuilders());
    }

    protected Collection<NodeBuilder> instanceBuilders(){
        return builderTypes.stream().map(ReflexUtils::instance).collect(Collectors.toSet());
    }
}
