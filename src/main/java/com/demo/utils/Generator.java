package com.demo.utils;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.text.RandomStringGenerator;

import java.text.DecimalFormat;
import java.util.Random;

public class Generator {

    public static int genInt(int from, int to) {
        return new Random().nextInt((to - from) + 1) + from;
    }

    public static String genString(int length) {
        return new RandomStringGenerator.Builder()
                .withinRange('a', 'z')
                .build()
                .generate(length);
    }

    public static String getRandomStringNumber(int length) {
        return new RandomStringGenerator.Builder()
                .withinRange('0', '9')
                .build()
                .generate(length);
    }

    public static String genAccountNumber() {
        return genInt(1, 9) + getRandomStringNumber(11);
    }

    public static String genMobilePhone(int length) {
        return "555" + getRandomStringNumber(7);
    }

    public static String genEmail() {
        return "testemail+" + getRandomStringNumber(7) + "@gmail.com";
    }

    public static float genFloat(double from, double to, int precision) {
        float number = genFloat(from, to);
        return (float) Math.round(number * Math.pow(10, precision)) / (float) Math.pow(10, precision);
    }

    private static float genFloat(double from, double to) {
        float tmp = .0f;
        if (to >= from)
            tmp = (float) (from + (Math.random() * (to - from)));
        return tmp;
    }

    public static String genAddress() {
        return "Test " + Generator.genString(4) + " Avenue, " + Generator.genInt(1, 1000);
    }

    public static long genLong(long from, long to) {
        long tmp = 0;
        if (to >= from)
            tmp = from + Math.round((Math.random() * (to - from)));
        return tmp;
    }

    public static String getRandomFormattedDecimalStringValue(String pattern) {
        Random ran = new Random();
        DecimalFormat df = new DecimalFormat(pattern);
        return String.valueOf(df.format(ran.nextFloat() * 100));
    }

    public static String getRandomIntMinMaxInclusiveStringValue(int min, int max ) {
        Random ran = new Random();
        return Integer.toString(ran.nextInt(max - min + 1) + min);
    }
}
