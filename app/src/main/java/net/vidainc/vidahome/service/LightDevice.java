package net.vidainc.vidahome.service;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Looper;
import android.util.Log;

import java.util.UUID;

/**
 * Created by Aaron on 16/07/2015.
 */
public class LightDevice {
    private final static UUID BEACON_SERVICE_UUID = UUID
            .fromString("0000fab0-0000-1000-8000-00805f9b34fb");
    private final static UUID LIGHT_CHARACTERISTIC_UUID = UUID
            .fromString("0000fab1-0000-1000-8000-00805f9b34fb");
    private BluetoothDevice myDevice;
    private BluetoothGatt mBluetoothGatt;
    private BluetoothGattService mRxService;
    private BluetoothGattCharacteristic mRxChar;
    private int mState;
    private int mStatus;
    private byte lightValue;

    public LightDevice(Context context, final OnLightDeviceConnectedListener
            onLightDeviceConnectedListener, String macAddress) {
        BluetoothManager bluetoothManager = (BluetoothManager) context
                .getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        myDevice = bluetoothAdapter.getRemoteDevice(macAddress);
        BluetoothGattCallback myGattCallback = new BluetoothGattCallback() {

            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                                int newState) {
                Log.i("TAG", "connect newState = " + newState);
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.i("TAG", "Attempting to start service discovery:"
                            + mBluetoothGatt.discoverServices());
                    onLightDeviceConnectedListener.onConnected();
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.i("TAG", "Disconnected from GATT server.");
                }
                super.onConnectionStateChange(gatt, status, newState);
                mStatus = status;
                mState = newState;
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, final int status) {
                Log.i("TAG", "onServicesDiscovered status = " + status);
                super.onServicesDiscovered(gatt, status);
                mStatus = status;
            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt,
                                              BluetoothGattCharacteristic characteristic, int status) {
                Log.i("TAG", "onCharacteristicWrite status = " + status);
                super.onCharacteristicWrite(gatt, characteristic, status);
                mStatus = status;
            }

        };
        mBluetoothGatt = myDevice.connectGatt(context, true, myGattCallback);
    }

    public int getState() {
        return mState;
    }

    public int getStatus() {
        return mStatus;
    }

    public String getMacAddress() {
        return myDevice.getAddress();
    }

    public boolean writeToLight(byte b) {
        //stop scanning
        lightValue = b;
        if(Looper.myLooper() == Looper.getMainLooper())
            throw new IllegalThreadStateException("Cannot call from main thread!");
        if (mStatus != BluetoothGatt.GATT_SUCCESS)
            throw new IllegalStateException("Not connected");
        byte[] value = new byte[6];
        value[0] = 'e';
        value[1] = b;
        value[2] = 1;
        value[3] = 1;
        value[4] = 1;
        value[5] = (byte) (value[1] ^ value[2] ^ value[3] ^ value[4]);

        if (mRxService == null)
            mRxService = mBluetoothGatt.getService(BEACON_SERVICE_UUID);
        if (mRxService == null)
            return false;

        if (mRxChar == null)
            mRxChar = mRxService.getCharacteristic(LIGHT_CHARACTERISTIC_UUID);
        if (mRxChar == null)
            return false;

        mRxChar.setValue(value);
        long startTime = System.nanoTime();
        boolean succeeded;
        do{
            succeeded = mBluetoothGatt.writeCharacteristic(mRxChar);
        }while (!succeeded && System.nanoTime() - startTime < 5000000000L);


        // resume scanning
        return succeeded;
    }
public byte getLightValue(){
    return lightValue;
}
    public interface OnLightDeviceConnectedListener {
        void onConnected();
    }
}
