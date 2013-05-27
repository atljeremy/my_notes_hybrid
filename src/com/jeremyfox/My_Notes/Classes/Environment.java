package com.jeremyfox.My_Notes.Classes;

/**
 * Created with IntelliJ IDEA.
 * User: jeremy
 * Date: 3/30/13
 * Time: 3:44 PM
 */
public class Environment {

    /**
     * The single instance of AnalyticsManager
     */
    private static Environment instance = null;

    /**
     * The enum App env.
     */
    public static enum AppEnv { APP_ENV_PROD, APP_ENV_DEBUG }

    private AppEnv appEnvironment;

    /**
     * Instantiates a new Environment.
     */
    protected Environment() {
        setAppEnvironment(AppEnv.APP_ENV_PROD);
    }

    /**
     * Gets instance.
     *
     * @return the instance
     */
    public static Environment getInstance() {
        if(instance == null) {
            instance = new Environment();
        }
        return instance;
    }

    /**
     * Gets app environment.
     *
     * @return the app environment
     */
    public AppEnv getAppEnvironment() {
        return appEnvironment;
    }

    /**
     * Sets app environment.
     *
     * @param appEnvironment the app environment
     */
    public void setAppEnvironment(AppEnv appEnvironment) {
        this.appEnvironment = appEnvironment;
    }

    public static boolean isDebug() {
        return Environment.getInstance().getAppEnvironment() != AppEnv.APP_ENV_PROD;
    }
}
