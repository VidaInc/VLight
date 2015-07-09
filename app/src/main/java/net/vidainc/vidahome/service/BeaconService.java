package net.vidainc.vidahome.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.aprilbrother.aprilbrothersdk.Beacon;
import com.aprilbrother.aprilbrothersdk.BeaconManager;
import com.aprilbrother.aprilbrothersdk.Region;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BeaconService extends Service {
    public BeaconService() {
    }

    private static final int REQUEST_ENABLE_BT = 1234;
    private static final String TAG = "BeaconList";
    // private static final Region ALL_BEACONS_REGION = new Region("apr",
    // "B9407F30-F5F8-466E-AFF9-25556B57FE6D",
    // null, null);
    private static final Region ALL_BEACONS_REGION = new Region("apr", null,
            null, null);
    //	private static final Region ALL_BEACONS_REGION = new Region("apr", "e2c56db5-dffb-48d2-b060-d0f5a71096e0",
//			985,211);
    //????uuid?"aa000000-0000-0000-0000-000000000000"?beacon
//	private static final Region ALL_BEACONS_REGION = new Region("apr", "aa000000-0000-0000-0000-000000000000",
//			null, null);
    private BeaconManager mBeaconManager;
    private ArrayList<Beacon> mBeacons;

    private BluetoothGatt mBluetoothGatt;
    public final static UUID BEACONSERVICEUUID = UUID
            .fromString("0000fab0-0000-1000-8000-00805f9b34fb");
    public final static UUID BEACONPROXIMITYUUID = UUID
            .fromString("0000fab1-0000-1000-8000-00805f9b34fb");
    public final static UUID BEACONMAJORUUID = UUID
            .fromString("0000fab2-0000-1000-8000-00805f9b34fb");
    public static final UUID CCCD = UUID
            .fromString("00002901-0000-1000-8000-00805f9b34fb");

    private BluetoothGattService RxService;
    private BluetoothGattCharacteristic RxChar;

    private long lastTime;
    private int i = 0;
    private byte[] a = {15, 50, 100, 0};


    private BluetoothGattCallback myGattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                            int newState) {
            Log.i(TAG, "connect newState = " + newState);
            showMessage("OnConnectionStateChange: " + newState);
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
            showMessage("OnServiceDis:" + status);
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
            showMessage("OnCharWrite:" + status);
            Log.i(TAG, "onCharacteristicWrite status = " + status);
            super.onCharacteristicWrite(gatt, characteristic, status);
        }

    };

    public void control(String macAdd) {
        Beacon beacon;
        BluetoothDevice myDevice;

        beacon = this.getBeaconByMacAdd("D0:FF:50:67:7C:4A");
//		beacon = this.getBeaconByMacAdd(macAdd);
//		try {
//			mBeaconManager.stopRanging(ALL_BEACONS_REGION);
//			mBeaconManager.disconnect();
//		} catch (RemoteException e) {
//			Log.d(TAG, "Error while stopping ranging", e);
//		}

        if (mBluetoothGatt != null) {
            mBluetoothGatt.disconnect();
            mBluetoothGatt.close();
        }
        showMessage("disconnected from beaconlist");


        myDevice = deviceFromBeacon(beacon);
        mBluetoothGatt = myDevice.connectGatt(this, false, myGattCallback);
        mBluetoothGatt.connect();
        mBluetoothGatt.discoverServices();
        System.out.println("ddebug about to write value to " + beacon.getMacAddress());
        try {
            Thread.sleep(2000);
            showMessage("sleeping");
        } catch (Exception e) {
            showMessage("sleeping exception");
        }


        i += 1;
        showMessage("writing a[i]:" + a[i] + " i:" + i);
        write(a[i]);
        if (i == 4) {
            i = 0;
        }
//		try{
//			Thread.sleep(5000);
//			showMessage("sleeping");
//		}catch(Exception e){
//
//		}
        if (mBluetoothGatt != null) {
            showMessage("disconnect from control");
            mBluetoothGatt.disconnect();
            mBluetoothGatt.close();
        }
        RxService = null;
        RxChar = null;

        connectToService();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


//	public boolean writeCharacteristic(byte[] value, UUID mService, UUID mCharacteristic) {
//		int tries = 10;
////
//		if (RxService == null) {
//			int i=0;
//			while(RxService == null && i < tries) {
//				i++;
//				RxService = mBluetoothGatt.getService(mService);
//				System.out.println("ddebug here RxService"+RxService.toString());
//			}
//
//		}
////		showMessage("mBluetoothGatt RxService null" + mBluetoothGatt);
//		if (RxService == null) {
//			showMessage("Rx service not found! in control");
//			return false;
//		}
//		if (RxChar == null){
//			int i=0;
//			while(RxChar == null && i < tries) {
//				i++;
//				RxChar = RxService.getCharacteristic(mCharacteristic);
//				System.out.println("ddebug here RxChar"+RxChar.getDescriptor(mCharacteristic));
//			}
//		}
//		if (RxChar == null) {
//			showMessage("Rx charateristic not found! in control");
//			return false;
//		}
//		RxChar.setValue(value);
//		showMessage("RxChar.setValue(value)" + value);
//
//		boolean status = mBluetoothGatt.writeCharacteristic(RxChar);
//		showMessage("status          " + status);
//
//
//
//		Log.d(TAG, "write TXchar - status=" + status);
//		try {
//			Thread.sleep(10);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//		return status;
//	}

    //DA original code
    public boolean writeCharacteristic(byte[] value, UUID mService,
                                       UUID mCharacteristic) {
//		if (RxService == null)
        RxService = mBluetoothGatt.getService(mService);
//		showMessage("mBluetoothGatt null" + mBluetoothGatt);
        if (RxService == null) {
            showMessage("Rx service not found!");
            return false;
        }
        showMessage("RxService retrieved");

//		if (RxChar == null)
        RxChar = RxService.getCharacteristic(mCharacteristic);
        if (RxChar == null) {
            showMessage("Rx characteristic not found!");
            return false;
        }
        showMessage("RxChar retrieved");
        RxChar.setValue(value);
        showMessage("RxChar.setValue(value)" + value[1]);
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

    private void init() {
        mBeacons = new ArrayList<>();

        mBeaconManager = new BeaconManager(this);
        mBeaconManager.setMonitoringExpirationMill(10L);
        mBeaconManager.setRangingExpirationMill(10L);
        mBeaconManager.setForegroundScanPeriod(200, 0);
        mBeaconManager.setRangingListener(new BeaconManager.RangingListener() {

            @Override
            public void onBeaconsDiscovered(Region region,
                                            final List<Beacon> beacons) {
                if (beacons != null && beacons.size() > 0) {
                    for (Beacon beacon : beacons) {
                        Log.d("beacon", "" + beacon.getMinor());
                        Log.d("beacon", "" + beacon.getMacAddress());
                        Log.d("beacon", "" + beacon.getName());
                        if (beacon.getName() != null && !mBeacons.contains(beacon)) {
                            mBeacons.add(beacon);
                        }
                    }
                }
            }
        });

        mBeaconManager.setMonitoringListener(new BeaconManager.MonitoringListener() {

            @Override
            public void onExitedRegion(Region arg0) {
                Toast.makeText(BeaconService.this, "????", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onEnteredRegion(Region arg0, List<Beacon> arg1) {
                Toast.makeText(BeaconService.this, "????", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Searching for beacon
     */
    private void connectToService() {
        mBeaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                try {
                    mBeaconManager.startRanging(ALL_BEACONS_REGION);
//					mBeaconManager.startMonitoring(ALL_BEACONS_REGION);
                } catch (RemoteException ignored) {
                }
            }
        });
    }

    @Override
    public void onCreate() {
        super.onCreate();
        init();
        // control();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            mBeacons.clear();
            mBeaconManager.stopRanging(ALL_BEACONS_REGION);
            mBeaconManager.disconnect();
        } catch (RemoteException e) {
            Log.d(TAG, "Error while stopping ranging", e);
        }

        if (mBluetoothGatt != null) {
            mBluetoothGatt.disconnect();
            mBluetoothGatt.close();
        }
        mBeaconManager.disconnect();
    }

    @SuppressLint("NewApi")
    private BluetoothDevice deviceFromBeacon(Beacon beacon) {
        BluetoothManager bluetoothManager = (BluetoothManager) this
                .getSystemService(BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        return bluetoothAdapter.getRemoteDevice(beacon.getMacAddress());
    }

    public Beacon getBeaconByMacAdd(String s) {
        //find from mac address
        for (Beacon bc : this.mBeacons) {
            if (bc.getMacAddress().equals(s)) {
                return bc;
            }
        }
        return null;
    }

    private void write(byte i) {
        showMessage("write value " + i);
        byte[] value = new byte[6];
        value[0] = 'e';
        value[1] =  i;
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
            return ;
        }
        if (RxChar == null)
            RxChar = RxService.getCharacteristic(mCharacteristic);
        if (RxChar == null) {
            showMessage("Rx charateristic not found!");
            return ;
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

    private void showMessage(String msg) {
        System.out.println("ddebug" + msg);
        Log.e(TAG, msg);
    }
}
