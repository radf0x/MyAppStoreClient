package com.fox.myappstore.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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

public class GsonHelper {
    private static Gson mGson;

    public static Gson getGson() {
        if ( mGson == null ) {
            mGson = new GsonBuilder().create();
        }
        return mGson;
    }

    public static < T > T fromJson( String json, Class< T > targetClass ) {
        try {
            return getGson().fromJson( json, targetClass );
        }
        catch ( Exception e ) {
            e.printStackTrace();
            return null;
        }
    }
}
