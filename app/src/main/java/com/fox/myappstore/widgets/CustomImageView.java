package com.fox.myappstore.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.ViewUtils;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.bumptech.glide.DrawableRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.bumptech.glide.signature.StringSignature;
import com.fox.myappstore.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.glide.transformations.CropCircleTransformation;
import jp.wasabeef.glide.transformations.CropSquareTransformation;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

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

public class CustomImageView extends AppCompatImageView {

    enum ScaleType {
        CENTER_CROP,
        FIT_CENTER,
        CROP_CIRCLE,
        CROP_SQUARE,
        FIT_XY,
        CROP_ROUND_SQUARE
    }

    private Drawable placeHolder;
    private Drawable error;
    private int overrideWidth;
    private int overrideHeight;
    private boolean dontAnimate;
    private int priority;
    private boolean bCrossFade;
    private boolean bSkipMemoryCache;    // FALSE by default.
    private int diskCacheStrategy;      // RESULT by default.
    private int scaleType;
    private int roundedCornerRadius;
    private int roundedCornerMargin;
    private int roundedCornerType;

    public CustomImageView( Context context ) {
        super( context );
    }

    public CustomImageView( Context context, AttributeSet attrs ) {
        super( context, attrs );
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.CustomImageView,
                0, 0 );

        try {
            placeHolder = a.getDrawable( R.styleable.CustomImageView_placeholder );
            error = a.getDrawable( R.styleable.CustomImageView_error );
            overrideWidth = ( int ) a.getDimension( R.styleable.CustomImageView_override_width, -1f );
            overrideHeight = ( int ) a.getDimension( R.styleable.CustomImageView_override_height, -1f );
            scaleType = a.getInt( R.styleable.CustomImageView_scaleType, ScaleType.FIT_CENTER.ordinal() );
            bCrossFade = a.getBoolean( R.styleable.CustomImageView_crossFade, false );
            dontAnimate = a.getBoolean( R.styleable.CustomImageView_dontAnimate, false );
            priority = a.getInt( R.styleable.CustomImageView_priority, -1 );
            bSkipMemoryCache = a.getBoolean( R.styleable.CustomImageView_skipMemoryCache, false );
            diskCacheStrategy = a.getInt( R.styleable.CustomImageView_diskCacheStrategy, -1 );
            roundedCornerRadius = ( int ) a.getDimension( R.styleable.CustomImageView_roundedCornerRadius, 0f );
            roundedCornerMargin = ( int ) a.getDimension( R.styleable.CustomImageView_roundedCornerMargin, 0f );
            roundedCornerType = a.getInt( R.styleable.CustomImageView_roundedCornerType, 0 );
        }
        finally {
            a.recycle();
        }

        setScaleType( ScaleType.values()[ scaleType ] );
    }

    public CustomImageView( Context context, AttributeSet attrs, int defStyleAttr ) {
        super( context, attrs, defStyleAttr );
    }

    void loadUrl( String url ) {
        configure( url );
    }

    void loadFile( File file ) {
        configure( file );
    }

    void loadDrawableRes( @DrawableRes int resId ) {
        configure( resId );
    }

    void setScaleType( ScaleType scaleType ) {
        this.scaleType = scaleType.ordinal();

        //update scale type of ImageView.
        ImageView.ScaleType type = ( scaleType == ScaleType.FIT_XY ) ? ImageView.ScaleType.FIT_XY : ImageView.ScaleType.FIT_CENTER;
        if ( type != getScaleType() ) {
            setScaleType( type );
        }
    }

    protected void configure( Object object ) {
        Context context = getContext();
        DrawableRequestBuilder request = Glide.with( context ) //
                .load( object );
        if ( placeHolder != null ) {
            request.placeholder( placeHolder );
        }
        if ( error != null ) {
            request.error( error );
        }
        if ( overrideWidth > 0 && overrideHeight > 0 ) {
            request.override( overrideWidth, overrideHeight );
        }

        if ( bCrossFade ) {
            request.crossFade();
        }
        if ( dontAnimate ) {
            request.dontAnimate();
        }
        if ( priority >= 0 && priority < Priority.values().length ) {
            request.priority( Priority.values()[ priority ] );
        }

        request.skipMemoryCache( bSkipMemoryCache );

        if ( diskCacheStrategy >= 0 && diskCacheStrategy <= DiskCacheStrategy.values().length ) {
            request.diskCacheStrategy( DiskCacheStrategy.values()[ diskCacheStrategy ] );
        }

        List< Transformation< Bitmap > > list = new ArrayList<>();
        if ( scaleType >= 0 && scaleType < ScaleType.values().length ) {
            switch ( ScaleType.values()[ scaleType ] ) {
                case CENTER_CROP: {
                    request.centerCrop();
                    list.add( new CenterCrop( context ) );
                    break;
                }
                case FIT_XY:
                case FIT_CENTER: {
                    request.fitCenter();
                    list.add( new FitCenter( context ) );
                    break;
                }
                case CROP_CIRCLE: {
                    request.centerCrop();
                    list.add( new CropCircleTransformation( context ) );
                    break;
                }
                case CROP_SQUARE: {
                    request.centerCrop();
                    list.add( new CropSquareTransformation( context ) );
                    break;
                }
                case CROP_ROUND_SQUARE: {
                    request.centerCrop();
                    list.add( new RoundedCornersTransformation( context,
                            roundedCornerRadius,
                            roundedCornerMargin,
                            RoundedCornersTransformation.CornerType.values()[ roundedCornerType ] ) );
                    break;
                }
            }
        }
        if ( roundedCornerRadius > 0 ) {
            list.add( new RoundedCornersTransformation(
                    context,
                    roundedCornerRadius,
                    roundedCornerMargin,
                    RoundedCornersTransformation.CornerType.values()[ roundedCornerType ] ) );
        }
        if ( list.size() > 0 ) {
            request.bitmapTransform( list.toArray( new Transformation[ list.size() ] ) );

            StringBuilder sb = new StringBuilder();
            request.signature( new StringSignature( sb.toString() ) );
        }

        request.into( this );
    }

    /**
     * Clear the ImageView
     */
    void clear() {
        Glide.clear( this );
    }
}
