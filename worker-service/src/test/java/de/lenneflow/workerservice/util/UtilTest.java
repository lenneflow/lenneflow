package de.lenneflow.workerservice.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UtilTest {

    @Test
    void pause_shouldPauseForGivenMilliseconds() {
        long startTime = System.currentTimeMillis();
        Util.pause(100);
        long endTime = System.currentTimeMillis();
        assertTrue((endTime - startTime) >= 100);
    }

    @Test
    void pause_shouldHandleInterruptedException() {
        Thread.currentThread().interrupt();
        Util.pause(100);
        assertTrue(Thread.interrupted());
    }

    @Test
    void executeSSHCommand_shouldThrowExceptionForInvalidCredentials() {
        assertThrows(Exception.class, () -> Util.executeSSHCommand("invalidUser", "invalidPass", "host", 22, "command"));
    }

    @Test
    void executeSSHCommand_shouldThrowExceptionForInvalidHost() {
        assertThrows(Exception.class, () -> Util.executeSSHCommand("username", "password", "invalidHost", 22, "command"));
    }
}