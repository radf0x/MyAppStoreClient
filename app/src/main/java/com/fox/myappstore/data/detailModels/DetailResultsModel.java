package com.fox.myappstore.data.detailModels;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Ravic on 4/5/2017.
 */

public class DetailResultsModel {
    @SerializedName( "averageUserRating" )
    public float averageUserRating;
    @SerializedName( "userRatingCount" )
    public String userRatingCount;
    @SerializedName( "description" )
    public String description;
    @SerializedName( "trackId" )
    public int appId;
    @SerializedName( "trackName" )
    public String appName;

    public float getAverageUserRating() {
        return averageUserRating;
    }

    public String getUserRatingCount() {
        return userRatingCount;
    }

    public String getDescription() {
        return description;
    }

    public int getAppId() {
        return appId;
    }

    public String getAppName() {
        return appName;
    }
}
