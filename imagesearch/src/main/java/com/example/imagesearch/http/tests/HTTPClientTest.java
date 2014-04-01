package com.example.imagesearch.http.tests;

import android.test.InstrumentationTestCase;

import com.example.imagesearch.http.HTTPClient;
import com.example.imagesearch.http.HTTPRequest;
import com.example.imagesearch.http.HTTPResponse;

public class HTTPClientTest extends InstrumentationTestCase {

    public void testReceiveDataAsStringByDefault() throws Exception {
        HTTPClient client = new HTTPClient("http://www.google.com");
        HTTPRequest request = client.get("/");
        HTTPResponse response = request.getResponse();
        assertEquals(String.class.toString(), response.getData().getClass().toString());
    }

}
