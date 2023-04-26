package com.black.core.test.at;

import com.black.core.json.Trust;
import lombok.Data;

import java.util.List;

@Data @Trust
public class Usercls {

    String name;

    String phone;

    int age;

    List<Roleust> roleusts;
}
