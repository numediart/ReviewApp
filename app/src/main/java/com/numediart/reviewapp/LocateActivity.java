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

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.numediart.reviewapp.database.FloorsDatabase;
import com.numediart.reviewapp.database.ReviewsDatabase;
import com.numediart.reviewapp.models.Floor;
import com.numediart.reviewapp.models.Location;
import com.numediart.reviewapp.models.Review;
import com.numediart.reviewapp.graphics.ZoomPointerImageView;

import java.util.List;

public class LocateActivity extends AppCompatActivity implements View.OnClickListener {

    private ZoomPointerImageView locateImageView;
    private TextView mapNameText;
    private Button nextButton;
    private List<Floor> floors;
    private int current;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locate);

        locateImageView = findViewById(R.id.planImageView);
        locateImageView.setCursorColor(0xFF0000FF);

        mapNameText = findViewById(R.id.mapName);

        floors = FloorsDatabase.getValidFloors(this);

        if(floors != null) {

            locateImageView.setVisibility(View.VISIBLE);
            findViewById(R.id.waitingMap).setVisibility(View.GONE);

            updateMapDisplay();

            if (floors.size() > 1) {
                findViewById(R.id.previousMap).setOnClickListener(this);
                findViewById(R.id.nextMap).setOnClickListener(this);
            } else {
                findViewById(R.id.previousMap).setVisibility(View.INVISIBLE);
                findViewById(R.id.nextMap).setVisibility(View.INVISIBLE);
            }
        }
        else {
            //Should not happen if download was ok
            Log.e("ERROR", "Map directory should exist before reaching this point...");
        }

        nextButton = findViewById(R.id.nextButton);
        nextButton.setOnClickListener(this);
    }

    protected void onResume() {
        super.onResume();
        restoreValues();
        editFields(true);
    }

    private void restoreValues() {
        Review review = ReviewsDatabase.getCurrentReview(this);
        final Location location = review.getLocation();

        if(location == null) return;

        for(int i = 0; i < floors.size(); i++) {
            if(floors.get(i).getFloorId() == location.getFloorId()) {
                current = i;
                updateMapDisplay();
                ViewTreeObserver vto = locateImageView.getViewTreeObserver();
                vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    public void onGlobalLayout() {
                        locateImageView.setNormalizedCursor(location.getXCoordinate(),
                                location.getYCoordinate());
                        locateImageView.setShowCursor(true);

                        locateImageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                });
            }
        }
    }

    private void updateMapDisplay() {
        locateImageView.reset();
        locateImageView.setImageURI(null);
        locateImageView.setImageURI(floors.get(current).getImage());
        mapNameText.setText(floors.get(current).getName() + " (" + floors.get(current).getFloorId() + ")");
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.nextButton) {
            editFields(false);
            if(! checkValues()) {
                editFields(true);
                return;
            }

            updateLocationReview();

            Class nextActivity = Utils.setNextActivity(this, LocateActivity.class);
            Intent intent = new Intent(this, nextActivity);
            startActivity(intent);
        }
        else if(v.getId() == R.id.previousMap) {
            current = current-1 < 0 ? floors.size()-1 : current-1;
            updateMapDisplay();
        }
        else if(v.getId() == R.id.nextMap) {
            current = current+1 >= floors.size() ? 0 : current+1;
            updateMapDisplay();
        }
    }

    private void updateLocationReview() {
        Review review = ReviewsDatabase.getCurrentReview(this);

        Location location = new Location();
        location.setXCoordinate(locateImageView.getXNormalizedCursor());
        location.setYCoordinate(locateImageView.getYNormalizedCursor());
        location.setFloorId(floors.get(current).getFloorId());
        review.setLocation(location);

        ReviewsDatabase.updateCurrentReview(this, review);
    }

    private boolean checkValues() {
        if(! locateImageView.isShowCursor()) {
            Toast toast = Toast.makeText(getApplicationContext(), R.string.locate_error, Toast.LENGTH_SHORT);
            toast.show();

            return false;
        }
        return true;
    }

    private void editFields(boolean editable) {
        nextButton.setEnabled(editable);
    }
}
