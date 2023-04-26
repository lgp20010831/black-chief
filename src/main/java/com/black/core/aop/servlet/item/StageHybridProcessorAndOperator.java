package com.black.core.aop.servlet.item;

public class StageHybridProcessorAndOperator {

    private final EachParagraphOperator paragraphOperator;
    private final LAOperatorProcessor processor;

    public StageHybridProcessorAndOperator(EachParagraphOperator paragraphOperator, LAOperatorProcessor processor) {
        this.paragraphOperator = paragraphOperator;
        this.processor = processor;
    }


    public EachParagraphOperator getParagraphOperator() {
        return paragraphOperator;
    }

    public LAOperatorProcessor getProcessor() {
        return processor;
    }

    @Override
    public String toString() {
        return paragraphOperator.toString();
    }
}
