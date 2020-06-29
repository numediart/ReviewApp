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

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

public class ZoomPointerImageView extends PointerImageView {

    private ScaleGestureDetector scaleDetector;
    private float scaleFactor, xTranslateFactor, yTranslateFactor, xAnchor, yAnchor;
    private float oldXCursor, oldYCursor;
    private boolean restoreCursor, oldShowCursor;

    public ZoomPointerImageView(Context context) {
        this(context, null);
    }

    public ZoomPointerImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ZoomPointerImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        scaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        scaleFactor = 1.0f;
    }

    public void reset() {
        super.reset();
        scaleFactor = 1.0f;
        xTranslateFactor = 0.0f;
        yTranslateFactor = 0.0f;
        invalidate();
    }

    @Override
    public float getCursorRadius() {
        return super.getCursorRadius()/scaleFactor;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(! isTouchable()) return true;

        super.onTouchEvent(event);
        scaleDetector.onTouchEvent(event);

        if(event.getAction() == MotionEvent.ACTION_DOWN) {
            xAnchor = event.getX();
            yAnchor = event.getY();
            oldXCursor = getXCursor();
            oldYCursor = getYCursor();
            oldShowCursor = isShowCursor();
        }
        else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            float dX = event.getX() - xAnchor;
            float dY = event.getY() - yAnchor;
            float threshold = 10;

            if(Math.abs(dX) >= threshold || Math.abs(dY) >= threshold) {
                float speed = 0.5f;

                xTranslateFactor += (event.getX() - xAnchor) * speed;
                yTranslateFactor += (event.getY() - yAnchor) * speed;

                xTranslateFactor = Math.max(-getWidth() / 2f * (scaleFactor - 1), Math.min(xTranslateFactor, getWidth() / 2f * (scaleFactor - 1)));
                yTranslateFactor = Math.max(-getHeight() / 2f * (scaleFactor - 1), Math.min(yTranslateFactor, getHeight() / 2f * (scaleFactor - 1)));

                restoreCursor = true;

                invalidate();
            }
        }
        else if (event.getAction() == MotionEvent.ACTION_UP && restoreCursor) {
            setCursor(oldXCursor, oldYCursor);
            setShowCursor(oldShowCursor);
            restoreCursor = false;
        }
        return true;
    }

    @Override
    public void onDraw(Canvas canvas) {
        setX(xTranslateFactor);
        setY(yTranslateFactor);

        setScaleX(scaleFactor);
        setScaleY(scaleFactor);

        super.onDraw(canvas);
    }



    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scaleFactor *= detector.getScaleFactor();

            // Don't let the object get too small or too large.
            scaleFactor = Math.max(0.8f, Math.min(scaleFactor, 5.0f));

            invalidate();
            return true;
        }
    }

}
