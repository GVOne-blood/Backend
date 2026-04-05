package com.theblood.springfood.common.dto.request;

public class UserContextHolder {
    private static final ThreadLocal<CustomUserPrincipal> userContext = new InheritableThreadLocal<>();

    public static CustomUserPrincipal getContext() {
        CustomUserPrincipal context = userContext.get();

        if (context == null) {
            context = createEmptyContext();
            userContext.set(context);

        }
        return userContext.get();
    }

    public static void setContext(CustomUserPrincipal context) {
        userContext.set(context);
    }

    public static CustomUserPrincipal createEmptyContext() {
        return new CustomUserPrincipal();
    }

    public static void clearContext() {
        userContext.remove();
    }
}
