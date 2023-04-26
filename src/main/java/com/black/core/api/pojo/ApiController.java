package com.black.core.api.pojo;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter @ToString
public class ApiController {

    private String remark;

    List<ApiRestInterface> restInterfaces;
}
