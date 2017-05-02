package com.fox.myappstore.data;

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

public class ServerResponse {
    @SerializedName( "feed" )
    FeedModel feed;

    public FeedModel getFeed() {
        return feed;
    }

    public class FeedModel {
        @SerializedName( "author" )
        AuthorModel author;

        @SerializedName( "entry" )
        List< FreeAppModel > freeAppModels;

        public AuthorModel getAuthor() {
            return author;
        }

        public List< FreeAppModel > getFreeAppModels() {
            return freeAppModels;
        }
    }

    public class AuthorModel {
        @SerializedName( "name" )
        Name name;

        public Name getName() {
            return name;
        }

        public class Name {
            @SerializedName( "label" )
            String label;

            public String getLabel() {
                return label;
            }
        }
    }
}
