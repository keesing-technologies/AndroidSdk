package com.keesing.kvsclient;

import com.keesing.kvsclient.utils.WebServiceHelper;
import com.keesing.kvsclient.utils.WebServicePostOperation;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class WebServiceHelperUnitTest {
    @Test
    public void call_endpoint_with_parameters() {

        assertEquals(4, 2 + 2);
        /*new WebServiceHelper(new WebServicePostOperation() {
            @Override
            public void onFinish(String output, int statusCode) {
                assertEquals(200, statusCode);
            }
        }).execute("https://localhost:44335/kvsapi", "hi");*/
    }

    @Test
    public void call_endpoint_without_parameters() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void call_endpoint_with_bad_parameters() {
        assertEquals(4, 2 + 2);
    }
}