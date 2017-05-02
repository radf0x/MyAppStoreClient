package com.fox.myappstore.utils.concurrent;

import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;

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

public class MyHandler extends Handler implements MyHandlerCallback {

    private final WeakReference< MyHandlerCallback > mCallback;
    // only used for debug purpose.
    private String mCallbackClassName;

    public MyHandler( MyHandlerCallback callback ) {
        mCallback = new WeakReference< MyHandlerCallback >( callback );
    }

    @Override
    public void handleMessage( Message msg ) {
        MyHandlerCallback callback = mCallback.get();
        if ( null != callback ) {
            callback.handleMessage( msg );
        }
    }
}
