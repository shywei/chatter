package com.baidu.speech.serviceapi;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import javax.xml.bind.DatatypeConverter;

import org.json.JSONObject;

public class Sample {

    private static final String serverURL1 = "http://vop.baidu.com/server_api";
    private static final String serverURL2 = "http://tsn.baidu.com/text2audio";
    private static String token = "";
    private static final String testFileName = "output.wav";
    //put your own params here
    private static final String apiKey = "WNOLe4NEXY5GZFYNzEDc5WIq";
    private static final String secretKey = "Z1wHxMhg76Ig35VhSSvWF3CN7G2y7ULQ";
    private static final String cuid = "sadadaw";
    private static String text = "";

    public static void main(String[] args) throws Exception {
        getToken();
        method1();
        method2();
    }

    private static void getToken() throws Exception {
        String getTokenURL = "https://openapi.baidu.com/oauth/2.0/token?grant_type=client_credentials" + 
            "&client_id=" + apiKey + "&client_secret=" + secretKey;
        HttpURLConnection conn = (HttpURLConnection) new URL(getTokenURL).openConnection();
        token = new JSONObject(printResponse(conn)).getString("access_token");
    }

    private static void method1() throws Exception {
        File pcmFile = new File(testFileName);
        HttpURLConnection conn = (HttpURLConnection) new URL(serverURL1).openConnection();

        // construct params
        JSONObject params = new JSONObject();
        params.put("format", "wav");
        params.put("rate", 16000);
        params.put("channel", "1");
        params.put("token", token);
        params.put("cuid", cuid);
        params.put("len", pcmFile.length());
        params.put("speech", DatatypeConverter.printBase64Binary(loadFile(pcmFile)));
        System.out.println("len:"+pcmFile.length());
        System.out.println(DatatypeConverter.printBase64Binary(loadFile(pcmFile)));
        // add request header
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");

        conn.setDoInput(true);
        conn.setDoOutput(true);

        // send request
        DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
        wr.writeBytes(params.toString());
        wr.flush();
        wr.close();

        String result = printResponse(conn);
        text=(new JSONObject(result)).get("result").toString();
    }

    private static void method2() throws Exception {
    	File pcmFile = new File(testFileName);
        HttpURLConnection conn = (HttpURLConnection) new URL(serverURL2).openConnection();
        
        // add request header
        conn.setRequestMethod("POST");
        conn.setDoInput(true);
        conn.setDoOutput(true);

        // send request
        DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
        wr.writeBytes("tex="+URLEncoder.encode(text,"UTF-8")+"&lan="+URLEncoder.encode("zh","UTF-8")+"&ctp="+URLEncoder.encode("1","UTF-8")+"&tok="+URLEncoder.encode(token,"UTF-8")+"&cuid="+URLEncoder.encode(cuid,"UTF-8"));
        wr.flush();
        wr.close();

        System.out.println(conn.getResponseCode());
        BufferedInputStream bin = new BufferedInputStream(conn.getInputStream());

        String path = "temp.mp3";
        int fileLength = conn.getContentLength();
        File file = new File(path);
        OutputStream out = new FileOutputStream(file);
        int size = 0;
        int len = 0;
        byte[] buf = new byte[1024];
        while ((size = bin.read(buf)) != -1) {
            len += size;
            out.write(buf, 0, size);
            // 打印下载百分比
            System.out.println("下载了-------> " + len * 100 / fileLength +"%\n");
        }
        bin.close();
        out.close();
    }

    private static String printResponse(HttpURLConnection conn) throws Exception {
        if (conn.getResponseCode() != 200) {
            // request error
            return "";
        }
        InputStream is = conn.getInputStream();
        BufferedReader rd = new BufferedReader(new InputStreamReader(is));
        String line;
        StringBuffer response = new StringBuffer();
        while ((line = rd.readLine()) != null) {
            response.append(line);
            response.append('\r');
        }
        rd.close();
        System.out.println(new JSONObject(response.toString()).toString(4));
        return response.toString();
    }

    private static byte[] loadFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);

        long length = file.length();
        byte[] bytes = new byte[(int) length];

        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
                && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
            offset += numRead;
        }

        if (offset < bytes.length) {
            is.close();
            throw new IOException("Could not completely read file " + file.getName());
        }

        is.close();
        return bytes;
    }
}
