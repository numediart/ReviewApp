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

package com.numediart.reviewapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import com.numediart.reviewapp.database.FloorsDatabase;
import com.numediart.reviewapp.database.ReviewsDatabase;
import com.numediart.reviewapp.models.Emotion;
import com.numediart.reviewapp.models.Floor;
import com.numediart.reviewapp.models.Location;
import com.numediart.reviewapp.graphics.PointerImageView;
import com.numediart.reviewapp.models.Review;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class ReviewActivity extends AppCompatActivity {

    public final static String INTENT_TAG = "REVIEW_ID";
    private AsyncFloorTask asyncFloor;
    private Utils.AsyncImageTask asyncPicture;

    //Review should remain an instance variable to avoid releasing memory while loading picture and floor
    private Review review;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        long id = getIntent().getLongExtra(INTENT_TAG, -1);
        review = ReviewsDatabase.getReview(this, id);

        if(review == null) {
            finish();
            return;
        }

        String title = review.getTitle();
        if(title != null && ! title.isEmpty() ) ((TextView) findViewById(R.id.titleView)).setText(title);

        String author = review.getAuthor();
        if(author != null && ! author.isEmpty()) ((TextView) findViewById(R.id.authorView)).setText(author);

        SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm", Locale.getDefault());
        String date = review.getDate() != null ? sdf.format(review.getDate()) : "";
        if(! date.isEmpty()) ((TextView) findViewById(R.id.dateView)).setText(date);

        ImageView picture = findViewById(R.id.picture);
        asyncPicture = new Utils.AsyncImageTask(picture, review.getPicture(), this);
        asyncPicture.execute();

        PointerImageView location = findViewById(R.id.location);
        location.setTouchable(false);

        TextView floorNameView = findViewById(R.id.floorNameView);
        TextView floorIdView = findViewById(R.id.floorIdView);

        asyncFloor = new AsyncFloorTask(location, floorNameView, floorIdView, review, this);
        asyncFloor.execute();

        PointerImageView view = findViewById(R.id.emotion);
        view.setTouchable(false);
        setEmotionView(view, review);

        ((TextView) findViewById(R.id.contentView)).setText(review.getContent());

        ((TextView) findViewById(R.id.idView)).setText(String.format(Locale.getDefault(), "ID: %1$d", review.getId()));

        String key = review.getSessionKey();
        String sessionText = (key != null && ! key.isEmpty()) ? String.format(getString(R.string.session_key) +": %1$s", key) : getString(R.string.no_session_key);
        ((TextView) findViewById(R.id.sessionKeyView)).setText(sessionText);

        TextView completeView = findViewById(R.id.completedView);
        String completed = review.isComplete() ? getString(R.string.complete) : getString(R.string.incomplete);
        if(review.isComplete()) {
            completeView.setTextColor(getResources().getColor(R.color.success));
        } else {
            completeView.setTextColor(getResources().getColor(R.color.error));
        }
        ((TextView) findViewById(R.id.completedView)).setText(completed);

        TextView sentView = findViewById(R.id.sentView);
        if(review.getSent() == 1) {
            sentView.setTextColor(getResources().getColor(R.color.success));
            sentView.setText(getString(R.string.sent));
        } else if(review.getSent() == -1) {
            sentView.setTextColor(getResources().getColor(R.color.error));
            sentView.setText(getString(R.string.error_sent));
        } else {
            sentView.setText(getString(R.string.not_sent));
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        asyncPicture.cancel(true);
        asyncFloor.cancel(true);
    }

    private void setEmotionView(final PointerImageView view, Review review) {
        final Emotion emotion = review.getEmotion();

        if(emotion == null) {
            view.setScaleType(ImageView.ScaleType.CENTER);
            view.setImageResource(R.drawable.ic_sentiment_very_dissatisfied_black_24dp);
        } else {
            ViewTreeObserver vto = view.getViewTreeObserver();
            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                public void onGlobalLayout() {
                    view.setNormalizedCursor((emotion.getIntensity() + 1) / 2,
                            (emotion.getValence() - 1) / -2);
                    view.setShowCursor(true);
                    view.setCursorRadius(10);

                    view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            });
        }
    }

    private static class AsyncFloorTask extends AsyncTask<Void, Void, Bitmap> {

        private final WeakReference<Review> review;
        private final WeakReference<Context> context;
        private final WeakReference<PointerImageView> view;
        private final WeakReference<TextView> nameView;
        private final WeakReference<TextView> idView;

        private String name;

        private AsyncFloorTask(PointerImageView view, TextView nameView, TextView idView, Review review, Context context) {
            this.review = new WeakReference<>(review);
            this.context = new WeakReference<>(context);
            this.view = new WeakReference<>(view);
            this.nameView = new WeakReference<>(nameView);
            this.idView = new WeakReference<>(idView);

        }

        @Override
        protected Bitmap doInBackground(Void... voids) {
            Location location = review.get().getLocation();
            if(location == null) return null;

            SharedPreferences preferences = context.get().getSharedPreferences(Utils.PREF_NAME, MODE_PRIVATE);
            Floor floor = FloorsDatabase.getFloor(context.get(), location.getFloorId());

            assert floor != null;
            name = floor.getName();

            try {
                return MediaStore.Images.Media.getBitmap(context.get().getContentResolver(), floor.getImage());
            }
            catch(IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            if(result == null) {
                view.get().setScaleType(ImageView.ScaleType.CENTER);
                view.get().setImageResource(R.drawable.ic_location_off_black_24dp);
            }
            else {
                int id = review.get().getLocation().getFloorId();

                view.get().setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                view.get().setImageBitmap(result);
                nameView.get().setText(name);
                idView.get().setText(String.format(Locale.getDefault(), "%1$d", id));

                Location location = review.get().getLocation();

                view.get().setNormalizedCursor(location.getXCoordinate(),
                        location.getYCoordinate());
                view.get().setShowCursor(true);
                view.get().setCursorColor(0xFF0000FF);
                view.get().setCursorRadius(10);
            }
        }
    }

}
