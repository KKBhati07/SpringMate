package com.example.SpringMate.Shared.Helper;
import java.util.UUID;

public final class CoreHelper {

    private CoreHelper() {}

    public static UUID generateUUID() {
        return UUID.randomUUID();
    }


}
