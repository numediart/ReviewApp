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
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;

/* Extends ImageView with the ability to click on the image and display a cursor */
public class PointerImageView extends androidx.appcompat.widget.AppCompatImageView {

    private float xCursor, yCursor, radius;
    private boolean showCursor;
    private Paint paint;
    private int color;
    private boolean touchable;

    public PointerImageView(Context context) {
        this(context, null);
    }

    public PointerImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PointerImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        showCursor = false;
        color = 0xFFFFFFFF;
        radius = TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP, 15,
                context.getResources().getDisplayMetrics() );
        paint = new Paint();
        touchable = true;
    }

    public void setCursorRadius(float radius) {
        this.radius = radius;
    }

    public float getCursorRadius() {
        return radius;
    }

    public void setCursorColor(int color) {
        this.color = color;
    }

    public void reset() {
        xCursor = 0f;
        yCursor = 0f;
        showCursor = false;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(showCursor) {
            paint.setColor(0xFF000000);
            canvas.drawCircle(xCursor, yCursor, getCursorRadius()*1.20f, paint);
            paint.setColor(color);
            canvas.drawCircle(xCursor, yCursor, getCursorRadius(), paint);
        }
    }

    /* Set the cursor x-y position with respect to all the imageview size */
    public void setCursor(float xCursor, float yCursor) {
        this.xCursor = xCursor;
        this.yCursor = yCursor;
    }

    /* Get the cursor x position with respect to all the imageview size */
    public float getXCursor() {
        return xCursor;
    }

    /* Get the cursor y position with respect to all the imageview size */
    public float getYCursor() {
        return yCursor;
    }

    /* Set the cursor x-y position in percent with respect to the source image size
    * xPercent : should be between 0 and 1 included.
    * yPercent : should be between 0 and 1 included.
    * */
    public void setNormalizedCursor(float xPercent, float yPercent) {
        float[] matrix = new float[9];
        getImageMatrix().getValues(matrix);

        float xPos = (xPercent * getDrawable().getIntrinsicWidth() * matrix[Matrix.MSCALE_X]) + matrix[Matrix.MTRANS_X];
        float yPos = (yPercent * getDrawable().getIntrinsicHeight() * matrix[Matrix.MSCALE_Y]) + matrix[Matrix.MTRANS_Y];

        setCursor(xPos, yPos);
    }

    /* Get the cursor x position in percent with respect to the source image size
     * return value is between 0 and 1 included.
     * */
    public float getXNormalizedCursor() {
        float[] matrix = new float[9];
        getImageMatrix().getValues(matrix);

        return (getXCursor() -  matrix[Matrix.MTRANS_X]) / matrix[Matrix.MSCALE_X] / getDrawable().getIntrinsicWidth();
    }

    /* Get the cursor x position in percent with respect to the source image size
     * return value is between 0 and 1 included.
     * */
    public float getYNormalizedCursor() {
        float[] matrix = new float[9];
        getImageMatrix().getValues(matrix);

        return (getYCursor() -  matrix[Matrix.MTRANS_Y]) / matrix[Matrix.MSCALE_Y] / getDrawable().getIntrinsicHeight();
    }

    public boolean isShowCursor() {
        return showCursor;
    }

    public void setShowCursor(boolean showCursor) {
        this.showCursor = showCursor;
    }

    public boolean isTouchable() {
        return touchable;
    }

    public void setTouchable(boolean touchable) {
        this.touchable = touchable;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(! isTouchable()) return true;

        boolean retValue = super.onTouchEvent(event);

        if (event.getAction() == MotionEvent.ACTION_UP){
            if(!insideImageLimits(event.getX(), event.getY())) return true;
            showCursor = true;
            setCursor(event.getX(), event.getY());

            invalidate();

            return true;
        }

        if (event.getAction() == MotionEvent.ACTION_DOWN){
            //Notify caller that we want to be notified for simple actions
            return true;
        }
        return retValue;
    }

    private boolean insideImageLimits(float x, float y) {
        float[] matrix = new float[9];
        getImageMatrix().getValues(matrix);

        float xPercent = (x -  matrix[Matrix.MTRANS_X]) / matrix[Matrix.MSCALE_X] / getDrawable().getIntrinsicWidth();
        float yPercent = (y -  matrix[Matrix.MTRANS_Y]) / matrix[Matrix.MSCALE_Y] / getDrawable().getIntrinsicHeight();

        return 0 <= xPercent && xPercent <= 1 && 0 <= yPercent && yPercent <= 1;
    }
}
