package org.example.tamaapi.common.util;

import java.security.SecureRandom;

public class RandomNumberGenerator {
    private static final String CHARACTERS = "0123456789";
    private static final SecureRandom secureRandom = new SecureRandom();

    public static int generateRandomNumber(int length) {
        StringBuilder randomString = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int index = secureRandom.nextInt(CHARACTERS.length());
            randomString.append(CHARACTERS.charAt(index));
        }

        return  Integer.parseInt(randomString.toString());
    }

}
