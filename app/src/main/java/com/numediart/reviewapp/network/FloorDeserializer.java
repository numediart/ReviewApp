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

package com.numediart.reviewapp.network;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Base64;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.numediart.reviewapp.Utils;
import com.numediart.reviewapp.models.Floor;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;

public class FloorDeserializer implements JsonDeserializer<Floor> {

    private String sessionKey;
    private Context context;

    public FloorDeserializer(Context context, String sessionKey) {
        this.context = context;
        this.sessionKey = sessionKey;
    }

    @Override
    public Floor deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext jsonContext) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();

        Floor floor = new Floor();

        floor.setName(jsonObject.get("Name").getAsString());
        floor.setFloorId(jsonObject.get("Id").getAsInt());
        floor.setSessionKey(sessionKey);

        String encodedImage = jsonObject.get("Image").getAsString();
        File mapDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), sessionKey);

        if(!mapDir.exists()) {
            mapDir.mkdir();
        }

        File file = new File(mapDir, floor.getFloorId() + Utils.FLOOR_EXT);

        byte[] byteArrayImage = Base64.decode(encodedImage, Base64.DEFAULT);
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            bos.write(byteArrayImage);
            bos.flush();
            bos.close();

            floor.setImage(Uri.fromFile(file));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return floor;
    }
}
