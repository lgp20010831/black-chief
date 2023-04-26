package com.black.core.api.tacitly;

public class FormDataRequestExampleReader extends AbstractRequestBufferExampleReader{


    public FormDataRequestExampleReader(ApiAliasManger aliasManger, AnalysisProcessActuator processActuator) {
        super(aliasManger, processActuator);
    }

    @Override
    protected int writeColon(StringBuilder builder, int offSet) {
        writeOff(builder, offSet);
        builder.append("=");
        return offSet + 1;
    }
}
