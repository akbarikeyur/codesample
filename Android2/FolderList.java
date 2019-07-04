package com.objects;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by Keyur on 30-04-2017.
 */
public class FolderList implements Serializable {

    @SerializedName("folder_id")
    public String folder_id;

    @SerializedName("name")
    public String folder_name;

}
