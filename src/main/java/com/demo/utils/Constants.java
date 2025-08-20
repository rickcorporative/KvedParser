package com.demo.utils;

import com.demo.data.GlobalContext;

public class Constants {
    public static ThreadLocal<GlobalContext> globalContext = new ThreadLocal<>();
    //URL constants
    public static String BASE_URL;
    public static String LOGIN_URL;

    // Time constants
    public static int NANO_TIMEOUT;
    public static int MICRO_TIMEOUT;
    public static int MINI_TIMEOUT;
    public static int SMALL_TIMEOUT;
    public static int BIG_TIMEOUT;

    public static int SCREEN_WIDTH;
    public static int SCREEN_HEIGHT;

    public static int TIMEOUT_BEFORE_FAIL;
}
