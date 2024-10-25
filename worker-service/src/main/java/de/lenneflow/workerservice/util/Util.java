package de.lenneflow.workerservice.util;

public class Util {

    private Util(){}

    public static void pause(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
