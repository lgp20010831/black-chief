package com.black.core.aop.servlet.item;

import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ItemResolver {

    private final Collection<LAOperatorProcessor> operatorProcessors = new HashSet<>();

    private final Map<String, List<StageHybridProcessorAndOperator>> cache = new ConcurrentHashMap<>();

    public void addAll(Collection<LAOperatorProcessor> operatorProcessors){
        if(operatorProcessors != null){
            this.operatorProcessors.addAll(operatorProcessors);
        }
    }

    public Object resolver(Map<String, Object> globalVariable, String item){
        List<StageHybridProcessorAndOperator> processorAndOperators = cache.computeIfAbsent(item, this::parseProcessorAndOperator);
        Object previousValue = null;
        for (StageHybridProcessorAndOperator processorAndOperator : processorAndOperators) {
            LAOperatorProcessor processor = processorAndOperator.getProcessor();
            try {
                previousValue = processor.parse(globalVariable, previousValue, processorAndOperator.getParagraphOperator());
            } catch (Throwable e) {
                throw new ParsesException(e);
            }
        }
        return previousValue;
    }

    protected Collection<LAOperatorProcessor> obtainProcessors(){
        return operatorProcessors;
    }

    protected List<StageHybridProcessorAndOperator> parseProcessorAndOperator(String item){
        item = item.trim();
        if (!StringUtils.hasText(item)){
            return null;
        }

        List<StageHybridProcessorAndOperator> processorAndOperators = new ArrayList<>();
        char[] chars = item.toCharArray();
        //代表当前处理器
        LAOperatorProcessor currentProcessor = null;
        //因为前一个和后一个关系紧密
        //所以构造两个封装变量对象
        EachParagraphOperator operator = new EachParagraphOperator();
        EachParagraphOperator nextOperator = new EachParagraphOperator();

        //当一个处理器结束运算符是空,
        //那么下一个运算符才能算是当前处理器的结束符
        //所以开启并行寻找, 保持当前处理器依旧生效的情况下寻找下一个处理器的开始符
        //如果能够成功找到, 则代表当前处理器结束, 然后切换下一个处理器为当前处理器
        boolean parallelFindEnd = false;
        loopChars: for (char c : chars) {
            //当当前处理器查找结束为空时, 或者当前处理器没有结束符的时候
            if (currentProcessor == null || parallelFindEnd){
                for (LAOperatorProcessor processor : obtainProcessors()) {
                    if (c == processor.getStartChar()) {

                        if (parallelFindEnd){
                            //如果当前处理器存在, 则构造最终结果
                            //然后切换处理器
                            operator.setMediumQuantity(nextOperator.getFrontQuantity());
                            processorAndOperators.add(new StageHybridProcessorAndOperator(operator, currentProcessor));
                            operator = nextOperator;
                            nextOperator = new EachParagraphOperator();
                            parallelFindEnd = false;
                        }
                        currentProcessor = processor;
                        operator.setStartChar(c);
                        continue loopChars;
                    }
                }
                if (parallelFindEnd){
                    nextOperator.addFrontQuantity(c);
                }else {
                    operator.addFrontQuantity(c);
                }
            }

            if (currentProcessor != null && !parallelFindEnd){
                char endChar = currentProcessor.getEndChar();
                if (endChar != LAOperatorProcessor.EMTRY_CHAR){
                    if (c == endChar){
                        operator.setEndChar(c);
                        processorAndOperators.add(new StageHybridProcessorAndOperator(operator, currentProcessor));
                        operator = nextOperator;
                        nextOperator = new EachParagraphOperator();
                        currentProcessor = null;
                    }else {
                        operator.addMediumQuantity(c);
                    }
                }else {
                    parallelFindEnd = true;
                    nextOperator.addFrontQuantity(c);
                }
            }
        }

        if (currentProcessor != null){
            operator.setMediumQuantity(nextOperator.getFrontQuantity());
            processorAndOperators.add(new StageHybridProcessorAndOperator(operator, currentProcessor));
        }
        return processorAndOperators;
    }


    public Collection<LAOperatorProcessor> getOperatorProcessors() {
        return operatorProcessors;
    }
}
