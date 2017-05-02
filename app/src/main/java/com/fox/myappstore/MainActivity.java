package com.fox.myappstore;

import android.os.Bundle;
import android.os.Message;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.fox.myappstore.data.FreeAppModel;
import com.fox.myappstore.data.ServerResponse;
import com.fox.myappstore.request.AppListRequest;
import com.fox.myappstore.request.RecommendedAppsRequest;
import com.fox.myappstore.request.core.HttpRequestImpl;
import com.fox.myappstore.utils.GsonHelper;
import com.fox.myappstore.utils.NetworkHelper;
import com.fox.myappstore.utils.concurrent.AsyncLoaderTask;
import com.fox.myappstore.utils.concurrent.MyHandler;
import com.fox.myappstore.utils.concurrent.MyHandlerCallback;
import com.fox.myappstore.widgets.AppListAdapter;
import com.fox.myappstore.widgets.CustomListener;
import com.fox.myappstore.widgets.DividerDecoration;
import com.fox.myappstore.widgets.OnTaskCompleted;
import com.fox.myappstore.widgets.RecommendedAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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

public class MainActivity extends AppCompatActivity implements CustomListener, OnTaskCompleted, MyHandlerCallback {
    private final String TAG = this.getClass().getSimpleName();

    // Event ids.
    private final int EVENT_GET_RECOMMENDED_APPS = 1;
    private final int EVENT_GET_APPS_LIST = 2;
    private final int EVENT_QUERY_APPS_LIST = 3;
    private final int EVENT_APPEND_LOADING_VIEW = 4;
    private final int EVENT_REMOVE_LOADING_VIEW = 5;

    // View objects.
    public RecyclerView rvRecommended, rvFreeApps;
    public LinearLayoutManager horizontalManager, verticalManager;

    // Custom objects.
    private MyHandler mHandler = new MyHandler( this );
    public RecommendedAdapter recommendedAdapter;
    public AppListAdapter appListAdapter;
    private List< FreeAppModel > mFreeAppModels = new ArrayList<>();
    private List< FreeAppModel > mRecommendModels = new ArrayList<>();

    // Primitives.
    private int paginationStartPosition = 10;
    private boolean bEndOfFeed = false;

    //Multi threading stuff.
    private final int KEEP_ALIVE_TIME = 1;
    private final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
    private final int MIN_NUM_OF_CORES = 1;
    private final int MAX_NUM_OF_CORES = Runtime.getRuntime().availableProcessors();
    private final BlockingQueue< Runnable > RUNNABLES = new LinkedBlockingQueue<>();
    private final ThreadPoolExecutor EXECUTOR = new ThreadPoolExecutor( MIN_NUM_OF_CORES, MAX_NUM_OF_CORES, KEEP_ALIVE_TIME, KEEP_ALIVE_TIME_UNIT, RUNNABLES );

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );
        rvRecommended = ( RecyclerView ) findViewById( R.id.rv_recommended );
        rvFreeApps = ( RecyclerView ) findViewById( R.id.rv_free_apps );
        setupRecyclerViews();
        setupAdapters();
        fetchAppListFromServer();
        fetchRecommendedAppFromFromServer();
        fetchAppListFromAsset();
        fetchRecommendedAppsFromAsset();
    }

    private void fetchAppListFromServer() {
        EXECUTOR.execute( new Runnable() {
            @Override
            public void run() {
                new AppListRequest( mHandler, EVENT_GET_APPS_LIST ).sendRequest();
            }
        } );
    }

    private void fetchRecommendedAppFromFromServer() {
        EXECUTOR.execute( new Runnable() {
            @Override
            public void run() {
                new RecommendedAppsRequest( mHandler, EVENT_GET_RECOMMENDED_APPS ).sendRequest();
            }
        } );

    }

    private void fetchAppListFromAsset() {
        AsyncLoaderTask task = new AsyncLoaderTask( this, "app.json", AsyncLoaderTask.EVENT_APP_LIST ).addOnTaskCompletedListener( this );
        task.execute();
    }

    private void fetchRecommendedAppsFromAsset() {
        AsyncLoaderTask task = new AsyncLoaderTask( this, "recommend.json", AsyncLoaderTask.EVENT_RECOMMEND ).addOnTaskCompletedListener( this );
        task.execute();
    }

    private void setupRecyclerViews() {
        horizontalManager = new LinearLayoutManager( this, LinearLayoutManager.HORIZONTAL, false );
        verticalManager = new LinearLayoutManager( this, LinearLayoutManager.VERTICAL, false );
        rvRecommended.setLayoutManager( horizontalManager );
        rvFreeApps.setLayoutManager( verticalManager );
        rvFreeApps.addItemDecoration( new DividerDecoration( this, R.drawable.divider ) );
    }

    private void setupAdapters() {
        recommendedAdapter = new RecommendedAdapter();
        appListAdapter = new AppListAdapter( rvFreeApps );
        recommendedAdapter.addCustomListener( this );
        appListAdapter.addCustomListener( this );
        rvRecommended.setAdapter( recommendedAdapter );
        rvFreeApps.setAdapter( appListAdapter );
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mFreeAppModels.clear();
        mRecommendModels.clear();
        super.onDestroy();
    }

    @Override
    public void onRestoreInstanceState( Bundle savedInstanceState, PersistableBundle persistentState ) {
        super.onRestoreInstanceState( savedInstanceState, persistentState );
    }

    @Override
    public void onSaveInstanceState( Bundle outState, PersistableBundle outPersistentState ) {
        super.onSaveInstanceState( outState, outPersistentState );
    }

    /**
     * Custom Callbacks
     */

    @Override
    public void onTaskCompleted( int eventId, Object output ) {
        Log.d( TAG, "onTaskCompleted" );
        switch ( eventId ) {
            case AsyncLoaderTask.EVENT_APP_LIST: {
                if ( output.getClass().isAssignableFrom( ServerResponse.class ) ) {
                    mFreeAppModels = ( ( ServerResponse ) output ).getFeed().getFreeAppModels();
                    appListAdapter.setFreeAppModels( mFreeAppModels );
                }
                break;
            }
            case AsyncLoaderTask.EVENT_RECOMMEND: {
                if ( output.getClass().isAssignableFrom( ServerResponse.class ) ) {
                    mRecommendModels = ( ( ServerResponse ) output ).getFeed().getFreeAppModels();
                    recommendedAdapter.setFreeAppModels( mRecommendModels );
                }
                break;
            }
            default:
                break;
        }
    }

    @Override
    public void onItemClick( Object model ) {
    }

    @Override
    public void onLoadMore() {
        if ( !bEndOfFeed ) {
            mHandler.sendEmptyMessage( EVENT_APPEND_LOADING_VIEW );
            if ( NetworkHelper.isConnected( this ) ) {
                mHandler.sendEmptyMessage( EVENT_QUERY_APPS_LIST );
            }
        }
    }

    @Override
    public void handleMessage( Message msg ) {
        switch ( msg.what ) {
            case EVENT_GET_RECOMMENDED_APPS: {
                Bundle bundle = msg.getData();
                if ( bundle.getBoolean( HttpRequestImpl.IS_SUCCESSFUL ) ) {
                    setupRecommendedFeed( bundle );
                }
                break;
            }
            case EVENT_GET_APPS_LIST: {
                Bundle bundle = msg.getData();
                if ( bundle.getBoolean( HttpRequestImpl.IS_SUCCESSFUL ) ) {
                    setupAppListFeed( bundle );
                }
                break;
            }
            case EVENT_QUERY_APPS_LIST: {
                if ( paginationStartPosition == 100 ) {
                    bEndOfFeed = true;
                }
                int paginationEndPosition = paginationStartPosition + 10;
                appListAdapter.updateFreeAppModels( mFreeAppModels.subList( paginationStartPosition, paginationEndPosition ) );
                paginationStartPosition = paginationEndPosition;
                mHandler.sendEmptyMessage( EVENT_REMOVE_LOADING_VIEW );
                appListAdapter.onLoadFinished();
                break;
            }
            case EVENT_APPEND_LOADING_VIEW: {
                appListAdapter.appendLoadingView();
                break;
            }
            case EVENT_REMOVE_LOADING_VIEW: {
                appListAdapter.removeLoadingView( null );
                break;
            }
            default:
                break;
        }
    }

    private void setupRecommendedFeed( Bundle bundle ) {
        String responseStr = bundle.getString( HttpRequestImpl.HTTP_RESPONSE_BODY );
        ServerResponse response = GsonHelper.fromJson( responseStr, ServerResponse.class );
        if ( null != response ) {
            ServerResponse.FeedModel feed = response.getFeed();
            Log.i( TAG, "inbound feed for recommended apps: " );
            for ( FreeAppModel model : feed.getFreeAppModels() ) {
                Log.i( TAG, "app name : " + model.getAppNameModel().getName() );
            }
        }
    }

    private void setupAppListFeed( Bundle bundle ) {
        String responseStr = bundle.getString( HttpRequestImpl.HTTP_RESPONSE_BODY );
        ServerResponse response = GsonHelper.fromJson( responseStr, ServerResponse.class );
        if ( null != response ) {
            ServerResponse.FeedModel feed = response.getFeed();
            Log.i( TAG, "inbound feed for apps list: " );
            for ( FreeAppModel model : feed.getFreeAppModels() ) {
                Log.i( TAG, "app name : " + model.getAppNameModel().getName() );
            }
        }
    }
}
