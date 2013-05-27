package com.jeremyfox.My_Notes.Classes;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: jeremy
 * Date: 4/21/13
 * Time: 9:41 PM
 */
public class ResponseObject implements Serializable {

    private Object object;
    private RequestStatus status;

    /**
     * The enum Request status.
     */
    public enum RequestStatus {
        /**
         * The STATUS _ SUCCESS.
         */
        STATUS_SUCCESS,

        /**
         * The STATUS _ FAILED.
         */
        STATUS_FAILED }

    /**
     * Instantiates a new Response object.
     *
     * @param object the object
     */
    public ResponseObject(Object object, RequestStatus status){
        setObject(object);
        setStatus(status);
    }

    /**
     * Gets object.
     *
     * @return the object
     */
    public Object getObject() {
        return object;
    }

    /**
     * Sets object.
     *
     * @param object the object
     */
    public void setObject(Object object) {
        this.object = object;
    }

    /**
     * Gets status.
     *
     * @return the status
     */
    public RequestStatus getStatus() {
        return status;
    }

    /**
     * Sets status.
     *
     * @param status the status
     */
    public void setStatus(RequestStatus status) {
        this.status = status;
    }
}
