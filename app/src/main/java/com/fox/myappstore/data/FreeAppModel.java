package com.fox.myappstore.data;

import com.fox.myappstore.data.subModels.AppArtistModel;
import com.fox.myappstore.data.subModels.AppCategoryModel;
import com.fox.myappstore.data.subModels.AppIdModel;
import com.fox.myappstore.data.subModels.AppImageModel;
import com.fox.myappstore.data.subModels.AppLinkModel;
import com.fox.myappstore.data.subModels.AppNameModel;
import com.fox.myappstore.data.subModels.AppRightsModel;
import com.fox.myappstore.data.subModels.AppSummaryModel;
import com.fox.myappstore.data.subModels.AppTitleModel;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Copyright 2017 RavicPN
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class FreeAppModel {
    @SerializedName( "im:name" )
    AppNameModel appNameModel;

    @SerializedName( "im:image" )
    List< AppImageModel > appImageModel;

    @SerializedName( "summary" )
    AppSummaryModel appSummaryModel;

    @SerializedName( "rights" )
    AppRightsModel appRightsModel;

    @SerializedName( "title" )
    AppTitleModel appTitleModel;

    @SerializedName( "link" )
    AppLinkModel appLinkModel;

    @SerializedName( "id" )
    AppIdModel appIdModel;

    @SerializedName( "artist" )
    AppArtistModel appArtistModel;

    @SerializedName( "category" )
    AppCategoryModel appCategoryModel;

    private String userRating;

    public AppNameModel getAppNameModel() {
        return appNameModel;
    }

    /**
     * @return index 0 = 53x53
     * index 1 = 75x75
     * index 2 = 100x100
     */
    public List< AppImageModel > getAppImageModel() {
        return appImageModel;
    }

    public AppSummaryModel getAppSummaryModel() {
        return appSummaryModel;
    }

    public AppRightsModel getAppRightsModel() {
        return appRightsModel;
    }

    public AppTitleModel getAppTitleModel() {
        return appTitleModel;
    }

    public AppLinkModel getAppLinkModel() {
        return appLinkModel;
    }

    public AppIdModel getAppIdModel() {
        return appIdModel;
    }

    public AppArtistModel getAppArtistModel() {
        return appArtistModel;
    }

    public AppCategoryModel getAppCategoryModel() {
        return appCategoryModel;
    }

    public void setUserRating( String userRating ) {
        this.userRating = userRating;
    }

    public String getUserRating() {
        return userRating;
    }

    @Override
    public boolean equals( Object obj ) {
        if ( obj instanceof FreeAppModel ) {
            FreeAppModel aObj = ( FreeAppModel ) obj;
            //
            return aObj.getAppNameModel().getName().equals( getAppNameModel().getName() );

        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( getAppIdModel().getAppAttributes().getId() == null ) ? 0 : getAppIdModel().getAppAttributes().getId().hashCode() );
        return result;
    }
}
