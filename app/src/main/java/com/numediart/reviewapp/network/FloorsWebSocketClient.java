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
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.numediart.reviewapp.Utils;
import com.numediart.reviewapp.database.FloorsDatabase;
import com.numediart.reviewapp.models.Floor;

import java.net.URI;
import java.util.Objects;

import tech.gusavila92.websocketclient.WebSocketClient;

import static android.content.Context.MODE_PRIVATE;

public class FloorsWebSocketClient extends WebSocketClient {

    private Context context;
    private FloorsListener callback;
    private boolean floorsReceived;
    private String TAG_NAME = "Websocket";

    public FloorsWebSocketClient(URI uri, Context context, FloorsListener callback, boolean reconnect) {
        super(uri);
        this.context = context;
        this.callback = callback;
        floorsReceived = false;

        setConnectTimeout(Utils.WEBSOCKET_CONNECT_TIMEOUT);
        setReadTimeout(Utils.WEBSOCKET_READ_TIMEOUT);

        if(reconnect) {
            enableAutomaticReconnection(Utils.WEBSOCKET_CONNECT_TIMEOUT);
        }
    }

    @Override
    public void onOpen() {
        Log.v(TAG_NAME, "onOpen");
    }

    @Override
    public void onTextReceived(String message) {
        Log.v(TAG_NAME, "onTextReceived");
        Log.v(TAG_NAME, "Received: " + message);

        onFloorsReceived(message);
        floorsReceived = true;
        callback.onFloorsCompleted();
    }

    @Override
    public void onBinaryReceived(byte[] data) {
        Log.v(TAG_NAME, "onBinaryReceived");
    }

    @Override
    public void onPingReceived(byte[] data) {
        Log.v(TAG_NAME, "onPingReceived");
    }

    @Override
    public void onPongReceived(byte[] data) {
        Log.v(TAG_NAME, "onPongReceived");
    }

    @Override
    public void onException(Exception e) {
        Log.v(TAG_NAME, "onExceptionReceived");
        Log.v(TAG_NAME, "Error: " + e.toString());
        callback.onFloorsError(e);
    }

    @Override
    public void onCloseReceived() {
        Log.v(TAG_NAME, "onCloseReceived");

        if(! floorsReceived) {
            callback.onFloorsError(new Exception("Unexpected close from server !"));
        }
    }

    private void onFloorsReceived(String message) {
        SharedPreferences preferences = context.getSharedPreferences(Utils.PREF_NAME, MODE_PRIVATE);

        Gson gson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .registerTypeAdapter(Floor.class, new FloorDeserializer(context,
                        Objects.requireNonNull(preferences.getString(Utils.SESSION_KEY, null))))
                .create();

        Floor[] floors = gson.fromJson(message, Floor[].class);

        FloorsDatabase.updateFloors(context, floors);
    }
}
