package com.objects;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by Amisha on 7/7/16.
 */
public class StatusResponse implements Serializable {

    @SerializedName("statuscode")
    public int statuscode;

    @SerializedName("msg")
    public String msg;
}
