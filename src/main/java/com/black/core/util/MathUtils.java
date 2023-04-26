package com.black.core.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

public class MathUtils {

    public static boolean power2(int n){
        return (n & (n - 1)) == 0;
    }

    public static boolean power4(int n){
        return power2(n) && n % 3 == 1;
    }

    public static double sqrt(double a){
        return Math.sqrt(a);
    }

    //是否为完全平方根
    public static boolean csqrt(double a){
        return (int)sqrt(a) * (int)sqrt(a) == a;
    }

    //次方数
    public static double pow(double a, double n){
        return Math.pow(a, n);
    }

    public static int sumN(int n){
        return (1 + n) * n / 2;
    }


    /**
     * 提供精确的加法运算。
     * @param v1 被加数
     * @param v2 加数
     * @return 两个参数的和
     */
    public static double add(double v1, double v2) {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return (b1.add(b2)).doubleValue();
    }

    /**
     * 提供精确的减法运算。
     * @param v1 被减数
     * @param v2 减数
     * @return 两个参数的差
     */
    public static double sub(double v1, double v2) {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return (b1.subtract(b2)).doubleValue();
    }

    /**
     * 提供精确的乘法运算。
     * @param v1 被乘数
     * @param v2 乘数
     * @return 两个参数的积
     */
    public static double mul(double v1, double v2) {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return (b1.multiply(b2)).doubleValue();
    }

    /**
     * 提供（相对）精确的除法运算，当发生除不尽的情况时，精确到
     * 小数点以后多少位，以后的数字四舍五入。
     * @param v1 被除数
     * @param v2 除数
     * @return 两个参数的商
     */
    public static double div(double v1, double v2) {
        return div(v1, v2, 10);
    }

    /**
     * 提供（相对）精确的除法运算。当发生除不尽的情况时，由scale参数指
     * 定精度，以后的数字四舍五入。
     * @param v1 被除数
     * @param v2 除数
     * @param scale 表示需要精确到小数点以后几位。
     * @return 两个参数的商
     */
    public static double div(double v1, double v2, int scale) {
        if (scale < 0) {
            System.err.println("除法精度必须大于0!");
            return 0;
        }
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return (b1.divide(b2, scale, RoundingMode.HALF_UP)).doubleValue();
    }


    /**
     * 计算Factorial阶乘！
     * @param n   任意大于等于0的int
     * @return     n!的值
     */
    public static BigInteger getFactorial(int n) {
        if (n < 0) {
            System.err.println("n必须大于等于0！");
            return new BigInteger("-1");
        } else if (n == 0) {
            return new BigInteger("0");
        }
        //将数组换成字符串后构造BigInteger
        BigInteger result = new BigInteger("1");
        for (; n > 0; n--) {
            //将数字n转换成字符串后，再构造一个BigInteger对象，与现有结果做乘法
            result = result.multiply(new BigInteger(Integer.toString(n)));
        }
        return result;
    }
}
