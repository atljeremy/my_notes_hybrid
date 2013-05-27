package com.jeremyfox.My_Notes.Managers;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import com.jeremyfox.My_Notes.Interfaces.NetworkCallback;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created with IntelliJ IDEA.
 * User: jeremy
 * Date: 3/18/13
 * Time: 9:01 PM
 */
public class NetworkManager {

    /**
     * The single instance of NetworkManager
     */
    private static NetworkManager instance = null;
    /**
     * The constant FAILURE_UNKNOWN_STATUS. Used to determine if a network request failed without returning a status code.
     */
    public static final int FAILURE_UNKNOWN_STATUS = -1;
    /**
     * The constant SUCCESS_STATUS. Used to tell the callback.onFailure(statusCode) was and to compare against returned status codes.
     */
    public static final int SUCCESS_STATUS = 200;
    /**
     * The constant SUCCESS_RECORD_CREATED_STATUS. Used to tell the callback.onFailure(statusCode) was and to compare against returned status codes.
     */
    public static final int SUCCESS_RECORD_CREATED_STATUS = 201;
    /**
     * The constant SUCCESS_RECORD_DELETED_STATUS. Used to tell the callback.onFailure(statusCode) was and to compare against returned status codes.
     */
    public static final int SUCCESS_RECORD_DELETED_STATUS = 204;
    /**
     * The constant NOT_MODIFIED_STATUS. Used to tell the callback.onFailure(statusCode) was and to compare against returned status codes.
     */
    public static final int NOT_MODIFIED_STATUS = 304;

    /**
     * The constant API_HOST.
     */
    public static final String API_HOST = "https://young-cove-5823.herokuapp.com";

    public enum RequestType {

        /**
         * Specifies a GET request.
         */
        GET,

        /**
         * Specifies a POST request.
         */
        POST,

        /**
         * Specifies a PUT request.
         */
        PUT,

        /**
         * Specifies a DELETE request.
         */
        DELETE
    }

    /**
     * Instantiates a new Network manager.
     */
    protected NetworkManager() {
    }

    /**
     * Gets instance.
     *
     * @return the instance
     */
    public static NetworkManager getInstance() {
        if(instance == null) {
            instance = new NetworkManager();
        }
        return instance;
    }

    /**
     * Is connected helper method. Use this method to determine network connectivity.
     *
     * @param context the context
     * @return the boolean
     */
    public static boolean isConnected(Context context) {

        boolean isConnected = false;

        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = connectivityManager.getActiveNetworkInfo();
        if(ni!=null){
            if(ni.isConnected()){
                isConnected = true;
            }
        }

        return isConnected;
    }

    /**
     * Execute get request.
     *
     * @param url the url
     * @return the jSON array
     */
    public JSONArray executeSynchronousGetRequest(Context context, String url) {
        HttpRequestBase httpRequest = new HttpGet(url);
        JSONArray jsonArray = null;
        String jsonString = null;
        try {
            jsonString = new NetworkAsyncTask().execute(httpRequest, null).get(20, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

        if (null != jsonString && jsonString.length() > 0) {
            try {
                jsonArray = new JSONArray(jsonString);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return jsonArray;
    }

    /**
     * Execute get request.
     *
     * @param url the url
     * @param callback the callback
     */
    public void executeGetRequest(Context context, String url, NetworkCallback callback) {
        executeRequest(context, url, null, RequestType.GET, callback);
    }

    /**
     * Execute post request.
     *
     * @param url the url
     * @param params the params
     * @param callback the callback
     */
    public void executePostRequest(Context context, String url, JSONObject params, NetworkCallback callback) {
        executeRequest(context, url, params, RequestType.POST, callback);
    }

    /**
     * Execute put request.
     *
     * @param url the url
     * @param params the params
     * @param callback the callback
     */
    public void executePutRequest(Context context, String url, JSONObject params, NetworkCallback callback) {
        executeRequest(context, url, params, RequestType.PUT, callback);
    }

    /**
     * Execute delete request.
     *
     * @param url the url
     * @param params the params
     * @param callback the callback
     */
    public void executeDeleteRequest(Context context, String url, JSONObject params, NetworkCallback callback) {
        executeRequest(context, url, params, RequestType.DELETE, callback);
    }

    /**
     * Execute request.
     *
     * @param url the url
     * @param requestType the RequestType
     * @param callback the callback
     */
    private void executeRequest(Context context, String url, JSONObject params, RequestType requestType, NetworkCallback callback) {

        if (isConnected(context) && null != url) {

            HttpRequestBase httpRequest;
            switch (requestType) {
                case POST:
                    httpRequest = new HttpPost(url);
                    if (null != params && params.length() > 0) {
                        StringEntity se = null;
                        try {
                            se = new StringEntity(params.toString());
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                        ((HttpPost)httpRequest).setEntity(se);
                        httpRequest.setHeader("Accept", "application/json");
                        httpRequest.setHeader("Content-type", "application/json");
                    }
                    break;

                case PUT:
                    httpRequest = new HttpPut(url);
                    if (null != params && params.length() > 0) {
                        StringEntity se = null;
                        try {
                            se = new StringEntity(params.toString());
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                        ((HttpPut)httpRequest).setEntity(se);
                        httpRequest.setHeader("Accept", "application/json");
                        httpRequest.setHeader("Content-type", "application/json");
                    }
                    break;

                case DELETE:
                    httpRequest = new HttpDelete(url);
                    break;

                case GET:
                default:
                    httpRequest = new HttpGet(url);
                    break;
            }

            new NetworkAsyncTask().execute(httpRequest, callback);
        } else {
            callback.onFailure(NetworkManager.FAILURE_UNKNOWN_STATUS);
        }
    }

    /**
     * Creates and returns HttpRequestBase.
     *
     * @param url the url
     * @param requestType the RequestType
     */
    public HttpRequestBase createHttpRequest(String url, JSONObject params, RequestType requestType) {

        HttpRequestBase httpRequest;

        switch (requestType) {
            case POST:
                httpRequest = new HttpPost(url);
                if (null != params && params.length() > 0) {
                    StringEntity se = null;
                    try {
                        se = new StringEntity(params.toString());
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                    ((HttpPost)httpRequest).setEntity(se);
                    httpRequest.setHeader("Accept", "application/json");
                    httpRequest.setHeader("Content-type", "application/json");
                }
                break;

            case PUT:
                httpRequest = new HttpPut(url);
                if (null != params && params.length() > 0) {
                    StringEntity se = null;
                    try {
                        se = new StringEntity(params.toString());
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                    ((HttpPut)httpRequest).setEntity(se);
                    httpRequest.setHeader("Accept", "application/json");
                    httpRequest.setHeader("Content-type", "application/json");
                }
                break;

            case DELETE:
                httpRequest = new HttpDelete(url);
                break;

            case GET:
            default:
                httpRequest = new HttpGet(url);
                break;
        }

        return httpRequest;
    }
}

class NetworkAsyncTask extends AsyncTask<Object, Integer, String> {

    private NetworkCallback callback;

    protected String doInBackground(Object... params) {

        HttpRequestBase httpRequestBase = (HttpRequestBase)params[0];
        this.callback = (NetworkCallback)params[1];

        StringBuilder stringBuilder = new StringBuilder();
        HttpClient httpClient = new DefaultHttpClient();
        try {
            HttpResponse response = httpClient.execute(httpRequestBase);
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            boolean received200Status = (statusCode == NetworkManager.SUCCESS_STATUS);
            boolean received201Status = (statusCode == NetworkManager.SUCCESS_RECORD_CREATED_STATUS);
            boolean received204Status = (statusCode == NetworkManager.SUCCESS_RECORD_DELETED_STATUS);
            boolean received304Status = (statusCode == NetworkManager.NOT_MODIFIED_STATUS);

            if (received200Status || received201Status || received204Status || received304Status) {

                HttpEntity entity = response.getEntity();
                if (null != entity) {
                    InputStream inputStream = entity.getContent();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    String line;

                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line);
                    }

                    inputStream.close();
                }
            }
        } catch (Exception e) {
            Log.d("executeGetRequest", e.getLocalizedMessage());
        }

        return stringBuilder.toString();
    }

    protected void onPostExecute(String result) {
        final String JSON_ARRAY_OPEN_BRACKET = "[";
        boolean isJsonArray = false;

        if (null != this.callback) {
            if (null == result || result.length() == 0) {
                result = "{}";
            } else {
                int index = JSON_ARRAY_OPEN_BRACKET.indexOf(result.charAt(0));
                if (index >= 0) {
                    isJsonArray = true;
                }
            }
            try {
                if (isJsonArray) {
                    this.callback.onSuccess(new JSONArray(result));
                } else {
                    this.callback.onSuccess(new JSONObject(result));
                }
            } catch (JSONException e) {
                e.printStackTrace();
                this.callback.onFailure(NetworkManager.FAILURE_UNKNOWN_STATUS);
            }
        }
    }
}
