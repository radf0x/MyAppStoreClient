package com.fox.myappstore;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import com.fox.myappstore.utils.NetworkStateBroadcastReceiver;

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

public class NetworkService extends Service {

    private final String TAG = this.getClass().getSimpleName();
    public static final String NETWORK_STATE_DISCONNECTED = "NetworkService.Disconnected";
    public static final String NETWORK_STATE_RECONNECTED = "NetworkService.Reconnected";
    public static final String NETWORK_STATE_CHANGED = "NetworkService.Changed";

    private final RemoteCallbackList< INetworkServiceCallback > remoteCallback = new RemoteCallbackList<>();

    private final INetworkService.Stub remoteBinder = new INetworkService.Stub() {
        @Override
        public void registerNetworkCallback( INetworkServiceCallback callback ) throws RemoteException {
            if ( null != callback ) {
                remoteCallback.register( callback );
            }
        }

        @Override
        public void unregisterCallback( INetworkServiceCallback callback ) throws RemoteException {
            if ( null != callback ) {
                remoteCallback.unregister( callback );
            }
        }
    };

    @Override
    public void onCreate() {
        Log.d( TAG, "onCreate()" );
        super.onCreate();
        NetworkStateBroadcastReceiver.initNetworkStatus( getApplicationContext() );
    }

    @Override
    public int onStartCommand( Intent intent, int flags, int startId ) {
        if ( null != intent ) {
            String caller = intent.getDataString();
            String action = intent.getAction();
            Log.i( TAG, "onStartCommand: caller -> " + caller + " | action -> " + action );
            try {
                int broadCastItems = remoteCallback.beginBroadcast();
                for ( int i = 0; i < broadCastItems; i++ ) {
                    remoteCallback.getBroadcastItem( i ).onConnectionStatusChanged( action );
                }
                remoteCallback.finishBroadcast();
            }
            catch ( RemoteException e ) {
                e.printStackTrace();
            }
        }
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind( Intent intent ) {
        return remoteBinder;
    }

    @Override
    public boolean onUnbind( Intent intent ) {
        return super.onUnbind( intent );
    }
}
