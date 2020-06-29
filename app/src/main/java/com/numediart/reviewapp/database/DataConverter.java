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

import android.net.Uri;

import androidx.room.TypeConverter;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DataConverter implements Serializable {
    @TypeConverter
    public String fromUri(Uri uri) {
        if(uri == null) {
            return "";
        } else {
            return uri.toString();
        }
    }

    @TypeConverter
    public Uri toUri(String s) {
        if(s.equals("")) {
            return null;
        } else {
            return Uri.parse(s);
        }
    }

    @TypeConverter
    public String fromDate(Date date) {
        if(date == null) {
            return "";
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd-HH-mm-ss/z", Locale.getDefault());
            return sdf.format(date);
        }
    }

    @TypeConverter
    public Date toDate(String s) {
        if(s.equals("")) {
            return null;
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd-HH-mm-ss/z", Locale.getDefault());
            try {
                return sdf.parse(s);
            }
            catch(ParseException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

}
