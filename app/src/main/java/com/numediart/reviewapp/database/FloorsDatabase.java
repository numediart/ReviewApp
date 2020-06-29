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

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.numediart.reviewapp.Utils;
import com.numediart.reviewapp.models.Floor;

import java.util.List;

@Database(entities = {Floor.class}, version = 1, exportSchema = false)
public abstract class FloorsDatabase extends RoomDatabase {

    private static volatile FloorsDatabase INSTANCE;

    public abstract FloorDao floorDao();

    private static void getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (ReviewsDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            FloorsDatabase.class, "Floors")
                            .allowMainThreadQueries()
                            .build();
                }
            }
        }
    }

    public static void updateFloors(Context context, Floor[] floors) {
        getInstance(context);

        String sessionKey = context.getSharedPreferences(Utils.PREF_NAME, Context.MODE_PRIVATE)
                .getString(Utils.SESSION_KEY, null);

        invalidateFloors(context);

        synchronized (FloorsDatabase.class) {
            for(Floor floor: floors) {
                INSTANCE.floorDao().insertItem(floor);
            }
        }

    }

    private static void invalidateFloors(Context context) {
        getInstance(context);

        String sessionKey = context.getSharedPreferences(Utils.PREF_NAME, Context.MODE_PRIVATE)
                .getString(Utils.SESSION_KEY, null);

        synchronized (FloorsDatabase.class) {
            List<Floor> results = INSTANCE.floorDao().getFloors(sessionKey);
            for(Floor floor: results) {
                floor.setDeleted(true);
                INSTANCE.floorDao().updateItem(floor);
            }
        }
    }

    public static List<Floor> getValidFloors(Context context) {
        getInstance(context);

        String sessionKey = context.getSharedPreferences(Utils.PREF_NAME, Context.MODE_PRIVATE)
                .getString(Utils.SESSION_KEY, null);

        List<Floor> results;
        synchronized (FloorsDatabase.class) {
            results = INSTANCE.floorDao().getValidFloors(sessionKey);
        }

        if (results.size() == 0) return null;

        return results;
    }

    public static Floor getFloor(Context context, int id) {
        getInstance(context);

        String sessionKey = context.getSharedPreferences(Utils.PREF_NAME, Context.MODE_PRIVATE)
                .getString(Utils.SESSION_KEY, null);

        List<Floor> results;
        synchronized (FloorsDatabase.class) {
            results = INSTANCE.floorDao().getFloor(sessionKey, id);
        }

        if (results.size() == 0) return null;

        return results.get(0);
    }

    public static boolean existFloors(Context context) {
        getInstance(context);

        String sessionKey = context.getSharedPreferences(Utils.PREF_NAME, Context.MODE_PRIVATE)
                .getString(Utils.SESSION_KEY, null);

        List<Floor> results;
        synchronized (FloorsDatabase.class) {
            results = INSTANCE.floorDao().getValidFloors(sessionKey);
        }

        return results.size() != 0;

    }
}
