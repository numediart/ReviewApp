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

package com.numediart.reviewapp.database;

import android.content.Context;
import android.util.Log;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.numediart.reviewapp.Utils;
import com.numediart.reviewapp.models.Review;

import java.util.List;

@Database(entities = {Review.class}, version = 1, exportSchema = false)
public abstract class ReviewsDatabase extends RoomDatabase {

    private static volatile ReviewsDatabase INSTANCE;

    public abstract ReviewDao reviewDao();

    private static void getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (ReviewsDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            ReviewsDatabase.class, "Reviews")
                            .allowMainThreadQueries()
                            .build();
                }
            }
        }
    }

    public static Review getCurrentReview(Context context) {
        getInstance(context);

        String sessionKey = context.getSharedPreferences(Utils.PREF_NAME, Context.MODE_PRIVATE)
                .getString(Utils.SESSION_KEY, null);

        List<Review> results;
        synchronized (ReviewsDatabase.class) {
            results = INSTANCE.reviewDao().getUncompleteReviews(sessionKey);
        }
        if(results.size() > 0) {
            return results.get(0);
        } else {
            return createReview(context);
        }
    }

    public static boolean hasOpenReview(Context context) {
        getInstance(context);

        String sessionKey = context.getSharedPreferences(Utils.PREF_NAME, Context.MODE_PRIVATE)
                .getString(Utils.SESSION_KEY, null);

        List<Review> results;
        synchronized (ReviewsDatabase.class) {
            results = INSTANCE.reviewDao().getUncompleteReviews(sessionKey);
        }
        return results.size() > 0;
    }

    public static int pendingReviews(Context context) {
        getInstance(context);
        int nbr;

        String sessionKey = context.getSharedPreferences(Utils.PREF_NAME, Context.MODE_PRIVATE)
                .getString(Utils.SESSION_KEY, null);

        synchronized (ReviewsDatabase.class) {
            nbr = INSTANCE.reviewDao().countUnsentReviews(sessionKey);
        }

        return nbr;
    }

    public static int errorReviews(Context context) {
        getInstance(context);
        int nbr;

        String sessionKey = context.getSharedPreferences(Utils.PREF_NAME, Context.MODE_PRIVATE)
                .getString(Utils.SESSION_KEY, null);

        synchronized (ReviewsDatabase.class) {
            nbr = INSTANCE.reviewDao().countErrorSentReviews(sessionKey);
        }

        return nbr;
    }

    private static Review createReview(Context context) {
        getInstance(context);

        String sessionKey = context.getSharedPreferences(Utils.PREF_NAME, Context.MODE_PRIVATE)
                .getString(Utils.SESSION_KEY, null);

        Review review = new Review();
        review.setSessionKey(sessionKey);

        synchronized (ReviewsDatabase.class) {
            long id = INSTANCE.reviewDao().insertItem(review);
            review = INSTANCE.reviewDao().getReview(id).get(0);
        }

        Log.d("DATABASE", "New Review created. Id " + review.getId());

        return review;
    }

    public static boolean updateCurrentReview(Context context, Review review) {
        getInstance(context);

        String sessionKey = context.getSharedPreferences(Utils.PREF_NAME, Context.MODE_PRIVATE)
                .getString(Utils.SESSION_KEY, null);

        List<Review> results;
        synchronized (ReviewsDatabase.class) {
            results = INSTANCE.reviewDao().getUncompleteReviews(sessionKey);
        }

        if(results.size() == 0 || results.get(0).getId() != review.getId()) {
            Log.e("DATABASE", "No current review to update");
            return false;
        }

        synchronized (ReviewsDatabase.class) {
            INSTANCE.reviewDao().updateItem(review);
        }
        return true;
    }

    public static List<Review> getReviews(Context context) {
        getInstance(context);

        String sessionKey = context.getSharedPreferences(Utils.PREF_NAME, Context.MODE_PRIVATE)
                .getString(Utils.SESSION_KEY, null);

        List<Review> results;
        synchronized (ReviewsDatabase.class) {
            results = INSTANCE.reviewDao().getAllReviews(sessionKey);
        }
        return results;
    }

    public static Review getReview(Context context, long id) {
        getInstance(context);
        List<Review> results;
        synchronized (ReviewsDatabase.class) {
            results = INSTANCE.reviewDao().getReview(id);
        }
        if(results.size() > 0) {
            return results.get(0);
        } else {
            return null;
        }
    }

    public static List<Review> getReviewsToSend(Context context) {
        getInstance(context);

        String sessionKey = context.getSharedPreferences(Utils.PREF_NAME, Context.MODE_PRIVATE)
                .getString(Utils.SESSION_KEY, null);

        List<Review> results;
        synchronized (ReviewsDatabase.class) {
            results = INSTANCE.reviewDao().getReviewsToSend(sessionKey);
        }
        if (results.size() == 0) return null;
        else return results;
    }

    public static void markAsSent(Context context, long id) {
        getInstance(context);
        List<Review> result;
        synchronized (ReviewsDatabase.class) {
            result = INSTANCE.reviewDao().getReview(id);

            if(result.size() == 0) return;

            Review review = result.get(0);
            review.setSent(1);

            INSTANCE.reviewDao().updateItem(review);
        }
    }

    public static void markAsErrorSent(Context context, long id) {
        getInstance(context);
        List<Review> result;
        synchronized (ReviewsDatabase.class) {
            result = INSTANCE.reviewDao().getReview(id);

            if(result.size() == 0) return;

            Review review = result.get(0);
            review.setSent(-1);

            INSTANCE.reviewDao().updateItem(review);
        }
    }

    public static void deleteAll(Context context) {
        getInstance(context);
        synchronized (ReviewsDatabase.class) {
            INSTANCE.reviewDao().deleteAll();
        }
    }

    public static void generateReviews(Context context, int nbr) {
        getInstance(context);
        synchronized (ReviewsDatabase.class) {
            for(int i = 0; i < nbr; i++) {
                Review review = createReview(context);
                Utils.autofillReview(context, review);
                INSTANCE.reviewDao().updateItem(review);
            }
        }
    }
}
