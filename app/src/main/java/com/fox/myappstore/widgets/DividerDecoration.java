package com.fox.myappstore.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;

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

public class DividerDecoration extends RecyclerView.ItemDecoration {
    private static final int[] ATTRS = new int[] { android.R.attr.listDivider };

    private Drawable divider;

    /**
     * Default divider will be used
     */
    public DividerDecoration( Context context ) {
        final TypedArray styledAttributes = context.obtainStyledAttributes( ATTRS );
        divider = styledAttributes.getDrawable( 0 );
        styledAttributes.recycle();
    }

    /**
     * Custom divider will be used
     */
    public DividerDecoration( Context context, int resId ) {
        divider = ContextCompat.getDrawable( context, resId );
    }

    @Override
    public void onDraw( Canvas c, RecyclerView parent, RecyclerView.State state ) {
        int left = parent.getPaddingLeft();
        int right = parent.getWidth() - parent.getPaddingRight();

        int childCount = parent.getChildCount();
        for ( int i = 0; i < childCount; i++ ) {
            View child = parent.getChildAt( i );

            RecyclerView.LayoutParams params = ( RecyclerView.LayoutParams ) child.getLayoutParams();

            int top = child.getBottom() + params.bottomMargin;
            int bottom = top + divider.getIntrinsicHeight();

            divider.setBounds( left, top, right, bottom );
            divider.draw( c );
        }
    }
}
