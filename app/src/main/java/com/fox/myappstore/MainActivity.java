package com.fox.myappstore;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.RemoteException;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.fox.myappstore.data.FreeAppModel;
import com.fox.myappstore.data.ServerResponse;
import com.fox.myappstore.data.detailModels.DetailResponseModel;
import com.fox.myappstore.request.AppListRequest;
import com.fox.myappstore.request.AppLookupRequest;
import com.fox.myappstore.request.RecommendedAppsRequest;
import com.fox.myappstore.request.core.HttpRequestImpl;
import com.fox.myappstore.utils.GsonHelper;
import com.fox.myappstore.utils.NetworkHelper;
import com.fox.myappstore.utils.concurrent.AsyncLoaderTask;
import com.fox.myappstore.utils.concurrent.MyHandler;
import com.fox.myappstore.utils.concurrent.MyHandlerCallback;
import com.fox.myappstore.widgets.MyBaseAdapter;
import com.fox.myappstore.widgets.RecommendedAdapter;
import com.fox.myappstore.widgets.callbacks.CustomListener;
import com.fox.myappstore.widgets.callbacks.OnTaskCompleted;

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

public class MainActivity extends AppCompatActivity implements
        CustomListener,
        OnTaskCompleted,
        MyHandlerCallback,
        SwipeRefreshLayout.OnRefreshListener {
    private final String TAG = this.getClass().getSimpleName();

    // Event ids.
    private final int EVENT_GET_RECOMMENDED_APPS = 1;
    private final int EVENT_GET_APPS_LIST = 2;
    private final int EVENT_QUERY_APPS_LIST = 3;
    private final int EVENT_QUERY_APP_DETAIL = 4;

    // View objects.
    public View recommendedView;
    public RecyclerView rvBase, rvRecommended;
    public MenuItem menuItem;
    public SwipeRefreshLayout swipeRefreshLayout;
    public CoordinatorLayout coordinatorLayout;
    public Snackbar snackBar;

    // Custom objects.
    private MyHandler mHandler = new MyHandler( this );
    public RecommendedAdapter recommendedAdapter;
    public MyBaseAdapter myBaseAdapter;
    private List< FreeAppModel > appList = new ArrayList<>();
    private List< FreeAppModel > cachedAppList = new ArrayList<>();
    private List< FreeAppModel > recommendedApps = new ArrayList<>();
    private List< FreeAppModel > cachedRecommendedApps = new ArrayList<>();

    // Primitives.
    private int paginationStartPosition = 10;
    private int pageSize = 0;

    // Multi threading stuff.
    private final int KEEP_ALIVE_TIME = 1;
    private final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
    private final int MIN_NUM_OF_CORES = 1;
    private final int MAX_NUM_OF_CORES = Runtime.getRuntime().availableProcessors();
    private final BlockingQueue< Runnable > RUNNABLES = new LinkedBlockingQueue<>();
    private final ThreadPoolExecutor EXECUTOR = new ThreadPoolExecutor( MIN_NUM_OF_CORES, MAX_NUM_OF_CORES, KEEP_ALIVE_TIME, KEEP_ALIVE_TIME_UNIT, RUNNABLES );

    // Network service stuff.
    private INetworkService mService;
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected( ComponentName name, IBinder service ) {
            mService = INetworkService.Stub.asInterface( service );
            try {
                mService.registerNetworkCallback( new INetworkServiceCallback.Stub() {
                    @Override
                    public void onConnectionStatusChanged( String status ) throws RemoteException {
                        if ( NetworkService.NETWORK_STATE_DISCONNECTED.equals( status ) ) {
                            if ( snackBar != null ) snackBar.show();
                        } else if ( NetworkService.NETWORK_STATE_RECONNECTED.equals( status ) ) {
                            if ( snackBar != null ) snackBar.dismiss();
                        }
                    }
                } );
            }
            catch ( RemoteException e ) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected( ComponentName name ) {
            mService = null;
        }
    };

    private void bindNetworkService() {
        Intent intent = new Intent( this, NetworkService.class );
        bindService( intent, mConnection, Context.BIND_AUTO_CREATE );
    }

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );
        bindNetworkService();

        coordinatorLayout = ( CoordinatorLayout ) findViewById( R.id.coordinator_layout );

        swipeRefreshLayout = ( SwipeRefreshLayout ) findViewById( R.id.swipe_refresh );
        swipeRefreshLayout.setOnRefreshListener( this );

        snackBar = Snackbar.make( coordinatorLayout, "No network ", Snackbar.LENGTH_INDEFINITE )
                .setAction( "Retry", new View.OnClickListener() {
                    @Override
                    public void onClick( View v ) {
                        if ( NetworkHelper.isConnected( getApplicationContext() ) ) {
                            snackBar.dismiss();
                            fetchAppListFromServer();
                            fetchRecommendedAppFromFromServer();
                        }
                    }
                } );

        setupAppListView();
        setupRecommendedView();

        if ( NetworkHelper.isConnected( this ) ) {
            fetchAppListFromServer();
            fetchRecommendedAppFromFromServer();
        } else {
            snackBar.show();
            fetchAppListFromCache();
            fetchRecommendedAppsFromCache();
        }
    }

    @Override
    public boolean onCreateOptionsMenu( Menu menu ) {
        getMenuInflater().inflate( R.menu.menu_search, menu );
        setupSearchView( menu );
        return super.onCreateOptionsMenu( menu );
    }

    private void setupAppListView() {
        rvBase = ( RecyclerView ) findViewById( R.id.rv_main );
        rvBase.setLayoutManager( new LinearLayoutManager( this ) );
        rvBase.setHasFixedSize( true );

        myBaseAdapter = new MyBaseAdapter( rvBase );
        myBaseAdapter.addCustomListener( this );
        rvBase.setAdapter( myBaseAdapter );
    }

    private void setupRecommendedView() {
        recommendedView = LayoutInflater.from( this ).inflate( R.layout.recommended_view, rvBase, false );
        rvRecommended = ( RecyclerView ) recommendedView.findViewById( R.id.rv_recommended );
        rvRecommended.setLayoutManager( new LinearLayoutManager( this, LinearLayoutManager.HORIZONTAL, false ) );
        rvRecommended.setHasFixedSize( true );
        recommendedAdapter = new RecommendedAdapter();
        rvRecommended.setAdapter( recommendedAdapter );
        recommendedAdapter.addCustomListener( this );

        myBaseAdapter.setRecommendedView( recommendedView );
        myBaseAdapter.notifyDataSetChanged();
    }

    private void setupSearchView( Menu menu ) {
        menuItem = menu.findItem( R.id.action_search );
        SearchView searchView = ( SearchView ) menuItem.getActionView();
        searchView.setIconifiedByDefault( false );
        searchView.setOnQueryTextListener( new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit( String query ) {
                return false;
            }

            @Override
            public boolean onQueryTextChange( String newText ) {
                if ( !TextUtils.isEmpty( newText ) ) {
                    searchForApps( newText );
                    searchForRecommendedApps( newText );
                }
                return true;
            }
        } );
        searchView.setOnQueryTextFocusChangeListener( new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange( View v, boolean hasFocus ) {
                if ( !hasFocus ) {
                    menuItem.collapseActionView();
                }
            }
        } );
    }

    private void searchForApps( String query ) {
        final List< FreeAppModel > filtered;
        if ( appList.size() > 0 ) {
            filtered = filter( appList, query );
        } else {
            filtered = filter( cachedAppList, query );
        }

        myBaseAdapter.setAppListData( filtered );
        myBaseAdapter.notifyDataSetChanged();
    }

    private List< FreeAppModel > filter( List< FreeAppModel > data, String query ) {
        query = query.toLowerCase();
        final List< FreeAppModel > filtered = new ArrayList<>();
        for ( FreeAppModel model : data ) {
            final String name = model.getAppNameModel().getName().toLowerCase();
            final String category = model.getAppCategoryModel().getAppAttributes().getLabel().toLowerCase();
            final String author = model.getAppArtistModel().getLabel().toLowerCase();
            final String summary = model.getAppSummaryModel().getSummary().toLowerCase();
            if ( name.contains( query ) || category.contains( query ) || author.contains( query ) || summary.contains( query ) ) {
                filtered.add( model );
            }
        }
        return filtered;
    }

    private void fetchAppListFromServer() {
        new AppListRequest( mHandler, EVENT_GET_APPS_LIST ).sendRequest();
//        EXECUTOR.execute( new Runnable() {
//            @Override
//            public void run() {
//                new AppListRequest( mHandler, EVENT_GET_APPS_LIST ).sendRequest();
//            }
//        } );
    }

    private void fetchRecommendedAppFromFromServer() {
        new RecommendedAppsRequest( mHandler, EVENT_GET_RECOMMENDED_APPS ).sendRequest();
//        EXECUTOR.execute( new Runnable() {
//            @Override
//            public void run() {
//                new RecommendedAppsRequest( mHandler, EVENT_GET_RECOMMENDED_APPS ).sendRequest();
//            }
//        } );

    }

    private void fetchAppListFromCache() {
        AsyncLoaderTask task = new AsyncLoaderTask( this, "app.json", AsyncLoaderTask.EVENT_APP_LIST ).addOnTaskCompletedListener( this );
        task.execute();
    }

    private void fetchRecommendedAppsFromCache() {
        AsyncLoaderTask task = new AsyncLoaderTask( this, "recommend.json", AsyncLoaderTask.EVENT_RECOMMEND ).addOnTaskCompletedListener( this );
        task.execute();
    }

    private void queryAppDetail( List< FreeAppModel > models ) {
        for ( FreeAppModel model : models ) {
            int id = model.getAppIdModel().getAppAttributes().getId();
            new AppLookupRequest( mHandler, EVENT_QUERY_APP_DETAIL, id ).sendRequest();
        }
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
        unbindService( mConnection );
        appList.clear();
        recommendedApps.clear();
        cachedAppList.clear();
        cachedRecommendedApps.clear();
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

    private void searchForRecommendedApps( String query ) {
        recommendedAdapter.getFilter().filter( query );
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onRefresh() {
        if ( NetworkHelper.isConnected( this ) ) {
            myBaseAdapter.resetLoader();
            pageSize = 0;
            paginationStartPosition = 10;
            // query new data set from server.
            fetchAppListFromServer();
            fetchRecommendedAppFromFromServer();
        } else {
            snackBar.show();
        }
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
                    cachedAppList = ( ( ServerResponse ) output ).getFeed().getFreeAppModels();
                    myBaseAdapter.setAppListData( cachedAppList.subList( 0, 9 ) ); // hardcoded range.
                    myBaseAdapter.notifyDataSetChanged();
                }
                break;
            }
            case AsyncLoaderTask.EVENT_RECOMMEND: {
                if ( output.getClass().isAssignableFrom( ServerResponse.class ) ) {
                    cachedRecommendedApps = ( ( ServerResponse ) output ).getFeed().getFreeAppModels();
                    recommendedAdapter.setRecommendedData( cachedRecommendedApps );
                    recommendedAdapter.notifyDataSetChanged();
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
        Log.d( TAG, "onLoadMore(" + pageSize + ")" );
        if ( pageSize == 9 ) {
            Log.d( TAG, "page size is 9" );
            return;
        } else {
            if ( NetworkHelper.isConnected( this ) ) {
                myBaseAdapter.setLoading( true );
                mHandler.sendEmptyMessage( EVENT_QUERY_APPS_LIST );
                ++pageSize;
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
                int paginationEndPosition = paginationStartPosition + 10;
                List< FreeAppModel > models;
                if ( appList.size() > 0 ) {
                    Log.i( TAG, "using appList" );
                    models = appList.subList( paginationStartPosition, paginationEndPosition );
                } else {
                    Log.i( TAG, "using cache" );
                    models = cachedAppList.subList( paginationStartPosition, paginationEndPosition );
                }
                myBaseAdapter.updateAppListData( models );
                myBaseAdapter.setLoading( false );
                myBaseAdapter.notifyDataSetChanged();
                paginationStartPosition = paginationEndPosition;
                myBaseAdapter.onLoadFinished();
                break;
            }

            case EVENT_QUERY_APP_DETAIL: {
                Bundle bundle = msg.getData();
                if ( bundle.getBoolean( HttpRequestImpl.IS_SUCCESSFUL ) ) {
                    setupRatingModules( bundle );
                    break;
                }
                break;
            }
            default:
                break;
        }
    }

    private void setupRatingModules( Bundle bundle ) {
        String responseStr = bundle.getString( HttpRequestImpl.HTTP_RESPONSE_BODY );
        DetailResponseModel response = GsonHelper.fromJson( responseStr, DetailResponseModel.class );
        if ( null != response ) {
            float rating = response.getModels().get( 0 ).getAverageUserRating();
            int id = response.getModels().get( 0 ).getAppId();
//            Log.i( TAG, "id : " + id + "-----rating : " + rating );
            for ( int i = 0; i < appList.size(); i++ ) {
                if ( appList.get( i ).getAppIdModel().getAppAttributes().getId() == id ) {
                    appList.get( i ).setUserRating( rating );
                }
            }
        }
        updateAppListAdapter( appList );
    }

    private void setupRecommendedFeed( Bundle bundle ) {
        String responseStr = bundle.getString( HttpRequestImpl.HTTP_RESPONSE_BODY );
        ServerResponse response = GsonHelper.fromJson( responseStr, ServerResponse.class );
        if ( null != response ) {
            recommendedApps.clear();
            ServerResponse.FeedModel feed = response.getFeed();
            for ( FreeAppModel model : feed.getFreeAppModels() ) {
                recommendedApps.add( model );
            }
            updateRecommendedAdapter( recommendedApps );
        }
    }

    private void setupAppListFeed( Bundle bundle ) {
        String responseStr = bundle.getString( HttpRequestImpl.HTTP_RESPONSE_BODY );
        ServerResponse response = GsonHelper.fromJson( responseStr, ServerResponse.class );
        if ( null != response ) {
            appList.clear();
            ServerResponse.FeedModel feed = response.getFeed();
            for ( FreeAppModel model : feed.getFreeAppModels() ) {
                appList.add( model );
            }
            queryAppDetail( appList );
        }
        notifyRefreshLayout();
    }

    private void updateRecommendedAdapter( List< FreeAppModel > newModels ) {
        recommendedAdapter.setRecommendedData( newModels );
        recommendedAdapter.notifyDataSetChanged();
    }

    private void updateAppListAdapter( List< FreeAppModel > newModels ) {
        myBaseAdapter.setAppListData( newModels.subList( 0, 9 ) );
        myBaseAdapter.notifyDataSetChanged();
    }

    private void notifyRefreshLayout() {
        if ( swipeRefreshLayout.isRefreshing() ) {
            swipeRefreshLayout.setRefreshing( false );
        }
    }
}
