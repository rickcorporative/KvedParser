package com.demo.utils;

import java.security.SecureRandom;

public class Random {
    private static int shortRandAdd = 0;
    private static int longRandAdd = 0;

    public static int genInt(int from, int to) {
        int tmp = 0;
        if (to >= from)
            tmp = (int) (from + Math.round((Math.random() * (to - from))));
        return tmp;
    }

    private static float genFloat(double from, double to) {
        float tmp = .0f;
        if (to >= from)
            tmp = (float) (from + (Math.random() * (to - from)));
        return tmp;
    }

    private static synchronized long genShortRandNumberByTime() {
        return (genInt(1, 9) * 10_000_000L) + (System.currentTimeMillis() % 10_000_000) + shortRandAdd++;
    }

    public static String genString(int length) {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < length; i++) {
            s.append((char) Random.genInt(97, 120));
        }
        return s.toString();
    }

    public static String genSpecialString(int length) {
        String specialChars = "!@#$%^&*()_+-=[]{}|;':\"<>,.?/~`";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(specialChars.length());
            sb.append(specialChars.charAt(randomIndex));
        }

        return sb.toString();
    }


    public static long genLong(long from, long to) {
        long tmp = 0;
        if (to >= from)
            tmp = from + Math.round((Math.random() * (to - from)));
        return tmp;
    }

    public static String genName(int length) {
        return Random.genString(1).toUpperCase() + Random.genString(length - 1);
    }

    public static String genAddress() {
        return "Test " + Random.genString(4) + " Avenue, " + Random.genInt(1, 1000);
    }

    public static String genPostalCode() {
        return String.valueOf(Random.genInt(1, 9)) + Random.genInt(10, 99) + Random.genInt(10, 99);
    }

    public static String genURL() {
        return "http://test" + Random.genString(5) + ".com";
    }

    public static String genTimestart() {
        String[] times = {"Immediately", "In about a week", "In about a month"};
        int value = Random.genInt(0, 2);
        return times[value];
    }

    public static String genCity() {
        return "New test " + Random.genString(4) + "ville";
    }


    public static String genMobilePhone() {
        return "555" + genInt(1000000, 9999999);
    }


    public static String genEmail(String emailPattern) {
        return emailPattern.substring(0, emailPattern.indexOf("@")) + "+" + genShortRandNumberByTime()
                + emailPattern.substring(emailPattern.indexOf("@"));
    }

    public static float genFloat(double from, double to, int precision) {
        float number = genFloat(from, to);
        return (float) Math.round(number * Math.pow(10, precision)) / (float) Math.pow(10, precision);
    }

    public static synchronized long genRandNumberByTime() {
        return System.currentTimeMillis() % 10_000_000_000L + longRandAdd++;
    }

    /**
     * Card Number Generation
     */
    public static String genCardNumber() {
        long from = 1000_0000_0000_0000L;
        long to = 9999_9999_9999_9999L;
        return String.valueOf((from + Math.round((Math.random() * (to - from)))));
    }

    public static String genCardNumberStartWith(String startWith) {
        String cardNumber = genCardNumber();
        String editedCardNumber = startWith + cardNumber.substring(4);
        return editedCardNumber;
    }

    public static String genCardNumberEndWith(String endWith) {

        String cardNumber = genCardNumber();
        String editedCardNumber = cardNumber.substring(0, cardNumber.length() - 4) + endWith;
        return editedCardNumber;
    }

    public static String genCardNumberStartWithEndWith(String startWith, String endWith) {
        String cardNumber = genCardNumberStartWith(startWith);
        String editedCardNumber = cardNumber.substring(0, cardNumber.length() - 4) + endWith;
        return editedCardNumber;
    }
}
