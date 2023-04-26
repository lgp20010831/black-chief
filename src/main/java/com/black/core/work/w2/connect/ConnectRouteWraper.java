package com.black.core.work.w2.connect;

import com.black.core.json.Alias;
import com.black.core.work.utils.WorkUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor @Getter  @Setter
public class ConnectRouteWraper {

    String id;

    String workflowId;

    String name;

    String startAlias;

    String endAlias;

    String engineName;

    //条件处理
    Condition condition;
    @Alias("conditional")
    String conditionExpression;

    public ConnectRouteWraper(String startAlias, String endAlias,
                              String engineName, Condition condition,
                              String name) {
        this.startAlias = startAlias;
        this.endAlias = endAlias;
        this.engineName = engineName;
        this.condition = condition;
        this.name = name;
    }

    public ConnectRouteWraper rom(){
        id = WorkUtils.getRandomId();
        return this;
    }
}
