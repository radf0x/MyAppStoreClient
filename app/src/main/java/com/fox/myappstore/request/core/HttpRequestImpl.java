package com.fox.myappstore.request.core;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.io.IOException;

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

public abstract class HttpRequestImpl extends HttpRequest {

    // Bundle key
    public static final String IS_SUCCESSFUL = "success";
    public static final String HTTP_STATUS_CODE = "code";
    public static final String HTTP_RESPONSE_BODY = "body";
    public static final String IO_EXCEPTION = "ioe";

    // Internal data
    private Handler mHandler;
    private int mWhat;
    private Object mObject;
    private Receiver mReceiver;

    public interface Receiver {
        void onReceiveResult( Bundle bundle );
    }

    /**
     * Constructor.
     *
     * @param handler Target.
     * @param what    Request code.
     * @param obj     Input argument to be passing back to target.
     */
    public HttpRequestImpl( Handler handler, int what, Object obj ) {
        mHandler = handler;
        mWhat = what;
        mObject = obj;
    }

    public HttpRequestImpl( Handler handler, int what ) {
        this( handler, what, null );
    }

    public HttpRequestImpl( Receiver receiver ) {
        mReceiver = receiver;
    }

    protected abstract RequestBuilder newBuilder();

    public final void sendRequest() {
        sendRequest( newBuilder().build() );
    }

    @Override
    protected void handleFailure( IOException e ) {
        Bundle bundle = new Bundle();
        bundle.putBoolean( IO_EXCEPTION, true );
        sendResult( bundle );
    }

    /**
     * Returns true if the code is in [200..300), which means the request was successfully received,
     * understood, and accepted.
     */
    @Override
    protected void handleResponse( boolean isSuccessful, int code, String body ) {
        Bundle bundle = new Bundle();
        bundle.putBoolean( IS_SUCCESSFUL, isSuccessful );
        bundle.putInt( HTTP_STATUS_CODE, code );
        bundle.putString( HTTP_RESPONSE_BODY, body );
        sendResult( bundle );
    }

    /**
     * Send message back to handler.
     *
     * @param bundle Contains server response. Null if connectivity problem or timeout.
     */
    protected void sendResult( Bundle bundle ) {
        if ( mHandler != null ) {
            Message msg = Message.obtain( mHandler, mWhat, mObject );
            msg.setData( bundle );
            msg.sendToTarget();
        } else if ( mReceiver != null ) {
            mReceiver.onReceiveResult( bundle );
        }
    }
}
