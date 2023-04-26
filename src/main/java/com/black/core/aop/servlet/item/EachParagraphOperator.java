package com.black.core.aop.servlet.item;

public class EachParagraphOperator {

    //前字符量
    String frontQuantity = "";

    char startChar, endChar;

    //中间字符量
    String mediumQuantity = "";

    public void setMediumQuantity(String mediumQuantity) {
        this.mediumQuantity = mediumQuantity;
    }

    public void setFrontQuantity(String frontQuantity) {
        this.frontQuantity = frontQuantity;
    }

    public void setStartChar(char startChar) {
        this.startChar = startChar;
    }

    public void setEndChar(char endChar) {
        this.endChar = endChar;
    }

    public void addFrontQuantity(char c){
        frontQuantity = frontQuantity + c;
    }

    public void addMediumQuantity(char c){
        mediumQuantity = mediumQuantity + c;
    }

    public String getFrontQuantity() {
        return frontQuantity;
    }

    public char getStartChar() {
        return startChar;
    }

    public char getEndChar() {
        return endChar;
    }

    public String getMediumQuantity() {
        return mediumQuantity;
    }

    @Override
    public String toString() {
        return "EachParagraphOperator{" +
                "frontQuantity='" + frontQuantity + '\'' +
                ", startChar=" + startChar +
                ", endChar=" + endChar +
                ", mediumQuantity='" + mediumQuantity + '\'' +
                '}';
    }
}
