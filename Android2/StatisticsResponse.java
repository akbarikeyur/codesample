package com.objects;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

@SuppressWarnings("serial")
public class StatisticsResponse implements Serializable {

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

    @SerializedName("task_per_day")
    public String task_per_day;

    @SerializedName("time_per_day")
    public String time_per_day;

    @SerializedName("time_per_task")
    public String time_per_task;

}
