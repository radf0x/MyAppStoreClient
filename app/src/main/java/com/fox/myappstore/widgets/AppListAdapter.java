package com.fox.myappstore.widgets;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.fox.myappstore.R;
import com.fox.myappstore.data.FreeAppModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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

public class AppListAdapter extends RecyclerView.Adapter< RecyclerView.ViewHolder > implements Filterable {
    private final String TAG = this.getClass().getSimpleName();
    private final int TYPE_ODD = 0;
    private final int TYPE_EVEN = 1;
    private final int TYPE_FOOTER = 2;

    private Object mLock = new Object();
    private List< FreeAppModel > mFreeAppModels = new ArrayList<>();
    private List< FreeAppModel > mOriginalModels;
    private boolean bIsLoading = false;
    private int visibleThreshold = 9;

    public CustomListener mListener;

    public AppListAdapter( RecyclerView recyclerView ) {
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

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder( ViewGroup parent, int viewType ) {
        switch ( viewType ) {
            case TYPE_ODD: {
                return new ItemHolder( LayoutInflater.from( parent.getContext() ).inflate( R.layout.item_app_odd, parent, false ) );
            }
            case TYPE_EVEN: {
                return new ItemHolder( LayoutInflater.from( parent.getContext() ).inflate( R.layout.item_app_even, parent, false ) );
            }
            case TYPE_FOOTER: {
                return new SimpleViewHolder( LayoutInflater.from( parent.getContext() ).inflate( R.layout.item_loading, parent, false ) );
            }
            default: {
                throw new IllegalArgumentException();
            }
        }
    }

    @Override
    public void onBindViewHolder( RecyclerView.ViewHolder holder, int position ) {
        FreeAppModel model = mFreeAppModels.get( position );
        if ( null != model ) {
            if ( holder.getClass().isAssignableFrom( ItemHolder.class ) ) {
                ItemHolder itemHolder = ( ItemHolder ) holder;
                itemHolder.bindData( model );
            }
        }
    }

    // Determine the total amount of items should be loaded.
    @Override
    public int getItemCount() {
        return mFreeAppModels.size();
    }

    @Override
    public int getItemViewType( int position ) {
        if ( isFooterViewPosition( position ) ) {
            return TYPE_FOOTER;
        }
        return position % 2 == 0 ? TYPE_EVEN : TYPE_ODD;
    }

    @Override
    public void onViewRecycled( RecyclerView.ViewHolder holder ) {
        super.onViewRecycled( holder );
        if ( holder.getClass().isAssignableFrom( ItemHolder.class ) ) {
            ItemHolder itemHolder = ( ItemHolder ) holder;
            itemHolder.clearData();
        }
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
    public void setFreeAppModels( List< FreeAppModel > freeAppModels ) {
        mFreeAppModels = freeAppModels;
        notifyDataSetChanged();
    }

    /**
     * Append new data to last index.
     *
     * @param newFreeAppModels inbound data from server.
     */
    public void updateFreeAppModels( List< FreeAppModel > newFreeAppModels ) {
        int oldPos = mFreeAppModels.size();
        mFreeAppModels.addAll( mFreeAppModels.size(), newFreeAppModels );
        notifyItemRangeChanged( oldPos, mFreeAppModels.size() );
    }

    /**
     * Notify load delegate when loading is completed.
     */
    public void onLoadFinished() {
        bIsLoading = false;
    }

    public void appendLoadingView() {
        mFreeAppModels.add( null );
        notifyItemInserted( mFreeAppModels.size() - 1 );
    }

    public void removeLoadingView( Object item ) {
        int index = mFreeAppModels.indexOf( item );
        if ( index != -1 ) {
            mFreeAppModels.remove( index );
            notifyItemRemoved( index );
        }

    }

    private boolean isFooterViewPosition( int position ) {
        return position == mFreeAppModels.size() - 1;
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
            int position = getAdapterPosition();
            if ( position >= 0 && position < mFreeAppModels.size() ) {
                if ( null != mListener ) {
                    Log.d( TAG, "clicked pos : " + position );
                    mListener.onItemClick( mFreeAppModels.get( position ) );
                }
            }
        }
    }

    private class SimpleViewHolder extends RecyclerView.ViewHolder {
        public SimpleViewHolder( View itemView ) {
            super( itemView );
        }
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering( CharSequence prefix ) {
                FilterResults results = new FilterResults();
                if ( mOriginalModels == null ) {
                    synchronized ( mLock ) {
                        mOriginalModels = new ArrayList<>( mFreeAppModels );
                    }
                }
                if ( TextUtils.isEmpty( prefix ) ) {
                    ArrayList< FreeAppModel > list;
                    synchronized ( mLock ) {
                        list = new ArrayList<>( mOriginalModels );
                    }
                    results.values = list;
                    results.count = list.size();
                } else {
                    final ArrayList< FreeAppModel > newValues = new ArrayList<>();
                    for ( FreeAppModel item : mOriginalModels ) {
                        String query = prefix.toString().toLowerCase( Locale.getDefault() );
                        if ( item.getAppNameModel().getName().toLowerCase( Locale.getDefault() ).contains( query ) ) {
                            newValues.add( item );
                        }
                    }
                    results.values = newValues;
                    results.count = newValues.size();
                }
                return results;
            }

            @Override
            protected void publishResults( CharSequence constraint, FilterResults results ) {
                //noinspection unchecked
                mFreeAppModels = ( List< FreeAppModel > ) results.values;
                notifyDataSetChanged();
            }
        };
    }
}
