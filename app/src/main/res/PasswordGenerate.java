package com.example.mykhail.tracklocationv20;

import java.util.Random;

/**
 * Created by kompot on 18.04.2016.
 */
public class PasswordGenerate {
    public static String generatePass() {
        String password = "";
        Random random = new Random();
        for (int i = 0; i < 8; ++i) {
            char next = 0;
            int range = 10;

            switch (random.nextInt(2)) {
                case 0: {
                    next = '0';
                    range = 10;
                }
                break;
                case 1: {
                    next = 'a';
                    range = 26;
                }
                break;
            }
            password += (char) ((random.nextInt(range)) + next);
        }

        return password;
    }
}
