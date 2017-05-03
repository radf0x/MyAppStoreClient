package com.fox.myappstore.utils.concurrent;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.MainThread;

import com.fox.myappstore.data.ServerResponse;
import com.fox.myappstore.utils.GsonHelper;
import com.fox.myappstore.widgets.callbacks.OnTaskCompleted;

import java.io.IOException;
import java.io.InputStream;

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

public class AsyncLoaderTask extends AsyncTask< String, String, ServerResponse > {
    public static final int EVENT_APP_LIST = 1;
    public static final int EVENT_RECOMMEND = EVENT_APP_LIST << 1;

    private final String TAG = this.getClass().getSimpleName();
    private Context context;
    private OnTaskCompleted mListener;
    private String fileName;
    private int eventId;

    public AsyncLoaderTask( Context context, String fileName, int eventId ) {
        this.context = context;
        this.fileName = fileName;
        this.eventId = eventId;
    }

    public AsyncLoaderTask addOnTaskCompletedListener( OnTaskCompleted listener ) {
        mListener = listener;
        return this;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected ServerResponse doInBackground( String... params ) {
        return composeModel( loadJSONFromAsset() );
    }

    @SuppressWarnings( { "UnusedDeclaration" } )
    @MainThread
    @Override
    protected void onPostExecute( ServerResponse model ) {
        if ( null != mListener ) {
            mListener.onTaskCompleted( eventId, model );
        }
    }

    public ServerResponse composeModel( String jsonStr ) {
        ServerResponse response = GsonHelper.fromJson( jsonStr, ServerResponse.class );
        return response;
    }

    public String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = context.getAssets().open( fileName );
            int size = is.available();
            byte[] buffer = new byte[ size ];
            is.read( buffer );
            is.close();
            json = new String( buffer, "UTF-8" );
        }
        catch ( IOException ex ) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }
}
