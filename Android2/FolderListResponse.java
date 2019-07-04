package com.objects;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

@SuppressWarnings("serial")
public class FolderListResponse implements Serializable {

    @SerializedName("statuscode")
    public int status;

    @SerializedName("msg")
    public String msg;

    @SerializedName("data")
    public ArrayList<FolderList> folderList = new ArrayList<>();
}
