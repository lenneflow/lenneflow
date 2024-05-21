package de.lenneflow.taskservice.util;

import java.util.UUID;

public class Util {

    public static String generateUUID(){
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }
}
