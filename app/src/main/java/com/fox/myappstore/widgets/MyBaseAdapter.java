package com.fox.myappstore.widgets;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fox.myappstore.R;
import com.fox.myappstore.data.FreeAppModel;
import com.fox.myappstore.widgets.callbacks.CustomListener;
import com.fox.myappstore.widgets.viewholders.LoadingViewHolder;

import java.util.ArrayList;
import java.util.List;

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

public class MyBaseAdapter extends RecyclerView.Adapter< RecyclerView.ViewHolder > {

    private final String TAG = this.getClass().getSimpleName();

    // View type ids.
    private final int TYPE_RECOMMENDED = 0;
    private final int TYPE_APP_LIST_ITEM_ODD = 1;
    private final int TYPE_APP_LIST_ITEM_EVEN = 2;
    private final int TYPE_APP_LIST_LOADING = 3;

    // Custom objects.
    private List< FreeAppModel > mFreeAppModels = new ArrayList<>();

    // Primitives.
    private boolean bIsLoading = false;
    private boolean bIsFooterVisible = false;
    private int visibleThreshold = 5;
    public CustomListener mListener;

    // View
    private View recommendedView;

    public MyBaseAdapter( RecyclerView recyclerView ) {
        final LinearLayoutManager layoutManager = ( LinearLayoutManager ) recyclerView.getLayoutManager();
        recyclerView.addOnScrollListener( new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled( RecyclerView recyclerView, int dx, int dy ) {
                super.onScrolled( recyclerView, dx, dy );
                if ( dy > 0 ) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
                    if ( !bIsLoading && ( totalItemCount - visibleItemCount ) <= ( firstVisibleItemPosition + visibleThreshold ) ) {
                        if ( null != mListener ) {
                            mListener.onLoadMore();
                        }
                        bIsLoading = true;
                    }
                }
            }
        } );
    }

    /**
     * Register event for item click
     *
     * @param mListener event delegate.
     */
    public void addCustomListener( CustomListener mListener ) {
        this.mListener = mListener;
    }

    /**
     * Set the data object for displaying on the recycler view.
     *
     * @param freeAppModels FreeAppModel
     */
    public void setAppListData( List< FreeAppModel > freeAppModels ) {
        mFreeAppModels.clear();
        mFreeAppModels.addAll( freeAppModels );
    }

    public void updateAppListData( List< FreeAppModel > freeAppModels ) {
        mFreeAppModels.addAll( freeAppModels );
    }

    /**
     * Notify load delegate when loading is completed.
     */
    public void onLoadFinished() {
        bIsLoading = false;
    }

    public void setLoading( boolean status ) {
        bIsFooterVisible = status;
    }

    public void setRecommendedView( View recommendedView ) {
        this.recommendedView = recommendedView;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder( ViewGroup parent, int viewType ) {
        switch ( viewType ) {
            case TYPE_RECOMMENDED: {
                return new SimpleHolder( recommendedView );
            }
            case TYPE_APP_LIST_ITEM_ODD: {
                return new ItemHolder( LayoutInflater.from( parent.getContext() ).inflate( R.layout.item_app_odd, parent, false ) );
            }
            case TYPE_APP_LIST_ITEM_EVEN: {
                return new ItemHolder( LayoutInflater.from( parent.getContext() ).inflate( R.layout.item_app_even, parent, false ) );
            }
            case TYPE_APP_LIST_LOADING: {
                return new LoadingViewHolder( LayoutInflater.from( parent.getContext() ).inflate( R.layout.item_loading, parent, false ) );
            }
            default: {
                break;
            }
        }
        throw new IllegalArgumentException();
    }

    @Override
    public void onBindViewHolder( RecyclerView.ViewHolder holder, int position ) {
        if ( holder instanceof LoadingViewHolder ) {
            LoadingViewHolder viewHolder = ( LoadingViewHolder ) holder;
            viewHolder.pb.setVisibility( bIsFooterVisible ? View.VISIBLE : View.GONE );
        }
        if ( holder instanceof ItemHolder ) {
            ItemHolder itemHolder = ( ItemHolder ) holder;
            FreeAppModel model = mFreeAppModels.get( position - 1 );
            if ( null != model ) {
                itemHolder.bindData( model );
            }
        }
    }

    @Override
    public int getItemViewType( int position ) {
        if ( position == 0 ) {
            return TYPE_RECOMMENDED;
        }

        if ( position == getItemCount() - 1 ) {
            return TYPE_APP_LIST_LOADING;
        }

        return position % 2 == 0 ? TYPE_APP_LIST_ITEM_EVEN : TYPE_APP_LIST_ITEM_ODD;
    }

    @Override
    public int getItemCount() {
        if ( mFreeAppModels == null || mFreeAppModels.size() == 0 ) {
            return 1;
        }
        // +2 for header and footer.
        return mFreeAppModels.size() + 2;
    }

    @Override
    public void onViewRecycled( RecyclerView.ViewHolder holder ) {
        super.onViewRecycled( holder );
        if ( holder.getClass().isAssignableFrom( ItemHolder.class ) ) {
            ItemHolder itemHolder = ( ItemHolder ) holder;
            itemHolder.clearData();
        }
    }

    private class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        CustomImageView ivIcon;
        TextView tvTitle;
        TextView tvSubtitle;

        /**
         * Bind reference to view objects.
         *
         * @param itemView
         */
        public ItemHolder( View itemView ) {
            super( itemView );
            ivIcon = ( CustomImageView ) itemView.findViewById( R.id.iv_icon );
            tvTitle = ( TextView ) itemView.findViewById( R.id.tv_app_title );
            tvSubtitle = ( TextView ) itemView.findViewById( R.id.tv_app_subtitle );
            itemView.setOnClickListener( this );
        }

        /**
         * Wrapper function for setting values for views.
         *
         * @param model FreeAppModel
         */
        public void bindData( FreeAppModel model ) {
            ivIcon.loadUrl( model.getAppImageModel().get( 0 ).getIconUrl() );
            tvTitle.setText( model.getAppNameModel().getName() );
            tvSubtitle.setText( model.getAppCategoryModel().getAppAttributes().getLabel() );
        }

        public void clearData() {
            ivIcon.clear();
        }

        @Override
        public void onClick( View v ) {
            int position = getAdapterPosition() - 1;
            if ( position >= 0 && position < mFreeAppModels.size() ) {
                if ( null != mListener ) {
                    mListener.onItemClick( mFreeAppModels.get( position ) );
                    Log.d( TAG, "===== BEGIN DEBUG =====" );
                    Log.i( TAG, "clicked pos : " + position + " app name : " + mFreeAppModels.get( position ).getAppNameModel().getName() + " avg rating : " + mFreeAppModels.get( position ).getUserRating() );
                    Log.d( TAG, "=======================" );
                }
            }
        }
    }

    private class SimpleHolder extends RecyclerView.ViewHolder {
        public SimpleHolder( View itemView ) {
            super( itemView );
        }
    }

}
