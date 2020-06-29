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

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.numediart.reviewapp.database.ReviewsDatabase;
import com.numediart.reviewapp.models.Review;

public class NotesActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText notesTitle, notesContent;
    private Button nextButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        notesTitle = findViewById(R.id.editNotesTitle);
        notesContent = findViewById(R.id.editNotesContent);

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
        notesTitle.setText(review.getTitle());
        notesContent.setText(review.getContent());
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.nextButton) {
            editFields(false);

            if(!checkValues()) {
                editFields(true);
                return;
            }

            updateNotesReview();

            Class nextActivity = Utils.setNextActivity(this, NotesActivity.class);
            Intent intent = new Intent(this, nextActivity);
            //As it is the last activity for our adding review Workflow, clear the task
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            startActivity(intent);
        }
    }

    private boolean checkValues() {
        if(notesTitle.getText().toString().isEmpty() || notesContent.getText().toString().isEmpty()) {
            Toast toast = Toast.makeText(getApplicationContext(), R.string.notes_error, Toast.LENGTH_SHORT);
            toast.show();

            return false;
        }
        return true;
    }

    private void updateNotesReview() {
        Review review = ReviewsDatabase.getCurrentReview(this);
        review.setTitle(notesTitle.getText().toString());
        review.setContent(notesContent.getText().toString());
        ReviewsDatabase.updateCurrentReview(this, review);
    }

    private void editFields(boolean editable) {
        nextButton.setEnabled(editable);
        notesTitle.setEnabled(editable);
        notesContent.setEnabled(editable);
    }

    @Override
    public void onBackPressed() {
        updateNotesReview();
        super.onBackPressed();
    }
}
