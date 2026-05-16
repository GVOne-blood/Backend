package com.theblood.springfood.common.util;

import com.theblood.springfood.common.exception.custom.InvalidDataException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

public class CookieUtil {


    public static Cookie createCookie(String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        // SameSite=Lax: cookie được gửi với same-site requests (kể cả top-level navigation),
        // KHÔNG gửi với cross-site XHR. Phù hợp với dev proxy (FE+BE cùng origin).
        // Production HTTPS có thể cân nhắc SameSite=None + Secure cho cross-origin SPA.
        cookie.setAttribute("SameSite", "Lax");
        cookie.setMaxAge(maxAge);

        return cookie;
    }

    public static String getElementFromCookie(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            throw new InvalidDataException("No cookies found in request");
        }
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(cookieName)) {
                return cookie.getValue();
            }
        }
        throw new InvalidDataException("Cookie '" + cookieName + "' not found");

    }
}
