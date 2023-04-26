package com.black.syntax;

import java.util.Map;

public interface SyntaxInterlocutor {

    void interlude(String item, Map<String, Object> env, Object resolveResult);

}
