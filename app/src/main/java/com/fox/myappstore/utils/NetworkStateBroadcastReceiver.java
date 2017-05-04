package com.fox.myappstore.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.fox.myappstore.NetworkService;

import static com.fox.myappstore.NetworkService.NETWORK_STATE_CHANGED;
import static com.fox.myappstore.NetworkService.NETWORK_STATE_DISCONNECTED;
import static com.fox.myappstore.NetworkService.NETWORK_STATE_RECONNECTED;

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

public class NetworkStateBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = NetworkStateBroadcastReceiver.class.getSimpleName();
    private static int lastNetworkType = -1;

    public static void initNetworkStatus( Context context ) {
        ConnectivityManager manager = ( ConnectivityManager ) context.getSystemService( Context.CONNECTIVITY_SERVICE );
        NetworkInfo info = manager.getActiveNetworkInfo();
        Log.i( TAG, "MyBroadcastReceiver.Init: ACTIVE NetworkInfo: " + ( info != null ? info.toString() : "NONE" ) );
        lastNetworkType = -1;
        if ( ( null != info ) && info.isConnected() ) {
            lastNetworkType = info.getType();
        }
        if ( lastNetworkType == -1 ) {
            Log.i( TAG, "MyBroadcastReceiver.Init: No active Network." );
        }
    }

    @Override
    public void onReceive( Context context, Intent intent ) {
        String action = intent.getAction();
        Intent serviceIntent = new Intent( context, NetworkService.class );
        Log.i( TAG, "MyBroadcastReceiver.onReceive( " + action + " )" );

        if ( action.equals( Intent.ACTION_SHUTDOWN ) ) {
            Log.d( TAG, "Device shut down. Stopping NetworkStateService." );
        } else if ( action.equals( ConnectivityManager.CONNECTIVITY_ACTION ) ) {
            Log.d( TAG, "MyBroadcastReceiver: Connectivity changed" );
            ConnectivityManager manager = ( ConnectivityManager ) context.getSystemService( Context.CONNECTIVITY_SERVICE );
            NetworkInfo info = manager.getActiveNetworkInfo();
            Log.i( TAG, "ACTIVE NetworkInfo: " + ( info != null ? info.toString() : "NONE" ) );
            Log.i( TAG, "lastNetworkType: " + lastNetworkType );
            Log.i( TAG, "currentNetworkType: " + ( info != null ? info.getType() : "NONE" ) );

            // there are three possible situations here: disconnect, reconnect, connection change
            boolean is_connected = ( null != info ) && info.isConnected();
            boolean was_connected = ( lastNetworkType != -1 );

            if ( was_connected && !is_connected ) {
                Log.d( TAG, "Network status changed - we got NO internet connectivity now..." );
                lastNetworkType = -1;
                serviceIntent.setAction( NETWORK_STATE_DISCONNECTED );
            } else if ( is_connected && ( info.getType() != lastNetworkType ) ) {
                Log.d( TAG, "Network status changed - we got internet connectivity" );
                lastNetworkType = info.getType();
                Log.i( TAG, "currentNetworkType: " + lastNetworkType );
                serviceIntent.setAction( NETWORK_STATE_RECONNECTED );
            } else if ( is_connected && ( info.getType() != lastNetworkType ) ) {
                Log.d( TAG, "Network status changed - we got internet connectivity" );
                Log.i( TAG, "currentNetworkType: " + lastNetworkType );
                serviceIntent.setAction( NETWORK_STATE_CHANGED );
            } else {
                return;
            }
            context.startService( serviceIntent );
        }
    }
}