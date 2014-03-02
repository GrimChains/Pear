package com.example.pear;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class URLReader {
    public String read(String s) throws Exception {

        URL oracle = new URL(s);
        URLConnection conn = oracle.openConnection();
        BufferedReader in = new BufferedReader(
        new InputStreamReader(conn.getInputStream()));

        String inputLine = "";
        String inputLine2 = "";
        while ((inputLine = in.readLine()) != null)
            inputLine2 += inputLine; 
        in.close();
        return inputLine2;
    }
}