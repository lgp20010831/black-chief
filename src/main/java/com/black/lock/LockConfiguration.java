package com.black.lock;

import com.black.JsonBean;
import lombok.Getter;
import lombok.Setter;

@Setter @Getter
public class LockConfiguration extends JsonBean {

    private int limit;

    private boolean fair;

}
