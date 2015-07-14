/*
 * Copyright (C) 2014 Wolfram Rittmeyer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.vidainc.vidahome.service;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import net.vidainc.vidahome.Constants;
import net.vidainc.vidahome.R;
import net.vidainc.vidahome.models.BeaconData;
import net.vidainc.vidahome.receivers.GcmBroadcastReceiver;

import org.json.JSONException;

import java.io.IOException;
import java.util.UUID;

import de.greenrobot.event.EventBus;

public class GcmIntentService extends IntentService {

    private NotificationManager mNotificationManager;
    private String mSenderId = null;
    private BluetoothDevice myDevice;
    private BluetoothGatt mBluetoothGatt;
    public final static UUID BEACONSERVICEUUID = UUID
            .fromString("0000fab0-0000-1000-8000-00805f9b34fb");
    public final static UUID BEACONPROXIMITYUUID = UUID
            .fromString("0000fab1-0000-1000-8000-00805f9b34fb");
    public final static UUID BEACONMAJORUUID = UUID
            .fromString("0000fab2-0000-1000-8000-00805f9b34fb");
    BluetoothGattService RxService;
    BluetoothGattCharacteristic RxChar;
    boolean isOn;

    public GcmIntentService() {
        super("GcmIntentService");
    }

    public boolean writeCharacteristic(byte[] value, UUID mService,
                                       UUID mCharacteristic) {
        if (RxService == null)
            RxService = mBluetoothGatt.getService(mService);

        if (RxService == null) {
            return false;
        }
        if (RxChar == null)
            RxChar = RxService.getCharacteristic(mCharacteristic);
        if (RxChar == null) {
            return false;
        }
        RxChar.setValue(value);
        return mBluetoothGatt.writeCharacteristic(RxChar);
    }

    private BluetoothGattCallback myGattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                            int newState) {
            Log.i("TAG", "connect newState = " + newState);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i("TAG", "Attempting to start service discovery:"
                        + mBluetoothGatt.discoverServices());
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i("TAG", "Disconnected from GATT server.");
            }
            super.onConnectionStateChange(gatt, status, newState);
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.i("TAG", "onServicesDiscovered status = " + status);
            super.onServicesDiscovered(gatt, status);
            if (isOn)
                write(255);
            else
                write(0);
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(GcmIntentService.this,
                            "DISCOVERED", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic, int status) {
            Log.i("TAG", "onCharacteristicWrite status = " + status);
            super.onCharacteristicWrite(gatt, characteristic, status);
        }

    };

    @SuppressLint("NewApi")
    private BluetoothDevice deviceFromBeaconData(BeaconData beaconData) {
        BluetoothManager bluetoothManager = (BluetoothManager) this
                .getSystemService(BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        return bluetoothAdapter.getRemoteDevice(beaconData.getBluetoothAddress());
    }

    private void write(int i) {
        byte[] value = new byte[6];
        value[0] = 'e';
        value[1] = (byte) i;
        value[2] = 1;
        value[3] = 1;
        value[4] = 1;
        value[5] = (byte) (value[1] ^ value[2] ^ value[3] ^ value[4]);
        writeCharacteristic(value, BEACONSERVICEUUID, BEACONPROXIMITYUUID);
        // mService.writeRXCharacteristic(value);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mSenderId = getResources().getString(R.string.gcm_project_id);
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);

        // action handling for actions of the activity
        String action = intent.getAction();
        switch (action) {
            case Constants.ACTION_REGISTER:
                register(gcm);
                break;
            case Constants.ACTION_UNREGISTER:
                unregister(gcm, intent);
                break;
            case Constants.ACTION_BEACON_DATA:
                sendBeaconData(gcm, intent);
                break;
        }

        // handling of stuff as described on
        // http://developer.android.com/google/gcm/client.html
        try {
            Bundle extras = intent.getExtras();
            // The getMessageType() intent parameter must be the intent you
            // received in your BroadcastReceiver.
            String messageType = gcm.getMessageType(intent);

            if (extras != null && !extras.isEmpty()) { // has effect of
                // unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that
             * GCM will be extended in the future with new message types, just
             * ignore any message types you're not interested in, or that you
             * don't recognize.
             */
                if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR
                        .equals(messageType)) {
                    sendNotification("Send error: " + extras.toString());
                } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED
                        .equals(messageType)) {
                    sendNotification("Deleted messages on server: "
                            + extras.toString());
                    // If it's a regular GCM message, do some work.
                } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE
                        .equals(messageType)) {
                    Log.d("BENCHMARK", "MESSAGE RECEIVED AT: " + System.nanoTime());
                    // Post notification of received message.
                    String msg = extras.getString("message");
                    if (myDevice == null) {
                        try {
                            BeaconData beaconData = BeaconData.listFromJsonString(msg).get(0);
                            myDevice = deviceFromBeaconData(beaconData);
                            mBluetoothGatt = myDevice.connectGatt(this, true, myGattCallback);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(GcmIntentService.this,
                                        "CREATED", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    if (TextUtils.isEmpty(msg)) {
                        msg = "empty message";
                    }
                    sendNotification(msg);
                    Log.i("vida_home", "Received: " + extras.toString()
                            + ", sent: " + msg);
                }
            }
        } finally {
            // Release the wake lock provided by the WakefulBroadcastReceiver.
            GcmBroadcastReceiver.completeWakefulIntent(intent);
        }
    }

    private void unregister(GoogleCloudMessaging gcm, Intent intent) {
        try {
            Log.v("vida_home", "about to unregister...");
            gcm.unregister();
            Log.v("vida_home", "device unregistered");

            // Persist the regID - no need to register again.
            removeRegistrationId();
            Bundle bundle = new Bundle();
            bundle.putInt(Constants.KEY_EVENT_TYPE,
                    Constants.EventbusMessageType.UNREGISTRATION_SUCCEEDED.ordinal());
            EventBus.getDefault().post(bundle);
        } catch (IOException e) {
            // If there is an error, don't just keep trying to register.
            // Require the user to click a button again, or perform
            // exponential back-off.

            // I simply notify the user:
            Bundle bundle = new Bundle();
            bundle.putInt(Constants.KEY_EVENT_TYPE,
                    Constants.EventbusMessageType.UNREGISTRATION_FAILED.ordinal());
            EventBus.getDefault().post(bundle);
            Log.e("vida_home", "Unregistration failed", e);
        }
    }

    private void register(GoogleCloudMessaging gcm) {
        try {
            Log.v("vida_home", "about to register...");
            String regid = gcm.register(mSenderId);
            Log.v("vida_home", "device registered: " + regid);

            String account = Settings.Secure.getString(getContentResolver(),
                    Settings.Secure.ANDROID_ID);
            sendRegistrationIdToBackend(gcm, regid, account);

            // Persist the regID - no need to register again.
            storeRegistrationId(regid);
            Bundle bundle = new Bundle();
            bundle.putInt(Constants.KEY_EVENT_TYPE,
                    Constants.EventbusMessageType.REGISTRATION_SUCCEEDED.ordinal());
            bundle.putString(Constants.KEY_REG_ID, regid);
            EventBus.getDefault().post(bundle);
        } catch (IOException e) {
            // If there is an error, don't just keep trying to register.
            // Require the user to click a button again, or perform
            // exponential back-off.

            // I simply notify the user:
            Bundle bundle = new Bundle();
            bundle.putInt(Constants.KEY_EVENT_TYPE,
                    Constants.EventbusMessageType.REGISTRATION_FAILED.ordinal());
            EventBus.getDefault().post(bundle);
            Log.e("vida_home", "Registration failed", e);
        }
    }

    private void storeRegistrationId(String regId) {
        final SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(this);
        Log.i("vida_home", "Saving regId to prefs: " + regId);
        Editor editor = prefs.edit();
        editor.putString(Constants.KEY_REG_ID, regId);
        editor.putInt(Constants.KEY_STATE, Constants.State.REGISTERED.ordinal());
        editor.commit();
    }

    private void removeRegistrationId() {
        final SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(this);
        Log.i("vida_home", "Removing regId from prefs");
        Editor editor = prefs.edit();
        editor.remove(Constants.KEY_REG_ID);
        editor.putInt(Constants.KEY_STATE, Constants.State.UNREGISTERED.ordinal());
        editor.commit();
    }

    private void sendRegistrationIdToBackend(GoogleCloudMessaging gcm,
                                             String regId, String account) {
        try {
            Bundle data = new Bundle();
            // the name is used for keeping track of user notifications
            // if you use the same name everywhere, the notifications will
            // be cancelled
            data.putString("account", account);
            data.putString("action", Constants.ACTION_REGISTER);
            String msgId = Integer.toString(getNextMsgId());
            gcm.send(mSenderId + "@gcm.googleapis.com", msgId,
                    Constants.GCM_DEFAULT_TTL, data);
            Log.v("vida_home", "regId sent: " + regId);
        } catch (IOException e) {
            Log.e("vida_home",
                    "IOException while sending registration to backend...", e);
        }
    }

    private void sendBeaconData(GoogleCloudMessaging gcm, Intent intent) {
        try {
            String msg = intent.getStringExtra(Constants.KEY_MESSAGE_TXT);
            Bundle data = new Bundle();
            data.putString(Constants.ACTION, Constants.ACTION_BEACON_DATA);
            data.putString("message", msg);
            String id = Integer.toString(getNextMsgId());
            Log.d("BENCHMARK", "MESSAGE SENT AT: " + System.nanoTime());
            gcm.send(mSenderId + "@gcm.googleapis.com", id, data);
            Log.v("vida_home", "sent message: " + msg);
        } catch (IOException e) {
            Log.e("Vida_home", "Error while sending a message", e);
        }
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(String msg) {
        mNotificationManager = (NotificationManager) this
                .getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("GCM Notification")
                .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                .setContentText(msg);

        mNotificationManager.notify(Constants.NOTIFICATION_NR, mBuilder.build());
    }

    private int getNextMsgId() {
        SharedPreferences prefs = getPrefs();
        int id = prefs.getInt(Constants.KEY_MSG_ID, 0);
        Editor editor = prefs.edit();
        editor.putInt(Constants.KEY_MSG_ID, ++id);
        editor.commit();
        return id;
    }

    private SharedPreferences getPrefs() {
        return PreferenceManager.getDefaultSharedPreferences(this);
    }
}
