package com.fox.myappstore.request.core;

import android.util.Log;

import com.fox.myappstore.MainApplication;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;

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

abstract class HttpRequest implements Callback {
    private final boolean DEBUG = false;
    public static final String TAG = HttpRequest.class.getSimpleName();

    private static final OkHttpClient mOkHttpClient = new OkHttpClient.Builder() //
            .protocols( Arrays.asList( Protocol.HTTP_2, Protocol.HTTP_1_1 ) ) //
            .connectionPool( new ConnectionPool() ) //
            .readTimeout( 1, TimeUnit.MINUTES ) //
            .writeTimeout( 1, TimeUnit.MINUTES ) //
            .connectTimeout( 1, TimeUnit.MINUTES ) //
            .cache( new Cache( MainApplication.getInstance().getCacheDir(), 10 * 1024 * 1024 ) ) //
            .build();

    protected final void sendRequest( Request request ) {
        mOkHttpClient.newCall( request ).enqueue( this );
    }

    protected abstract void handleFailure( IOException e );

    protected abstract void handleResponse( boolean isSuccessful, int code, String body );

    @Override
    public final void onFailure( Call call, IOException e ) {
        if ( DEBUG ) {
            Log.d( TAG, "onFailure()" );
            e.printStackTrace();
        }
        handleFailure( e );
    }

    @Override
    public final void onResponse( Call call, Response response ) throws IOException {
        if ( response.request().cacheControl().onlyIfCached() && response.code() == 504 /*Unsatisfiable Request*/ ) {
            if ( DEBUG ) {
                Log.d( TAG, "onResponse(): " + response.toString() );
            }
            CacheControl control;
            int maxStaleSeconds = call.request().cacheControl().maxStaleSeconds();
            if ( maxStaleSeconds > 0 ) {
                control = new CacheControl.Builder().maxStale( maxStaleSeconds, TimeUnit.SECONDS ).build();
            } else {
                control = new CacheControl.Builder().noCache().noStore().build();
            }
            sendRequest( call.request().newBuilder().cacheControl( control ).build() );
        } else {
            String body = response.body().string();
            if ( DEBUG ) {
                Log.d( TAG, "onResponse(): " + response.toString() + //
                        " network=" + ( response.networkResponse() != null ) + //
                        " cache=" + ( response.cacheResponse() != null ) + //
                        ( ( body.length() > 0 ) ? ( "\nBody: " + body ) : "" ) );
            }
            handleResponse( response.isSuccessful(), response.code(), body );
        }
    }
}