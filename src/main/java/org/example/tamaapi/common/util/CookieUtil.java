package org.example.tamaapi.common.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.SerializationUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Base64;

public class CookieUtil {

    // 요청값 (이름, 값, 만료기간)을 바탕으로 HTTP 응답에 쿠키를 추가한다.
    public static void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        cookie.setHttpOnly(true);
        cookie.setSecure(true); // HTTPS 환경에서만 쿠키가 전송됩니다.
        response.addCookie(cookie);
    }

    // 쿠키 이름을 입력받아 쿠키를 삭제
    // 실제로 삭제하는 방법은 없으므로 해당 쿠키를 빈값으로 바꾸고 만료시간을 0으로 설정해 만료 처리
    public static void deleteCookie(HttpServletRequest request, HttpServletResponse response, String name) {
        Cookie[] cookies = request.getCookies();
        if( cookies == null) return;

        for(Cookie cookie : cookies) {
            if(name.equals(cookie.getName())) {
                cookie.setValue("");
                cookie.setPath("/");
                cookie.setMaxAge(0);
                response.addCookie(cookie);
            }
        }
    }

    // 객체를 직렬화해 쿠키의 값으로 들어갈 값으로 변환
    public static String serialize(Object object) {
        return Base64.getUrlEncoder().encodeToString(SerializationUtils.serialize(object));
    }

    // 쿠키를 역질렬화 객체로 변환
    public static <T> T deserialize(Cookie cookie, Class<T> cls) {
        byte[] data = Base64.getUrlDecoder().decode(cookie.getValue());

        try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
             ObjectInputStream ois = new ObjectInputStream(bais)) {

            Object obj = ois.readObject();
            return cls.cast(obj);
        } catch (IOException | ClassNotFoundException e) {
            throw new IllegalArgumentException("쿠키 역직렬화 실패", e);
        }
    }
}