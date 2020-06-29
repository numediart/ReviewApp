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

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.numediart.reviewapp.models.Review;

import java.util.List;

@Dao
public interface ReviewDao {
    @Query("SELECT * FROM Review WHERE id = :id")
    List<Review> getReview(long id);

    @Query("SELECT * FROM Review WHERE sessionKey = :key ORDER BY id DESC ")
    List<Review> getAllReviews(String key);

    @Query("SELECT * FROM Review WHERE sent <> 1 AND isComplete = 1 AND sessionKey = :key ORDER BY sent desc")
    List<Review> getReviewsToSend(String key);

    @Query("SELECT * FROM Review WHERE isComplete = 0 AND sessionKey = :key")
    List<Review> getUncompleteReviews(String key);

    @Query("SELECT COUNT(*) FROM Review WHERE sent = 0 AND sessionKey = :key")
    int countUnsentReviews(String key);

    @Query("SELECT COUNT(*) FROM Review WHERE sent = -1 AND sessionKey = :key")
    int countErrorSentReviews(String key);

    @Insert
    long insertItem(Review item);

    @Update
    void updateItem(Review item);

    @Query("DELETE FROM Review WHERE id = :reviewId")
    int deleteItem(long reviewId);

    @Query("DELETE FROM Review")
    void deleteAll();
}