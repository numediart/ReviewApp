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
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.numediart.reviewapp.models.Floor;

import java.util.List;

@Dao
public interface FloorDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertItem(Floor item);

    @Update
    void updateItem(Floor item);

    @Query("SELECT * FROM Floor WHERE sessionKey = :key AND floorId = :id")
    List<Floor> getFloor(String key, int id);

    @Query("SELECT * FROM Floor WHERE sessionKey = :key")
    List<Floor> getFloors(String key);

    @Query("SELECT * FROM Floor WHERE sessionKey = :key and deleted = 0")
    List<Floor> getValidFloors(String key);

    @Query("SELECT * FROM Floor")
    List<Floor> getAllDatabase();

}
