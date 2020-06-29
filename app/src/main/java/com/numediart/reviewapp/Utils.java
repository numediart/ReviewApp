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

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.numediart.reviewapp.database.FloorsDatabase;
import com.numediart.reviewapp.database.ReviewsDatabase;
import com.numediart.reviewapp.models.Emotion;
import com.numediart.reviewapp.models.Floor;
import com.numediart.reviewapp.models.Location;
import com.numediart.reviewapp.models.Review;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class Utils {
    public static final int REVIEWS_GENERATION_NBR = 10;

    // milliseconds to wait before throwing an exception if there is no more answer from server
    public static final int WEBSOCKET_READ_TIMEOUT = 20000;

    // milliseconds to wait before throwing an exception if we could not connect to server
    // Also used as "automatic reconnection wait time value"
    public static final int WEBSOCKET_CONNECT_TIMEOUT = 5000;

    public static final String USER_NAME = "USER_NAME";
    public static final String SERVER_IP = "SERVER_IP";
    public static final String SERVER_PORT = "SERVER_PORT";
    public static final String SESSION_KEY = "PASSWORD";
    public static final String OFFLINE_MODE = "OFFLINE_MODE";

    public static final String PREF_NAME = "REVIEW_APP";

    public static final String FLOOR_EXT = ".png";

    private static volatile Class[] activityList;

    public static void fillActivityList() {
        activityList = new Class[5];
        activityList[0] = RecapActivity.class;
        activityList[1] = LocateActivity.class;
        activityList[2] = TakePictureActivity.class;
        activityList[3] = ScoreActivity.class;
        activityList[4] = NotesActivity.class;
    }

    public static Class setNextActivity(Context context, Class actualActivity) {
        if(activityList == null) {
            fillActivityList();
        }
        
        for(int i = 0; i < activityList.length; i++) {
            if(activityList[i].equals(actualActivity)) {
                if (i == activityList.length - 1) {
                    completeReview(context);
                    displayReviews(context);
                    Toast.makeText(context, R.string.review_completed, Toast.LENGTH_SHORT).show();

                    return activityList[0];
                }
                else {
                    if(i == 0) {
                        initReview(context);
                    }
                    return activityList[i+1];
                }
            }
        }
        return null;
    }

    public static void displayReviews(Context context) {
        List<Review> results = ReviewsDatabase.getReviews(context);
        for(Review r : results) {
            Log.d("DATABASE", r.toString());
        }
    }

    public static void initReview(Context context) {
        Review review = ReviewsDatabase.getCurrentReview(context);
        SharedPreferences preferences = context.getSharedPreferences(Utils.PREF_NAME, Context.MODE_PRIVATE);
        review.setAuthor(preferences.getString(Utils.USER_NAME, null));
        review.setSessionKey(preferences.getString(Utils.SESSION_KEY, null));
        ReviewsDatabase.updateCurrentReview(context, review);
    }

    public static void completeReview(Context context) {
        Review review = ReviewsDatabase.getCurrentReview(context);
        review.setDate(new Date());
        review.setComplete(true);
        ReviewsDatabase.updateCurrentReview(context, review);
    }

    //Debug purpose
    public static void cleanAll(Context context) {
        ReviewsDatabase.deleteAll(context);
        File dir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        if(dir == null) return;

        File[] files = dir.listFiles();

        if(files == null) return;

        for (File f : files) f.delete();
    }

    public static String getUriBaseString(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(Utils.PREF_NAME, Context.MODE_PRIVATE);
        return "ws://" + prefs.getString(Utils.SERVER_IP, null)
                + ":" + prefs.getInt(Utils.SERVER_PORT, 0)
                + "/" + prefs.getString(Utils.SESSION_KEY, null);
    }

    public static URI getUriBase(Context context) {
        try {
            return new URI(getUriBaseString(context));
        } catch(URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static URI getUriFloors(Context context) {
        try {
            return new URI(getUriBaseString(context) + "/floors");
        } catch(URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static URI getUriNotes(Context context) {
        try {
            return new URI(getUriBaseString(context) + "/note");
        } catch(URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

    //Debug purpose
    public static void autofillReview(Context context, Review review) {
        Random rand = new Random();

        review.setTitle("Generated title - " + review.getId());
        review.setContent("This is an automated generated review for debug purpose. This is an automated generated review for debug purpose. This is an automated generated review for debug purpose.");
        review.setAuthor("Automatic Generation");
        review.setDate(new Date());

        Emotion emotion = new Emotion();
        emotion.setIntensity(rand.nextFloat() * 2 - 1);
        emotion.setValence(rand.nextFloat() * 2 - 1);
        review.setEmotion(emotion);

        Location location = new Location();
        List<Floor> floors = FloorsDatabase.getValidFloors(context);

        assert floors != null;
        location.setFloorId(floors.get(rand.nextInt(floors.size())).getFloorId());
        location.setXCoordinate(rand.nextFloat());
        location.setYCoordinate(rand.nextFloat());

        review.setLocation(location);

        String tmpPath = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + "/tmp.jpg";
        Uri tmp = FileProvider.getUriForFile(context, "com.numediart.reviewapp.provider", new File(tmpPath));

        File target = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), review.getId() + ".jpg");
        try {
            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(target));
            BufferedInputStream in = new BufferedInputStream(
                    Objects.requireNonNull(context.getContentResolver().openInputStream(tmp)));
            int b = in.read();
            while(b != -1) {
                out.write(b);
                b = in.read();
            }
            out.flush();
            out.close();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        review.setPicture(Uri.fromFile(target));
        review.setComplete(true);
    }


    public static class AsyncImageTask extends AsyncTask<Void, Void, Drawable> {

        private final WeakReference<Uri> uri;
        private final WeakReference<Context> context;
        private final WeakReference<ImageView> view;

        public AsyncImageTask(ImageView view, Uri uri, Context context) {
            this.uri = new WeakReference<>(uri);
            this.context = new WeakReference<>(context);
            this.view = new WeakReference<>(view);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            view.get().setScaleType(ImageView.ScaleType.CENTER);
            view.get().setImageResource(R.drawable.ic_image_black_24dp);
        }

        @Override
        protected Drawable doInBackground(Void... voids) {
            if(uri.get() == null) return null;
            try {
                return Drawable.createFromStream(context.get().getContentResolver().openInputStream(uri.get()), uri.get().getPath());
            }
            catch(IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Drawable result) {
            super.onPostExecute(result);

            ImageView imageview = this.view.get();

            if(result == null) {
                imageview.setScaleType(ImageView.ScaleType.CENTER);
                imageview.setImageResource(R.drawable.ic_block_black_24dp);
            } else {
                imageview.setScaleType(ImageView.ScaleType.FIT_XY);
                imageview.setImageDrawable(result);
            }
        }
    }
}
