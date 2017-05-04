package com.fox.myappstore.request;

import android.os.Handler;

import com.fox.myappstore.request.core.HttpRequestImpl;
import com.fox.myappstore.request.core.RequestBuilder;

/**
 * Created by Ravic on 4/5/2017.
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
