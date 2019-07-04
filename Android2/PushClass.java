package com.objects;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by Keyur on 9/3/2015.
 */
public class PushClass implements Serializable {

    @SerializedName("ntype")
    public String ntype;

    @SerializedName("meta_value")
    public String meta_value;

    @SerializedName("badge")
    public int badge;
}
