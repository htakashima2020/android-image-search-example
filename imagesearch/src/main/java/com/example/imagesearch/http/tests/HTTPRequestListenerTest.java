package com.example.imagesearch.http.tests;

import android.test.InstrumentationTestCase;

import com.example.imagesearch.http.HTTPClient;
import com.example.imagesearch.http.HTTPRequest;
import com.example.imagesearch.http.HTTPRequestListener;
import com.example.imagesearch.http.HTTPResponse;

/**
 * HTTPClientTest
 *
 * Basic tests to ensure GET is working properly.
 *
 * - Can receive a request [ ]
 * - Can receive chunks of a request [ ]
 * - Response has data [ ]
 * - JSON Response can be decoded [ ]
 */
public class HTTPRequestListenerTest extends InstrumentationTestCase {
    public void testDidSucceed() throws Exception {
        final boolean[] didSucceed = new boolean[1];
        HTTPClient client = new HTTPClient("http://www.google.com", 10); // 10 second timeout
        HTTPRequest request = client.GET("/", null, new HTTPRequestListener() {
            @Override
            public void didReceiveData(byte[] data) {

            }

            @Override
            public void canReceiveProgress(boolean val) {

            }

            @Override
            public void didReceiveProgress(float progress) {

            }

            @Override
            public void didSucceed(HTTPResponse response) {
                didSucceed[0] = true;
            }

            @Override
            public void didFail(HTTPResponse response) {
                didSucceed[0] = false;
            }

            @Override
            public void didError(Exception e) {
            }
        });

        HTTPResponse httpResponse = request.getResponse(); // wait for response..
        assertEquals(true, didSucceed[0]);
        assertEquals(httpResponse.getStatusCode(), 200);
    }
    public void testCanReceiveProgress() throws Exception {
        // hitting google.com doesn't return a content-length so canReceiveProgress... should return false
        final boolean[] canReceiveProgress = new boolean[1];
        final int[] numTimesRcvdProgress = new int[]{0};

        HTTPClient client = new HTTPClient("http://www.google.com", 10); // 10 second timeout
        HTTPRequest request = client.GET("/", null, new HTTPRequestListener() {

            @Override
            public void didReceiveData(byte[] data) {
            }

            @Override
            public void canReceiveProgress(boolean val) {
                canReceiveProgress[0] = val;

            }

            @Override
            public void didReceiveProgress(float progress) {
            }

            @Override
            public void didSucceed(HTTPResponse response) {
            }

            @Override
            public void didFail(HTTPResponse response) {
            }

            @Override
            public void didError(Exception e) {
            }
        });
        HTTPResponse httpResponse = request.getResponse(); // wait for response..
        assertFalse(canReceiveProgress[0]);
        assertEquals(200, httpResponse.getStatusCode());


        // google's logoo does have a content length..
        final float[] lastProgress = new float[1];

        request = client.GET("/images/srpr/logo11w.png", null, new HTTPRequestListener() {
            @Override
            public void didReceiveData(byte[] data) {
                numTimesRcvdProgress[0]++;
            }

            @Override
            public void canReceiveProgress(boolean val) {
                canReceiveProgress[0] = val;
            }

            @Override
            public void didReceiveProgress(float progress) {
                System.out.println("Got progress: " + progress);
                lastProgress[0] = progress;
            }

            @Override
            public void didSucceed(HTTPResponse response) {

            }

            @Override
            public void didFail(HTTPResponse response) {

            }

            @Override
            public void didError(Exception e) {
            }
        });
        httpResponse = request.getResponse();
        assertTrue(numTimesRcvdProgress[0] > 5);
        assertTrue(canReceiveProgress[0]);
        assertTrue(lastProgress[0] > 0.50 && lastProgress[0] <= 1.00);
        assertEquals(200, httpResponse.getStatusCode());
    }

}
