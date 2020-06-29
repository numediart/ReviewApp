// Copyright (C) 2020 - UMons
// 
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
// 
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
// 
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <https://www.gnu.org/licenses/>.

package com.numediart.reviewapp.graphics;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;

import com.numediart.reviewapp.R;

/* Extends PointerImageView with changing color cursor based on bitmap image */
public class ColorPointerImageView extends PointerImageView {
        private Bitmap colorMap;

        public ColorPointerImageView(Context context) {
            this(context, null);
        }

        public ColorPointerImageView(Context context, AttributeSet attrs) {
            this(context, attrs, 0);
        }

        public ColorPointerImageView(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);

            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ColorPointerImageView, defStyle, 0);

            BitmapDrawable bitmap_colors = (BitmapDrawable) a.getDrawable(R.styleable.ColorPointerImageView_bitmap_colors);
            assert bitmap_colors != null;

            colorMap = bitmap_colors.getBitmap();

            a.recycle();
        }

        @Override
        public void setCursor(float xCursor, float yCursor) {
            super.setCursor(xCursor, yCursor);
            setCursorColor(getPercentColor(getXNormalizedCursor(), getYNormalizedCursor()));
        }

        /* Return the color for the corresponding pixel in the colorMap
         * xPercent : should be between 0 and 1 included
         * yPercent : should be between 0 and 1 included
         */
        private int getPercentColor(float xPercent, float yPercent) {
            int xColorMap = (int) (xPercent * colorMap.getWidth());
            int yColorMap = (int) (yPercent * colorMap.getHeight());

            return colorMap.getPixel(xColorMap, yColorMap);
        }
}
