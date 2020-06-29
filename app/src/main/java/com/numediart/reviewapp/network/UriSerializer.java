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
import android.util.Base64;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;

public class UriSerializer implements JsonSerializer<Uri> {

    private Context context;

    public UriSerializer(Context context) {
        this.context = context;
    }

    @Override
    public JsonElement serialize(Uri src, Type typeOfSrc, JsonSerializationContext jsonContext) {
        try {
            int buffer_size = 2048;
            InputStream is = context.getContentResolver().openInputStream(src);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] b = new byte[buffer_size];
            int length = is.read(b);
            while(length == buffer_size) {
                baos.write(b);
                length = is.read(b);
            }

            baos.flush();

            byte[] byteArrayImage = baos.toByteArray();

            String encodedImage = Base64.encodeToString(byteArrayImage, Base64.DEFAULT);

            is.close();
            baos.close();

            return new JsonPrimitive(encodedImage);

        } catch(IOException e) {
            e.printStackTrace();
            return null;
        }

    }
}
