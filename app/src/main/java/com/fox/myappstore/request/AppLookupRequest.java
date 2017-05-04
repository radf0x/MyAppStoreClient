package com.fox.myappstore.request;

import android.os.Handler;

import com.fox.myappstore.request.core.HttpRequestImpl;
import com.fox.myappstore.request.core.RequestBuilder;

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


public class AppLookupRequest extends HttpRequestImpl {

    private final String RELATIVE_URL = "lookup?id=";
    private int appId;

    public AppLookupRequest( Handler handler, int what, Object obj ) {
        super( handler, what, obj );
    }

    public AppLookupRequest( Handler handler, int what, int appId ) {
        super( handler, what );
        this.appId = appId;
    }

    public AppLookupRequest( Receiver receiver ) {
        super( receiver );
    }

    @Override
    protected RequestBuilder newBuilder() {
        return new RequestBuilder().relativeUrl( RELATIVE_URL + appId ).get();
    }
}
