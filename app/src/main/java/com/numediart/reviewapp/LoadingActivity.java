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

package com.numediart.reviewapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.numediart.reviewapp.database.FloorsDatabase;
import com.numediart.reviewapp.network.PongListener;
import com.numediart.reviewapp.network.PongWebSocketClient;

import java.util.Locale;

import tech.gusavila92.websocketclient.exceptions.InvalidServerHandshakeException;

public class LoadingActivity extends AppCompatActivity implements View.OnClickListener, PongListener {

    private EditText userName, ipServer, portServer, sessionKey;
    private Button connectButton;
    private LinearLayout loadingPanel;
    private PongWebSocketClient websocket;

    private static final int REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_loading);

        userName = findViewById(R.id.userName);
        ipServer = findViewById(R.id.ipAddress);
        portServer = findViewById(R.id.portAddress);
        sessionKey = findViewById(R.id.sessionKey);

        connectButton = findViewById(R.id.nextButton);
        connectButton.setOnClickListener(this);

        loadingPanel = findViewById(R.id.loadingPanel);

        setPreferences();
        ensurePermissions();
    }

    protected void onResume() {
        super.onResume();
        editFields(true);
    }

    private void setPreferences() {
        SharedPreferences preferences = getSharedPreferences(Utils.PREF_NAME, MODE_PRIVATE);

        ipServer.setText(preferences.getString(Utils.SERVER_IP, null));
        userName.setText(preferences.getString(Utils.USER_NAME, null));
        int port = preferences.getInt(Utils.SERVER_PORT, -1);
        if(port != -1) {
            portServer.setText(String.format(Locale.getDefault(), "%d", port));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle state){
        state.putString(Utils.USER_NAME, userName.getText().toString());
        state.putString(Utils.SERVER_IP, ipServer.getText().toString());
        int port = -1;
        try{
            port = Integer.parseInt(portServer.getText().toString());
        }
        catch(NumberFormatException e) {
            // Do nothing
        }
        state.putInt(Utils.SERVER_PORT, port);
        state.putString(Utils.SESSION_KEY, sessionKey.getText().toString());

        super.onSaveInstanceState(state);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        userName.setText(savedInstanceState.getString(Utils.USER_NAME));
        ipServer.setText(savedInstanceState.getString(Utils.SERVER_IP));
        portServer.setText(String.format(Locale.getDefault(), "%d", savedInstanceState.getInt(Utils.SERVER_PORT)));
        sessionKey.setText(savedInstanceState.getString(Utils.SESSION_KEY));
    }

    public void onClick(View v) {
        editFields(false);
        if(v.getId() == R.id.nextButton) {
            int result = isConfigurationOK();
            if (result != 0) {
                printErrorMsg(-result);
                editFields(true);
                return;
            }

            SharedPreferences preferences = getSharedPreferences(Utils.PREF_NAME, MODE_PRIVATE);
            preferences.edit().putString(Utils.USER_NAME, userName.getText().toString())
                    .putString(Utils.SERVER_IP, ipServer.getText().toString())
                    .putInt(Utils.SERVER_PORT, Integer.parseInt(portServer.getText().toString()))
                    .putString(Utils.SESSION_KEY, sessionKey.getText().toString())
                    .apply();

            websocket = new PongWebSocketClient(Utils.getUriBase(this), this);
            websocket.connect();
            websocket.sendPing(null);
        }
    }

    private void editFields(boolean editable) {
        userName.setEnabled(editable);
        ipServer.setEnabled(editable);
        portServer.setEnabled(editable);
        sessionKey.setEnabled(editable);
        connectButton.setEnabled(editable);
        if(editable) {
            connectButton.setText(R.string.config_button);
            loadingPanel.setVisibility(View.INVISIBLE);
        } else {
            connectButton.setText(R.string.connect_button);
            loadingPanel.setVisibility(View.VISIBLE);
        }
    }

    private void ensurePermissions() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_CODE_WRITE_EXTERNAL_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.d("REQUEST CODE", "Value: " + requestCode);
        if (requestCode == REQUEST_CODE_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length == 0
                    || grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, R.string.storage_permission_denied_message,
                        Toast.LENGTH_LONG).show();
                finish();
            } else {
                ensurePermissions();
            }
        }
    }

    public void printErrorMsg(int errorCode) {
        if(errorCode <= 0) return;

        int errorId = 0;
        switch(errorCode) {
            case 1 :
                errorId = R.string.port_error;
                break;
            case 2 :
                errorId = R.string.empty_fields_error;
                break;
            case 3 :
                errorId = R.string.invalid_session_key;
                break;
            case 4 :
                errorId = R.string.server_unreachable;
                break;
        }

        Toast toast = Toast.makeText(getApplicationContext(), errorId, Toast.LENGTH_SHORT);
        toast.show();
    }

    private int isConfigurationOK() {
        int port;
        try{
            port = Integer.parseInt(portServer.getText().toString());
        } catch(NumberFormatException e) {
            return -1;
        }

        if(userName.getText().toString().equals("")
                || ipServer.getText().toString().equals("")
                || portServer.getText().toString().equals("")
                || sessionKey.getText().toString().equals("")) {
            return -2;
        }

        if(port < 0) {
            return -1;
        }

        return 0;
    }

    @Override
    public void onPongReceived() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                websocket.close();
                launch(false);
            }
        });
    }

    public void launch(boolean offline_mode) {
        Intent intent = new Intent(LoadingActivity.this, RecapActivity.class);
        intent.putExtra(Utils.OFFLINE_MODE, offline_mode);
        startActivity(intent);
        finish();
    }

    @Override
    public void onPongError(Exception e) {
        final int errorCode;
        if(e instanceof InvalidServerHandshakeException) {
            errorCode = 3;
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    printErrorMsg(errorCode);
                    editFields(true);
                }
            });
        } else if(FloorsDatabase.existFloors(this)){
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    askOfflineMode();
                }
            });
        } else {
            errorCode = 4;
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    printErrorMsg(errorCode);
                    editFields(true);
                }
            });
        }
    }

    private void askOfflineMode() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.offline_title)
                .setMessage(R.string.force_offline)

                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        editFields(true);
                    }
                })

                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        launch(true);
                    }
                })

                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

}
