package com.fox.myappstore.data.detailModels;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Ravic on 4/5/2017.
 */

public class DetailResponseModel {
    @SerializedName( "results" )
    public List< DetailResultsModel > models;

    public List< DetailResultsModel > getModels() {
        return models;
    }
}
