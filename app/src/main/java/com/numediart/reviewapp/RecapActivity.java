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

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.numediart.reviewapp.database.FloorsDatabase;
import com.numediart.reviewapp.database.ReviewsDatabase;
import com.numediart.reviewapp.models.Review;
import com.numediart.reviewapp.network.FloorsListener;
import com.numediart.reviewapp.network.FloorsWebSocketClient;
import com.numediart.reviewapp.network.ReviewsListener;
import com.numediart.reviewapp.network.ReviewsWebSocketClient;

import java.io.File;
import java.lang.ref.WeakReference;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.List;

public class RecapActivity extends AppCompatActivity implements View.OnClickListener, FloorsListener {

    Button nextButton, syncButton, listReviewsButton;
    ProgressBar upload;
    TextView infoView, sessionView;
    FloorsWebSocketClient floorWebsocket;
    int nbr_pending, nbr_error;
    boolean hasOpenReview, refreshingMap;
    AsyncUploadTask uploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recap);

        infoView = findViewById(R.id.infoView);
        sessionView = findViewById(R.id.sessionKeyView);
        upload = findViewById(R.id.uploading);

        nextButton = findViewById(R.id.nextButton);
        nextButton.setOnClickListener(this);

        syncButton = findViewById(R.id.syncButton);
        syncButton.setOnClickListener(this);

        listReviewsButton = findViewById(R.id.listReviewsButton);
        listReviewsButton.setOnClickListener(this);

        if(! getIntent().getBooleanExtra(Utils.OFFLINE_MODE, false)) {
            refreshMap();
        }
    }

    protected void onResume() {
        super.onResume();

        SharedPreferences prefs = getSharedPreferences(Utils.PREF_NAME, MODE_PRIVATE);
        String sessionId = prefs.getString(Utils.SESSION_KEY, null);
        sessionView.setText(String.format(getResources().getString(R.string.session_id), sessionId));

        if(!refreshingMap) {
            editFields(true);
        }
    }

    protected void onPause() {
        super.onPause();
        if(uploadTask != null) {
            uploadTask.cancel(true);
            upload.setVisibility(View.GONE);
            uploadTask = null;
        }
    }

    @Override
    public void onClick(View v) {
        editFields(false);

        if(v.getId() == R.id.nextButton) {
            Class nextActivity = Utils.setNextActivity(this, RecapActivity.class);
            Intent intent = new Intent(this, nextActivity);
            startActivity(intent);
        } else if(v.getId() == R.id.syncButton) {
            uploadTask = new AsyncUploadTask(this, upload, syncButton);
            uploadTask.execute();

            editFields(true);
        } else if(v.getId() == R.id.listReviewsButton) {
            Intent intent = new Intent(this, ReviewsListActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(BuildConfig.DEBUG) {
            getMenuInflater().inflate(R.menu.menu_debug, menu);
        }
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.generate) {
            editFields(false);
            File tmpPath = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + "/tmp.jpg");
            if(! tmpPath.exists()) {
                Toast.makeText(this, "No tmp image to use for generation !", Toast.LENGTH_SHORT).show();
            } else {
                ReviewsDatabase.generateReviews(this, Utils.REVIEWS_GENERATION_NBR);
            }
            editFields(true);
        }
        return super.onOptionsItemSelected(item);
    }

    public void onFloorsCompleted() {
        this.runOnUiThread(new Runnable(){
            @Override
            public void run() {
                mapRefreshed();
            }
        });

    }

    public void onFloorsError(Exception e) {
        Log.e("Websocket", e.getMessage() != null ? e.getMessage() : "Unexpected FloorsError");
        final String msg = e.getMessage();
        this.runOnUiThread(new Runnable(){
            @Override
            public void run() {
                Toast.makeText(getBaseContext(), "Error: " + msg, Toast.LENGTH_SHORT).show();
                mapRefreshed();
            }
        });
    }

    private void refreshMap() {
        refreshingMap = true;
        boolean reconnect = ! FloorsDatabase.existFloors(this);

        // Refreshing map has to forbid user click to avoid starting a review
        // with unfinished map downloads (no matter if it is a first dowload or a refresh)
        editFields(false);
        findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);

        floorWebsocket = new FloorsWebSocketClient(Utils.getUriFloors(this), this,
                this, reconnect);
        floorWebsocket.connect();
        floorWebsocket.send("");
    }

    private void mapRefreshed() {
        if(floorWebsocket != null) {
            floorWebsocket.close();
        }

        floorWebsocket = null;

        findViewById(R.id.loadingPanel).setVisibility(View.INVISIBLE);
        editFields(true);

        refreshingMap = false;
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.log_out)
                .setMessage(R.string.log_out_message)

                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        parentBackPressed();
                    }
                })

                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    public void parentBackPressed() {
        //Don't use super as it could go back to previous review edition !
        //super.onBackPressed();

        mapRefreshed();

        Intent intent = new Intent(this, LoadingActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void editFields(boolean editable) {
        nextButton.setEnabled(editable);
        listReviewsButton.setEnabled(editable);

        refreshNbrPending();

        hasOpenReview = ReviewsDatabase.hasOpenReview(this);
        if(hasOpenReview) {
            nextButton.setText(R.string.start_edit_button);
        } else {
            nextButton.setText(R.string.start_button);
        }

        syncButton.setEnabled(enableUploadButton() && editable);
    }

    private void refreshNbrPending() {
        nbr_pending = ReviewsDatabase.pendingReviews(this);

        nbr_error = ReviewsDatabase.errorReviews(this);

        String infos = String.format(getResources().getString(R.string.pending_reviews), nbr_pending);
        if(nbr_error > 0) {
            infos += "\n" + String.format(getResources().getString(R.string.error_reviews), nbr_error);
        }

        infoView.setText(infos);
    }

    public boolean enableUploadButton() {
        // Check if there are reviews ready (not in edition mode) and never sent
        boolean pendingDone = nbr_pending == 0|| (nbr_pending == 1 && hasOpenReview);

        // Check moreover if there are not reviews in error that can be sent again
        boolean canSendReviews = !pendingDone || nbr_error != 0;

        // Avoid enabling if upload is already running
        return uploadTask== null && canSendReviews;
    }

    private static class AsyncUploadTask extends AsyncTask<Void, Void, Void> implements ReviewsListener {

        private final WeakReference<RecapActivity> activity;
        private final WeakReference<ProgressBar> uploading;
        private final WeakReference<Button> syncButton;
        private String errorReview, errorServer;
        private Review current;
        private boolean sent;
        private ReviewsWebSocketClient reviewsWebsocket;

        AsyncUploadTask(RecapActivity activity, ProgressBar uploading, Button syncButton) {
            this.activity = new WeakReference<>(activity);
            this.uploading = new WeakReference<>(uploading);
            this.syncButton = new WeakReference<>(syncButton);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            syncButton.get().setEnabled(false);
            uploading.get().setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            reviewsWebsocket = new ReviewsWebSocketClient(Utils.getUriNotes(activity.get()), activity.get(), this);
            reviewsWebsocket.connect();

            List<Review> reviews = ReviewsDatabase.getReviewsToSend(activity.get());

            if(reviews == null) return null;

            while(reviews.size() != 0) {
                current = reviews.remove(0);
                Log.d("UPLOAD", "Review: " + current.getId());

                sent = false;
                errorReview = null;
                errorServer = null;

                reviewsWebsocket.sendReview(current);

                while(! sent && errorReview == null && errorServer == null) {
                    //Do nothing
                    try {
                        Thread.sleep(500);
                    }
                    catch(InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                if(isCancelled()) {
                    return null;
                }

                Log.d("UPLOAD", "Review uploaded");
                publishProgress();
            }
            return null;
        }

        @Override
        public void onProgressUpdate(Void... progress) {
            super.onProgressUpdate(progress);
            activity.get().refreshNbrPending();
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();

            if(reviewsWebsocket != null) {
                reviewsWebsocket.close();
            }

            // If there is an error msg while cancelling, it has been called from inside the thread,
            // meaning we are still showing the activity.
            if(errorReview != null || errorServer != null) {
                refreshDisplayOnClose();
            }
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if(reviewsWebsocket != null) {
                reviewsWebsocket.close();
            }

            refreshDisplayOnClose();
        }

        private void refreshDisplayOnClose() {
            if(this.errorServer != null) {
                Toast.makeText(activity.get(), this.errorServer, Toast.LENGTH_LONG).show();
            } else if (this.errorReview != null) {
                Toast.makeText(activity.get(),
                        String.format(activity.get().getResources().getString(R.string.upload_error), current.getId())
                                + this.errorReview, Toast.LENGTH_LONG).show();
            }

            activity.get().refreshNbrPending();
            uploading.get().setVisibility(View.GONE);
            activity.get().uploadTask = null;
            syncButton.get().setEnabled(activity.get().enableUploadButton());
        }

        public void onReviewSent() {
            ReviewsDatabase.markAsSent(activity.get(), current.getId());
            sent = true;
        }

        public void onReviewError(Exception e) {
            e.printStackTrace();

            //Server got an exception unrelated to review
            if (e instanceof SocketTimeoutException || e instanceof SocketException || current == null) {
                errorServer = e.getMessage() != null ? e.getMessage() : "Unknown server error";
                this.cancel(false);
                return;
            }

            errorReview = e.getMessage() != null ? e.getMessage() : "Unknown review error";
            ReviewsDatabase.markAsErrorSent(activity.get(), current.getId());

            //Interrupt upload if it is the first time this review got error
            if(current.getSent() == 0) {
                this.cancel(false);
            }
        }
    }
}