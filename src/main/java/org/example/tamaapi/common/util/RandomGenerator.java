package org.example.tamaapi.common.util;

import java.security.SecureRandom;

public class RandomGenerator {
    private static final String NUMBER_CHARACTERS = "0123456789";

    private static final String A2Z_NUMBER_CHARACTERS = "abcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom secureRandom = new SecureRandom();

    public static int generateRandomNumber(int length) {
        StringBuilder randomString = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int index = secureRandom.nextInt(NUMBER_CHARACTERS.length());
            randomString.append(NUMBER_CHARACTERS.charAt(index));
        }

        return  Integer.parseInt(randomString.toString());
    }



    public static String generateRandomString(int length) {
        StringBuilder randomString = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int index = secureRandom.nextInt(A2Z_NUMBER_CHARACTERS.length());
            randomString.append(A2Z_NUMBER_CHARACTERS.charAt(index));
        }

        return randomString.toString();
    }
}
