package com.objects;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

@SuppressWarnings("serial")
public class TodayTaskListResponse implements Serializable {

    @SerializedName("statuscode")
    public int statuscode;

    @SerializedName("msg")
    public String msg;

    @SerializedName("total_task_count")
    public String total_task_count;

    @SerializedName("complete_task_count")
    public String complete_task_count;

    @SerializedName("total_task_time")
    public String total_task_time;

    @SerializedName("complete_task_time")
    public String complete_task_time;


    @SerializedName("data")
    public ArrayList<TodayTaskList> data = new ArrayList<>();
    public class TodayTaskList{
        @SerializedName("task_id")
        public String task_id;

        @SerializedName("name")
        public String name;

        @SerializedName("folder_id")
        public String folder_id;

        @SerializedName("folder_name")
        public String folder_name;

        @SerializedName("task_date")
        public String task_date;

        @SerializedName("task_time")
        public String task_time;

        @SerializedName("is_reset")
        public String is_reset;

        @SerializedName("is_play")
        public String is_play;

        @SerializedName("is_complete")
        public String is_complete;

        @SerializedName("complete_time")
        public String complete_time;

        @SerializedName("created_date")
        public String created_date;
    }

}
