package com.theblood.productservice.util;

import java.util.Arrays;

public class ExtractIdUtil {
    // extract by ,
    public Object getIdsList(String ids) {
        if (ids.contains(",")) return ids;
        return Arrays.asList(ids.split(","));
    }
}
