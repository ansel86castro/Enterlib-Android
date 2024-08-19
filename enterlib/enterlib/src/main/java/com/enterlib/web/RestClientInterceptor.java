package com.enterlib.web;

import java.net.HttpURLConnection;

public interface RestClientInterceptor {

    HttpURLConnection handleRequest(RestClientRequest request, RestClientRequestHandler next);
}
