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

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.numediart.reviewapp.database.ReviewsDatabase;
import com.numediart.reviewapp.models.Review;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Objects;

public class TakePictureActivity extends AppCompatActivity implements View.OnClickListener{

    private static final int TAKE_PICTURE_ACTIVITY_REQUEST_CODE = 0;
    private static final int SELECT_IMG_ACTIVITY_REQUEST_CODE = 1;
    private ImageView imgView;
    private Uri imgUri, tmpUri;
    private Button galleryButton, cameraButton, nextButton;
    private boolean takingPicture;
    private Utils.AsyncImageTask asyncLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_picture);

        imgView = findViewById(R.id.imageView);

        galleryButton = findViewById(R.id.galleryButton);
        galleryButton.setOnClickListener(this);

        cameraButton = findViewById(R.id.cameraButton);
        cameraButton.setOnClickListener(this);

        nextButton = findViewById(R.id.nextButton);
        nextButton.setOnClickListener(this);

        String tmpPath = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + "/tmp.jpg";
        tmpUri = FileProvider.getUriForFile(TakePictureActivity.this, "com.numediart.reviewapp.provider", new File(tmpPath));

        restoreValues();
        if(imgUri == null) {
            editFields(false);
            loadImage(imgView, null);
            takePicture();
        }
    }

    protected void onResume() {
        super.onResume();
        if(imgUri == null) {
            restoreValues();
        }
        if(! takingPicture) {
            editFields(true);
        }
    }

    protected void onDestroy() {
        super.onDestroy();
        if(asyncLoader != null) {
            asyncLoader.cancel(true);
            asyncLoader = null;
        }
    }

    private void restoreValues() {
        Review review = ReviewsDatabase.getCurrentReview(this);
        Uri uri = review.getPicture();

        if(uri == null) return;

        imgUri = uri;
        loadImage(imgView, imgUri);
    }


    // Update screen when user has taken a picture
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == TAKE_PICTURE_ACTIVITY_REQUEST_CODE && resultCode==RESULT_OK) {
            imgUri = tmpUri;
            loadImage(imgView, imgUri);
        }
        else if(requestCode==SELECT_IMG_ACTIVITY_REQUEST_CODE && resultCode==RESULT_OK && data!=null) {
            imgUri = data.getData();
            loadImage(imgView, imgUri);
        }
        takingPicture = false;
    }

    private void loadImage(ImageView imgView, Uri imgUri) {
        if(asyncLoader != null) {
            asyncLoader.cancel(true);
            asyncLoader = null;
        }

        asyncLoader = new Utils.AsyncImageTask(imgView, imgUri, this);
        asyncLoader.execute();
    }

    // Start an activity to select a picture from gallery
    private void selectPicture() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, SELECT_IMG_ACTIVITY_REQUEST_CODE);
    }

    // Start an activity to take a picture from camera
    private void takePicture() {
        takingPicture = true;

        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, tmpUri);
        intent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        startActivityForResult(intent, TAKE_PICTURE_ACTIVITY_REQUEST_CODE);
    }

    // Save picture to application datas
    private Uri savePicture(String filename) {
        File target = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), filename);

        try {
            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(target));
            BufferedInputStream in = new BufferedInputStream(
                    Objects.requireNonNull(getContentResolver().openInputStream(imgUri)));
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

        return Uri.fromFile(target);
    }

    @Override
    public void onClick(View v) {
        editFields(false);
        if(v.getId() == R.id.galleryButton) {
            selectPicture();
        }
        else if(v.getId() == R.id.cameraButton) {
            takePicture();
        }
        else if(v.getId() == R.id.nextButton) {
            if(! checkValues()) {
                editFields(true);
                return;
            }

            updatePictureReview();

            Class nextActivity = Utils.setNextActivity(this, TakePictureActivity.class);
            Intent intent = new Intent(this, nextActivity);
            startActivity(intent);
        }
    }

    private boolean checkValues() {
        if(imgUri == null) {
            Toast toast = Toast.makeText(getApplicationContext(), R.string.picture_error, Toast.LENGTH_SHORT);
            toast.show();

            return false;
        }
        return true;
    }

    private void updatePictureReview() {
        Review review = ReviewsDatabase.getCurrentReview(this);
        if(! imgUri.equals(review.getPicture())) {
            Uri pictureUri = savePicture(review.getId() + ".jpg");
            review.setPicture(pictureUri);
            ReviewsDatabase.updateCurrentReview(this, review);
        }
    }

    private void editFields(boolean editable) {
        nextButton.setEnabled(editable);
        cameraButton.setEnabled(editable);
        galleryButton.setEnabled(editable);
    }
}

