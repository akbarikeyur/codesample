package com.objects;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

@SuppressWarnings("serial")
public class LoginResponse implements Serializable {

    @SerializedName("statuscode")
    public int statuscode;

    @SerializedName("msg")
    public String msg;

    @SerializedName("data")
    public LoginData data;

    public class LoginData {

        @SerializedName("u_id")
        public String user_id;

        @SerializedName("name")
        public String fullname;

        @SerializedName("email")
        public String email;

        @SerializedName("avatar")
        public String avatar;

        @SerializedName("is_show")
        public String is_show;

        @SerializedName("is_sound")
        public String is_sound;

        @SerializedName("is_vibrate")
        public String is_vibrate;

        @SerializedName("auth_token")
        public String auth_token;

    }
}
