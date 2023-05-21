package com.black.graphql.core.request.result;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@SuppressWarnings("all")
public class ResultAttributtes {
    private String name;
    public List<ResultAttributtes> resultAttributtes = new ArrayList();

    public ResultAttributtes(String name) {
        this.name = name;
    }

    public ResultAttributtes addResultAttributes(String... resultAttr) {
        if (resultAttr != null && resultAttr.length > 0) {
            String[] var2 = resultAttr;
            int var3 = resultAttr.length;

            for(int var4 = 0; var4 < var3; ++var4) {
                String str = var2[var4];
                ResultAttributtes ra = new ResultAttributtes(str);
                this.resultAttributtes.add(ra);
            }
        }

        return this;
    }

    public ResultAttributtes addResultAttributes(ResultAttributtes... resultAttr) {
        if (resultAttr != null && resultAttr.length > 0) {
            ResultAttributtes[] var2 = resultAttr;
            int var3 = resultAttr.length;

            for(int var4 = 0; var4 < var3; ++var4) {
                ResultAttributtes ra = var2[var4];
                this.resultAttributtes.add(ra);
            }
        }

        return this;
    }

    public String toString() {
        if (this.resultAttributtes.size() == 0) {
            return this.name;
        } else {
            String str = this.name + "{";

            ResultAttributtes ra;
            for(Iterator var2 = this.resultAttributtes.iterator(); var2.hasNext(); str = str + " " + ra.toString()) {
                ra = (ResultAttributtes)var2.next();
            }

            str = str + " }";
            return str;
        }
    }
}
