package com.bc2fa.a2fa;


import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Set;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;

import static com.bc2fa.a2fa.CryptoUtils.calculateApiKey;

/**
 * Created by Oleg Levitsky on 09.04.2018.
 */
public class HeaderInterceptor implements Interceptor {

    private String bodyToString(final RequestBody request) {
        try {
            final Buffer buffer = new Buffer();
            if (request != null) {
                request.writeTo(buffer);
            } else
                return "";
            return buffer.readUtf8();
        } catch (final IOException e) {
            return "did not work";
        }
    }

    @Override
    public Response intercept(Chain chain)
            throws IOException {
        Request request = chain.request();

        String key;
        switch (request.method()) {
            case "GET":
                key = "";

                Set<String> parameterNames = request.url().queryParameterNames();
                StringBuilder encodedBody = new StringBuilder();
                for (String param : parameterNames) {
                    encodedBody.append(param).append(":").append(request.url().queryParameterValues(param).get(0)).append(";");
                }

                if (request.url().queryParameterValues("phone_number").get(0).length() < 1){
                    break;
                }

                key = calculateApiKey(request.url().encodedPath(), encodedBody.toString(), request.url().queryParameterValues("phone_number").get(0));
                // Log.d("Retrofit", String.format("Sending request body %s", encodedBody));
                // Log.d("Retrofit", String.format("Sending request path %s", request.url().encodedPath()));
                // Log.d("Retrofit", String.format("Sending request phone_number %s", request.url().queryParameterValues("phone_number").get(0)));
                // Log.d("Retrofit", String.format("Sending request api_key %s", key));

                break;
            case "POST":
                key = "";

                String body = bodyToString(request.body());
                String phoneNumber;

                try {
                    JSONObject jsonObject = new JSONObject(body);
                    if (jsonObject.getString("phone_number").length() < 1){
                        break;
                    }
                    phoneNumber = jsonObject.getString("phone_number");
                } catch (JSONException e) {
                    break;
                }

                body = body.substring(1, body.length() - 2);
                body = body.replaceAll(",\"", ";");
                body = body.replaceAll("\"", "");
                body += ";";

                key = calculateApiKey(request.url().encodedPath(), body, phoneNumber);

                // Log.d("Retrofit", String.format("Sending request phone_number %s", phoneNumber));
                // Log.d("Retrofit", String.format("Sending request body %s", body));
                break;
            default:
                key = "";
                break;
        }

        request = request.newBuilder()
                .addHeader("api-key", key)
                .removeHeader("User-Agent")
                .addHeader("User-Agent", "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:38.0) Gecko/20100101 Firefox/38.0")
                .build();

        return chain.proceed(request);
    }
}
