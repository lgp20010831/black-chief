package com.black.asm;

import com.black.core.json.Alias;
import org.objectweb.asm.*;

import java.io.IOException;
import java.util.List;

public class Demo {


    public static void main(String[] args) throws IOException {
        ClassReader reader = new ClassReader("com.black.asm.Demo$User");
        reader.accept(new ClassVisitor(Opcodes.ASM5) {
            //s=方法名
            //s1 = 方法签名 ()V, (Lcom/black/asm/Demo$User;)Ljava/util/List;
            //s2=方法泛型签名  (Lcom/black/asm/Demo$User;)Ljava/util/List<Ljava/lang/String;>;

            @Override
            public MethodVisitor visitMethod(int i, String s, String s1, String s2, String[] strings) {
                return super.visitMethod(i, s, s1, s2, strings);
            }

            //s = 字段名称
            //s1 = 字段类型签名 Lcom/black/asm/Demo$User; I
            //s2 = 字段泛型签名
            @Override
            public FieldVisitor visitField(int i, String s, String s1, String s2, Object o) {
                return super.visitField(i, s, s1, s2, o);
            }

            @Override
            public void visitEnd() {
                super.visitEnd();
            }

            //s = 注解签名 Lcom/black/core/json/Alias;
            @Override
            public AnnotationVisitor visitAnnotation(String s, boolean b) {
                return super.visitAnnotation(s, b);
            }

            @Override
            public AnnotationVisitor visitTypeAnnotation(int i, TypePath typePath, String s, boolean b) {
                return super.visitTypeAnnotation(i, typePath, s, b);
            }

            @Override
            public void visitAttribute(Attribute attribute) {
                super.visitAttribute(attribute);
            }
        }, 0);
    }

    @Alias
    public static class User{

        @Alias
        User user;

        int age;

        String name;

        public void say(){
            System.out.println("say");
        }

        public List<String> get(User user){
            return null;
        }
    }
}
