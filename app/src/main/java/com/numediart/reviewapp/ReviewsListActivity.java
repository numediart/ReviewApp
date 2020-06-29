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

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.numediart.reviewapp.database.ReviewsDatabase;
import com.numediart.reviewapp.models.Review;

public class ReviewsListActivity extends AppCompatActivity implements ReviewRecyclerViewAdapter.OnListFragmentInteractionListener{

    private RecyclerView recyclerView;
    private ReviewRecyclerViewAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private boolean lock_listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviews_list);

        recyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // specify an adapter (see also next example)
        mAdapter = new ReviewRecyclerViewAdapter(this, ReviewsDatabase.getReviews(this), this);
        recyclerView.setAdapter(mAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        lock_listener = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAdapter.cancelAll();
    }


    @Override
    public void onListFragmentInteraction(Review item) {
        //Not an atomic operation but will do the job
        if(lock_listener) return;
        else lock_listener = true;

        Intent intent = new Intent(this, ReviewActivity.class);
        intent.putExtra(ReviewActivity.INTENT_TAG, item.getId());
        startActivity(intent);
    }
}
