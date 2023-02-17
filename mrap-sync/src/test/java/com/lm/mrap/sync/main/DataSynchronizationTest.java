package com.lm.mrap.sync.main;

import org.junit.Test;

public class DataSynchronizationTest {

    @Test
    public void testMain() throws Exception {
        // Setup
        // Run the test
        DataSynchronization.main(new String[]{"args"});

        // Verify the results
    }

    @Test(expected = Exception.class)
    public void testMain_ThrowsException() throws Exception {
        // Setup
        // Run the test
        DataSynchronization.main(new String[]{"args"});
    }
}
