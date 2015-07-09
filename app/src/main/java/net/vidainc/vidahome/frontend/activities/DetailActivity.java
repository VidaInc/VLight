package net.vidainc.vidahome.frontend.activities;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.aprilbrother.aprilbrothersdk.Beacon;

import net.vidainc.vidahome.R;

import java.util.UUID;

/**
 * Created by Aaron on 08/07/2015.
 */
public class DetailActivity extends BaseActivity {
    private Beacon beacon;
    private BluetoothDevice myDevice;
    private BluetoothGatt mBluetoothGatt;
    public final static UUID BEACONSERVICEUUID = UUID
            .fromString("0000fab0-0000-1000-8000-00805f9b34fb");
    public final static UUID BEACONPROXIMITYUUID = UUID
            .fromString("0000fab1-0000-1000-8000-00805f9b34fb");
    public final static UUID BEACONMAJORUUID = UUID
            .fromString("0000fab2-0000-1000-8000-00805f9b34fb");
    private static final String TAG = "DetailActivity";

    public static final UUID CCCD = UUID
            .fromString("00002901-0000-1000-8000-00805f9b34fb");

    private long lastTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail);
        init();

    }

    private void init() {
        Bundle bundle = getIntent().getExtras();
        beacon = bundle.getParcelable("beacon");
        myDevice = deviceFromBeacon(beacon);
//		tv = (TextView) findViewById(R.id.tv_connect);
        mBluetoothGatt = myDevice.connectGatt(this, false, myGattCallback);
        SeekBar sb = (SeekBar) findViewById(R.id.seekBar1);
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                write(seekBar.getProgress());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
//				long nowTime = System.currentTimeMillis();
//				if (nowTime - lastTime > 300) {
//					write(progress);
//					lastTime = nowTime;
//				}
            }
        });
    }

    public void connect(View view) {

    }

    BluetoothGattService RxService;
    BluetoothGattCharacteristic RxChar;

    public boolean writeCharacteristic(byte[] value, UUID mService,
                                       UUID mCharacteristic) {
        if (RxService == null)
            RxService = mBluetoothGatt.getService(mService);

        showMessage("mBluetoothGatt null" + mBluetoothGatt);
        if (RxService == null) {
            showMessage("Rx service not found!");
            return false;
        }
        if (RxChar == null)
            RxChar = RxService.getCharacteristic(mCharacteristic);
        if (RxChar == null) {
            showMessage("Rx characteristic not found!");
            return false;
        }
        RxChar.setValue(value);
        showMessage("RxChar.setValue(value)" + value);
        boolean status = mBluetoothGatt.writeCharacteristic(RxChar);
        showMessage("status          " + status);

        Log.d(TAG, "write TXchar - status=" + status);
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return status;
    }

    private void showMessage(String msg) {
        System.out.println("ddebug DA:" + msg);
        Log.e(TAG, msg);
        System.out.println("ddebug" + msg);
    }

    private BluetoothGattCallback myGattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                            int newState) {
            Log.i(TAG, "connect newState = " + newState);
            showMessage("Status Change" + newState);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i(TAG, "Attempting to start service discovery:"
                        + mBluetoothGatt.discoverServices());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i(TAG, "Disconnected from GATT server.");
            }
            super.onConnectionStateChange(gatt, status, newState);
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            showMessage("Service Discovered!");
            Log.i(TAG, "onServicesDiscovered status = " + status);
//			DetailActivity.this.runOnUiThread(new Runnable() {
//
//				@Override
//				public void run() {
//					tv.setText("disconnect");
//				}
//			});
//			enableTXNotification();
            super.onServicesDiscovered(gatt, status);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic, int status) {
            showMessage("Writing status:" + status);
            Log.i(TAG, "onCharacteristicWrite status = " + status);
            super.onCharacteristicWrite(gatt, characteristic, status);
        }

    };
    private TextView tv;

    @SuppressLint("NewApi")
    private BluetoothDevice deviceFromBeacon(Beacon beacon) {
        BluetoothManager bluetoothManager = (BluetoothManager) this
                .getSystemService(BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        return bluetoothAdapter.getRemoteDevice(beacon.getMacAddress());
    }

    private void write(int i) {
        showMessage("write value " + i);
        byte[] value = new byte[6];
        value[0] = 'e';
        value[1] = (byte) i;
        value[2] = 1;
        value[3] = 1;
        value[4] = 1;
        value[5] = (byte) (value[1] ^ value[2] ^ value[3] ^ value[4]);
        writeCharacteristic(value, BEACONSERVICEUUID, BEACONPROXIMITYUUID);
        // mService.writeRXCharacteristic(value);
//		writeCharacteristicDis(value,BEACONSERVICEUUID, BEACONPROXIMITYUUID);
    }

    public void writeCharacteristicDis(byte[] value, UUID mService,
                                       UUID mCharacteristic) {
        if (RxService == null)
            RxService = mBluetoothGatt.getService(mService);

        showMessage("mBluetoothGatt null" + mBluetoothGatt);
        if (RxService == null) {
            showMessage("Rx service not found!");
            return;
        }
        if (RxChar == null)
            RxChar = RxService.getCharacteristic(mCharacteristic);
        if (RxChar == null) {
            showMessage("Rx charateristic not found!");
            return;
        }
        BluetoothGattDescriptor descriptor = RxChar.getDescriptor(CCCD);
        Log.i(TAG, "getDescriptor");
        descriptor.setValue(value);
        Log.i(TAG, "setValue");
        boolean writeDescriptor = mBluetoothGatt.writeDescriptor(descriptor);
        Log.i(TAG, "writeDescriptor TXchar - status=" + writeDescriptor);
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void click1(View view) {
        write(0);
    }

    public void click2(View view) {
        write(123);
    }

    public void click3(View view) {
        write(255);
    }

    public void back(View view) {
        finish();
    }

    public void enableTXNotification() {
        /*
		 * if (mBluetoothGatt == null) { showMessage("mBluetoothGatt null" +
		 * mBluetoothGatt); broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
		 * return; }
		 */
//		List<BluetoothGattService> services = mBluetoothGatt.getServices();
//		Log.i("Test", "services.size = " + services.size());
        Log.i(TAG, "writeDescriptor TXchar - status=");
        BluetoothGattService RxService = mBluetoothGatt
                .getService(BEACONSERVICEUUID);
        if (RxService == null) {
            showMessage("Rx service not found!");
            return;
        }
        Log.i(TAG, "RxService != null");
        BluetoothGattCharacteristic TxChar = RxService
                .getCharacteristic(BEACONPROXIMITYUUID);
        if (TxChar == null) {
            showMessage("Tx charateristic not found!");
            return;
        }
        Log.i(TAG, "TxChar != null");

        mBluetoothGatt.setCharacteristicNotification(TxChar, true);
        Log.i(TAG, "setCharacteristicNotification");
        BluetoothGattDescriptor descriptor = TxChar.getDescriptor(CCCD);
        Log.i(TAG, "getDescriptor");
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        Log.i(TAG, "setValue");
        boolean writeDescriptor = mBluetoothGatt.writeDescriptor(descriptor);
        Log.i(TAG, "writeDescriptor TXchar - status=" + writeDescriptor);
    }

    @Override
    protected void onStop() {
        if (mBluetoothGatt != null) {
            showMessage("close");
            mBluetoothGatt.disconnect();
            mBluetoothGatt.close();
        }
        super.onStop();
    }
}
