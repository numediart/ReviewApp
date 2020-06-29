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
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.Toast;

import com.numediart.reviewapp.database.ReviewsDatabase;
import com.numediart.reviewapp.graphics.ColorPointerImageView;
import com.numediart.reviewapp.models.Emotion;
import com.numediart.reviewapp.models.Review;

public class ScoreActivity extends AppCompatActivity implements View.OnClickListener {

    private ColorPointerImageView emotionGraph;
    private Button nextButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);

        emotionGraph = findViewById(R.id.emotionGraph);
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
        final Emotion emotion = review.getEmotion();

        if(emotion == null) return;

        ViewTreeObserver vto = emotionGraph.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                emotionGraph.setNormalizedCursor((emotion.getIntensity() + 1) / 2,
                        (emotion.getValence() - 1) / -2);
                emotionGraph.setShowCursor(true);

                emotionGraph.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.nextButton) {
            editFields(false);

            if(!checkValues()) {
                editFields(true);
                return;
            }

            updateScoreReview();

            Class nextActivity = Utils.setNextActivity(this, ScoreActivity.class);
            Intent intent = new Intent(this, nextActivity);
            startActivity(intent);
        }
    }

    private boolean checkValues() {
        if(! emotionGraph.isShowCursor()) {
            Toast toast = Toast.makeText(getApplicationContext(), R.string.score_error, Toast.LENGTH_SHORT);
            toast.show();

            return false;
        }
        return true;
    }

    private void updateScoreReview() {
        Review review = ReviewsDatabase.getCurrentReview(this);
        float intensity = emotionGraph.getXNormalizedCursor() * 2.0f - 1.0f;
        float valence = emotionGraph.getYNormalizedCursor() * -2.0f + 1.0f;

        Emotion emotion = new Emotion();
        emotion.setIntensity(intensity);
        emotion.setValence(valence);
        review.setEmotion(emotion);

        ReviewsDatabase.updateCurrentReview(this, review);
    }

    private void editFields(boolean editable) {
        nextButton.setEnabled(editable);
    }
}
