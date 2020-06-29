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
import android.util.Base64DataException;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.numediart.reviewapp.Utils;
import com.numediart.reviewapp.models.Review;

import java.io.IOException;
import java.net.SocketException;
import java.net.URI;
import java.util.Date;

import tech.gusavila92.websocketclient.WebSocketClient;

public class ReviewsWebSocketClient extends WebSocketClient {

    private ReviewsListener callback;
    public String TAG_NAME = "Websocket";
    private Gson gson;

    public ReviewsWebSocketClient(URI uri, Context context, ReviewsListener callback) {
        super(uri);
        this.callback = callback;

        gson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .registerTypeAdapter(Uri.class, new UriSerializer(context))
                .registerTypeAdapter(Date.class, new DateSerializer())
                .create();

        setConnectTimeout(Utils.WEBSOCKET_CONNECT_TIMEOUT);
        setReadTimeout(Utils.WEBSOCKET_READ_TIMEOUT);
    }

    @Override
    public void onOpen() {
        Log.v(TAG_NAME, "onOpen");
    }

    @Override
    public void onTextReceived(String message) {
        switch(message) {
            case "INCORRECT_NOTE_FORMAT":
                Exception inf = new JsonSyntaxException("The JSON sent to server is missing one of the mandatory keys.");
                this.onException(inf);
                break;
            case "JSON_PARSING_ERROR":
                Exception jpe = new JsonParseException("Server could not parse the JSON data.");
                this.onException(jpe);
                break;
            case "TEXTURE_CONVERSION_ERROR":
                Exception tce = new Base64DataException("Image data is not a valid JPG Base64 string.");
                this.onException(tce);
                break;
            case "SAVE_DATA_ERROR":
                Exception sde = new IOException("Server could not save file.");
                this.onException(sde);
                break;
            case "UNKNOWN_FLOOR_ID":
                Exception ufl = new IndexOutOfBoundsException("Location Id does not exist.");
                this.onException(ufl);
                break;
            case "ERROR":
                Exception e = new Exception("Unidentified error, see server logs.");
                this.onException(e);
                break;
            case "OK":
                Log.v(TAG_NAME, "onTextReceived");
                Log.v(TAG_NAME, "Received: " + message);
                callback.onReviewSent();
                break;
            default:
                Log.v(TAG_NAME, "onTextReceived");
                Log.v(TAG_NAME, "Received: " + message);
                Exception unexpected = new Exception("Unidentified error, received \"" + message + "\" message.");
                this.onException(unexpected);
                break;
        }
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
        callback.onReviewError(e);
    }

    @Override
    public void onCloseReceived() {
        Log.v(TAG_NAME, "onCloseReceived");
        //Server closing before client is always abnormal
        onException(new SocketException("Server closed socket unexpectedly."));
    }

    public void sendReview(Review review) {
        String json = gson.toJson(review);
        Log.d("JSON", json);
        send(json);
    }
}
