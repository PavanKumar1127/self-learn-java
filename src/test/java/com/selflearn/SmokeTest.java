package com.selflearn;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SmokeTest {

    @Test
    void toolchainRuns() {
        assertTrue(Runtime.version().feature() >= 21);
    }
}
