package com.fox.myappstore.request.core;

import com.fox.myappstore.SharedConstants;

import java.util.concurrent.TimeUnit;

import okhttp3.CacheControl;
import okhttp3.Request;

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

public class RequestBuilder {

    private final String TAG = this.getClass().getSimpleName();
    private final boolean DEBUG = false;

    // Internal data
    private Request.Builder mBuilder = new Request.Builder();
    private String mHost;
    private String mPath;
    private int mKeepCacheFor = -1; // in seconds
    private boolean mTryCache = false;
    private final static CacheControl FORCE_NETWORK_NO_STORE = new CacheControl.Builder().noCache().noStore().build();


    /**
     * Default Constructor.
     *
     * Will set host to values specified in {@link SharedConstants}
     */
    public RequestBuilder() {
        this( SharedConstants.HOST );
    }

    /**
     * Constructor
     *
     * @param host e.g. https://itunes.apple.com/hk
     */
    public RequestBuilder( String host ) {
        mHost = host;
    }

    public RequestBuilder keepCacheFor( int sec ) {
        mKeepCacheFor = sec;
        return this;
    }

    public RequestBuilder tryCache( boolean tryCache ) {
        mTryCache = tryCache;
        return this;
    }

    /**
     * Set relative Url.
     *
     * @param path Relative path. e.g. /rss/topgrossingapplications/limit=10/json
     * @return Builder
     */
    public RequestBuilder relativeUrl( String path ) {
        mPath = path;
        return this;
    }

    /**
     * Configure GET method.
     *
     * @return Builder
     */
    public RequestBuilder get() {
        mBuilder.get();
        return this;
    }

    public Request build() {
        mBuilder.url( mHost.concat( mPath == null ? "" : mPath ) );
        // Cache control
        if ( mTryCache || mKeepCacheFor > 0 ) {
            CacheControl.Builder cb = new CacheControl.Builder();
            if ( mTryCache ) {
                cb.onlyIfCached();
            }
            if ( mKeepCacheFor > 0 ) {
                cb.maxStale( mKeepCacheFor, TimeUnit.SECONDS );
            }
            mBuilder.cacheControl( cb.build() );
        } else {
            mBuilder.cacheControl( FORCE_NETWORK_NO_STORE );
        }

        return mBuilder.build();
    }
}
