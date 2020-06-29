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

import android.util.Log;

import com.numediart.reviewapp.Utils;

import java.net.URI;

import tech.gusavila92.websocketclient.WebSocketClient;

public class PongWebSocketClient extends WebSocketClient {

    private PongListener callback;
    private boolean pongReceived;

    private String TAG_NAME = "Websocket";

    public PongWebSocketClient(URI uri, PongListener callback) {
        super(uri);
        this.callback = callback;
        pongReceived = false;

        setConnectTimeout(Utils.WEBSOCKET_CONNECT_TIMEOUT);
        setReadTimeout(Utils.WEBSOCKET_READ_TIMEOUT);
    }

    @Override
    public void onOpen() {
        Log.v(TAG_NAME, "onOpen");
    }

    @Override
    public void onTextReceived(String message) {
        Log.v(TAG_NAME, "onTextReceived");
        Log.v(TAG_NAME, "Received: " + message);
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
        pongReceived = true;
        callback.onPongReceived();
    }

    @Override
    public void onException(Exception e) {
        Log.v(TAG_NAME, "onExceptionReceived");
        Log.v(TAG_NAME, "Error: " + e.toString());
        callback.onPongError(e);
    }

    @Override
    public void onCloseReceived() {
        Log.v(TAG_NAME, "onCloseReceived");

        if(! pongReceived) {
            callback.onPongError(new Exception("Unexpected close from server !"));
        }
    }

}
