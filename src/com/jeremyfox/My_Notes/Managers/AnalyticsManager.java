package com.jeremyfox.My_Notes.Managers;

import com.jeremyfox.My_Notes.Activities.MainActivity;
import com.jeremyfox.My_Notes.Classes.Environment;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created with IntelliJ IDEA.
 * User: jeremy
 * Date: 3/28/13
 * Time: 12:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class AnalyticsManager {

    /**
     * The single instance of AnalyticsManager
     */
    private static AnalyticsManager instance = null;
    private MixpanelAPI mixpanelAPI;
    private static final String API_TOKEN = "e8fb97ba8b60cfbc027dfb2e337cd822";
    private static final String API_TOKEN_DEBUG = "8c692559bf5c07a59ab6cf31c1975a76";

    /**
     * Protected to hide the constructor. Must use getInstance method.
     */
    protected AnalyticsManager() {
        String apiToken = API_TOKEN;
        if (Environment.isDebug()) {
            apiToken = API_TOKEN_DEBUG;
        }
        this.mixpanelAPI = MixpanelAPI.getInstance(MainActivity.ACTIVITY, apiToken);
    }

    /**
     * Gets instance.
     *
     * @return the instance
     */
    public static AnalyticsManager getInstance() {
        if(instance == null) {
            instance = new AnalyticsManager();
        }
        return instance;
    }

    /**
     * Fire event.
     *
     * @param eventKey the event key
     * @param propertiesMap the properties map
     */
    public void fireEvent(String eventKey, HashMap<String, String> propertiesMap) {
        JSONObject eventProperties = null;
        if (propertiesMap != null) {
            eventProperties = new JSONObject();
            Iterator iterator = propertiesMap.entrySet().iterator();
            while (iterator.hasNext()) {
                HashMap.Entry pairs = (HashMap.Entry)iterator.next();
                String entryKey = (pairs.getKey().toString().length() > 0) ? pairs.getKey().toString() : "no_key";
                String entryValue = (pairs.getValue().toString().length() > 0) ? pairs.getValue().toString() : "no_value";
                try {
                    eventProperties.put(entryKey, entryValue);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        this.mixpanelAPI.track(eventKey, eventProperties);
    }

    /**
     * Flush events.
     */
    public void flushEvents() {
        this.mixpanelAPI.flush();
    }

    /**
     * Register super property.
     *
     * @param key the key
     * @param property the property
     */
    public void registerSuperProperty(String key, String property) {
        JSONObject superProperties = new JSONObject();
        try {
            superProperties.put(key, property);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        this.mixpanelAPI.registerSuperProperties(superProperties);
    }

}
