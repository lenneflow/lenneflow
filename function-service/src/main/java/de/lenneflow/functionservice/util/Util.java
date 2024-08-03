package de.lenneflow.functionservice.util;

import java.util.UUID;

public class Util {

    public static String generateUUID(){
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }
}
