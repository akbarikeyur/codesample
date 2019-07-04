package com.objects;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

@SuppressWarnings("serial")
public class MotivationResponse implements Serializable {

    @SerializedName("statuscode")
    public int statuscode;

    @SerializedName("msg")
    public String msg;

    @SerializedName("data")
    public ArrayList<MotivationList> data = new ArrayList<>();

    public class MotivationList {

        @SerializedName("motivation_id")
        public String motivation_id;

        @SerializedName("name")
        public String name;

        @SerializedName("description")
        public String description;
    }
}
