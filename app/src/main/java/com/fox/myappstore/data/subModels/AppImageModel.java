package com.fox.myappstore.data.subModels;

import com.google.gson.annotations.SerializedName;

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

public class AppImageModel {
    @SerializedName( "label" )
    String iconUrl;
    @SerializedName( "attributes" )
    Attributes attributes;

    public String getIconUrl() {
        return iconUrl;
    }

    public Attributes getAttributes() {
        return attributes;
    }

    public class Attributes {
        @SerializedName( "height" )
        int height;

        public int getHeight() {
            return height;
        }
    }
}
