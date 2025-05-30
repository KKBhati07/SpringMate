package com.example.SpringMate.Helpers;
import java.util.UUID;

public final class CoreHelper {

    private CoreHelper() {}

    public static String generateUUID() {
        return UUID.randomUUID().toString();
    }


}
