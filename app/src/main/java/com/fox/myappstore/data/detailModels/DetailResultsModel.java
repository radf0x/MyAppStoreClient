package com.fox.myappstore.data.detailModels;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Ravic on 4/5/2017.
 */

public class DetailResultsModel {
    @SerializedName( "averageUserRating" )
    public String averageUserRating;
    @SerializedName( "userRatingCount" )
    public String userRatingCount;
    @SerializedName( "description" )
    public String description;

    public String getAverageUserRating() {
        return averageUserRating;
    }

    public String getUserRatingCount() {
        return userRatingCount;
    }

    public String getDescription() {
        return description;
    }
}
