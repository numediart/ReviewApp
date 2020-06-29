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

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.numediart.reviewapp.models.Review;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Review} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 */
public class ReviewRecyclerViewAdapter extends RecyclerView.Adapter<ReviewRecyclerViewAdapter.ViewHolder> {

    private final List<Review> values;
    private final List<ViewHolder> holders;
    private final OnListFragmentInteractionListener listener;
    private final Context context;

    public ReviewRecyclerViewAdapter(Context context, List<Review> items, OnListFragmentInteractionListener listener) {
        values = items;
        this.listener = listener;
        this.context = context;
        this.holders = new ArrayList<>();
    }

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_review, parent, false);

        ViewHolder holder = new ViewHolder(view);
        holders.add(holder);

        return holder;
    }

    public void cancelAll() {
        for(int i = 0; i < holders.size(); i++) {
            if (holders.get(i).asyncPicture != null) {
                holders.get(i).asyncPicture.cancel(true);
                holders.get(i).asyncPicture = null;
            }
        }
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        //Refresh icon
        holder.iconView.setScaleType(ImageView.ScaleType.CENTER);
        holder.iconView.setImageResource(R.drawable.ic_image_black_24dp);
        if(holder.asyncPicture != null) {
            holder.asyncPicture.cancel(true);
            holder.asyncPicture = null;
        }

        holder.item = values.get(position);
        holder.idView.setText(String.format(Locale.getDefault(), "%1$d", values.get(position).getId()));
        holder.asyncPicture = new Utils.AsyncImageTask(holder.iconView, holder.item.getPicture(), context);
        holder.asyncPicture.execute();

        holder.contentView.setText(values.get(position).getTitle());

        if(holder.item.isComplete()) {
            holder.completeView.setImageResource(R.drawable.ic_done_black_24dp);
        } else {
            holder.completeView.setImageResource(R.drawable.ic_create_black_24dp);
        }

        if(holder.item.getSent() == 1) {
            holder.sentView.setImageResource(R.drawable.ic_cloud_done_black_24dp);
        } else if(holder.item.getSent() == -1) {
            holder.sentView.setImageResource(R.drawable.ic_report_problem_black_24dp);
        } else {
            holder.sentView.setImageResource(R.drawable.ic_cloud_off_black_24dp);
        }

        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    listener.onListFragmentInteraction(holder.item);
                }
            }
        });
    }

    @Override
    public void onViewRecycled(ViewHolder holder) {
        holder.asyncPicture.cancel(true);
    }

    @Override
    public int getItemCount() {
        return values.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final View view;
        final TextView idView;
        final TextView contentView;
        final ImageView iconView;
        final ImageView completeView;
        final ImageView sentView;
        Utils.AsyncImageTask asyncPicture;
        Review item;

        ViewHolder(View view) {
            super(view);
            this.view = view;
            idView = view.findViewById(R.id.id);
            iconView = view.findViewById(R.id.iconPicture);
            contentView = view.findViewById(R.id.title);
            completeView = view.findViewById(R.id.complete);
            sentView = view.findViewById(R.id.sent);
        }

        @Override
        @NonNull
        public String toString() {
            return super.toString() + " '" + contentView.getText() + "'";
        }
    }

    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(Review item);
    }
}
