package com.fox.myappstore.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

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

public class NetworkHelper {
    public static boolean isConnected( Context context ) {
        NetworkInfo info = ( ( ConnectivityManager ) context.getSystemService( Context.CONNECTIVITY_SERVICE ) ).getActiveNetworkInfo();
        return ( null != info ) && info.isConnected();
    }

    // TODO: 2/5/2017 should add a boolean method to detect reconnecting state.
}
