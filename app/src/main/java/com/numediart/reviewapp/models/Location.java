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

package com.numediart.reviewapp.models;

import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Location {
    @SerializedName("XCoordinate")
    @Expose
    private float xCoordinate;

    @SerializedName("YCoordinate")
    @Expose
    private float yCoordinate;

    @SerializedName("Id")
    @Expose
    private int floorId;

    public Location() {
        floorId = Integer.MIN_VALUE;
    }

    public float getXCoordinate() {
        return xCoordinate;
    }

    public void setXCoordinate(float x) {
        this.xCoordinate = x;
    }

    public float getYCoordinate() {
        return yCoordinate;
    }

    public void setYCoordinate(float y) {
        this.yCoordinate = y;
    }

    public int getFloorId() {
        return floorId;
    }

    public void setFloorId(int floorId) {
        this.floorId = floorId;
    }

    @NonNull
    public String toString() {
        return xCoordinate +"; " + yCoordinate + "; " + floorId;
    }
}
