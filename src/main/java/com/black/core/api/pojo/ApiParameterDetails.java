package com.black.core.api.pojo;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter @ToString
public class ApiParameterDetails {

     private String type;

     private String name;

     private String remark;

     private boolean required;
}
