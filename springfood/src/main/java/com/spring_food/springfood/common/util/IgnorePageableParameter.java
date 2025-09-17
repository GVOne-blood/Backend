package com.spring_food.springfood.common.util;

import java.util.Map;

public class IgnorePageableParameter {

    private static final String attributes = "page|size|sort";

    public static boolean ignoreFromMap(Map<?, ?> map){
        return !(map.containsKey("page") );
    }
}
