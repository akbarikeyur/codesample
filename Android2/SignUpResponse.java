package com.objects;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

@SuppressWarnings("serial")
public class SignUpResponse implements Serializable {

    @SerializedName("statuscode")
    public int statuscode;

    @SerializedName("msg")
    public String msg;

    @SerializedName("data")
    public Data data;

    public class Data {
        @SerializedName("user_id")
        public String user_id;

        @SerializedName("name")
        public String name;

        @SerializedName("email")
        public String email;
    }
}
