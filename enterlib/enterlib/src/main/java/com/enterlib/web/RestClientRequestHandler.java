package com.enterlib.web;

import java.io.IOException;
import java.net.HttpURLConnection;

public  interface RestClientRequestHandler {
    HttpURLConnection handleRequest(RestClientRequest request);
}
