package com.objects;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

@SuppressWarnings("serial")
public class CommonResponse implements Serializable {

    @SerializedName("statuscode")
    public int statuscode;

    @SerializedName("msg")
    public String msg;

    @SerializedName("data")
    public Data data;

    public class Data {

        @SerializedName("token")
        public String token;
    }
}
