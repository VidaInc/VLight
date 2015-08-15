package net.vidainc.vidahome.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.widget.Toast;

import net.vidainc.vidahome.Constants;
import net.vidainc.vidahome.R;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.apache.commons.math3.util.FastMath;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;


public class BeaconService extends Service implements BeaconConsumer {
    public static final Region ALL_BEACONS_REGION = new Region("apr", null,
            null, null);
    public static final int NUM_OF_FEATURES = 71;
    private BeaconManager beaconManager;
    private volatile Handler mHandler;
    private boolean training = false;
    private volatile int pos;
    private final double[] theta = {0.103173769743789,
            -0.101984273679479,
            0.00396318221266563,
            0.0432714631647556,
            -0.125851049794661,
            -0.0668147847642316,
            -0.0644593363920503,
            0.00302054391923662,
            0.00981250523782724,
            0.0253253890696688,
            -0.0745305528872685,
            -0.0618005169094821,
            -0.0635700337573497,
            -0.0205661413695518,
            -0.0281109640367432,
            -0.0312055472060439,
            0.00546215799023319,
            0.00722130706850736,
            0.0120111530760539,
            0.0163006823740879,
            -0.0187975895861915,
            -0.0367268452939406,
            -0.0220586089328664,
            -0.0164845725187462,
            -0.0257285933035895,
            -0.0213490382617184,
            -0.000453603348008487,
            -0.00213590236303126,
            -0.00623439085090089,
            -0.00945370950839737,
            0.00203491896602619,
            0.00332912949223779,
            0.00496838411947537,
            0.00819341160570178,
            0.00589494037492344,
            0.00417876479716244,
            -0.00783244424884796,
            0.00198020494019314,
            0.00108687010944410,
            -0.00748155290104922,
            0.000177751590426689,
            0.00366578704437762,
            0.00259836893801316,
            -0.00363045075660319,
            -0.000373993096220321,
            0.000132505318567856,
            0.00657503202342426,
            0.00860917412875260,
            0.00618808464149139,
            0.00303577873832059,
            -0.00162070466789109,
            -0.000723977571505028,
            0.000156861876544903,
            0.00109368280260619,
            0.00263473152603647,
            -0.00281407535121060,
            -0.0480346434074234,
            -0.0481469609742585,
            -0.0481060117671548,
            0.00107422215807663,
            -0.00232894188813344,
            -0.00117427818006760,
            -0.000208869425892304,
            0.000943323499752048,
            0.000453596610008821,
            0,
            0,
            0,
            0,
            0,
            0};

    public BeaconService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        beaconManager = BeaconManager.getInstanceForApplication(getApplicationContext());
        //BeaconManager.setBeaconSimulator(new TimedBeaconSimulator());
        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout(getString(R.string.ibeacon_layout)));
        beaconManager.bind(this);
        //beaconManager.setDebug(true);
        beaconManager.setForegroundScanPeriod(2000);
        mHandler = new Handler();
        Intent regIntent = new Intent(this, GcmIntentService.class);
        regIntent.setAction(Constants.ACTION_REGISTER);
        startService(regIntent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onBeaconServiceConnect() {
        Toast.makeText(BeaconService.this, "Beacon service connected", Toast.LENGTH_SHORT).show();
        beaconManager.setMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(BeaconService.this, "Entered Region", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void didExitRegion(Region region) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(BeaconService.this, "Left Region", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void didDetermineStateForRegion(int state, Region region) {
            }
        });

        try {
            beaconManager.startMonitoringBeaconsInRegion(ALL_BEACONS_REGION);
        } catch (RemoteException ignored) {
        }
        beaconManager.setRangeNotifier(new RangeNotifier() {

            @Override
            public synchronized void didRangeBeaconsInRegion(final Collection<Beacon> beacons, Region region) {
                if (beacons.size() > 0 && (!training || pos < 400)) {
                    //beacons.iterator().next().getDistance()
                    //beaconManager.setBackgroundMode(true);
                    final double[] values = training ? new double[13] : new double[12];
                    for (Beacon beacon : beacons) {
                        if (beacon.getBluetoothAddress().endsWith("4A")) {
                            values[0] = beacon.getDistance();
                            values[3] = beacon.getRssi();
                        } else if (beacon.getBluetoothAddress().endsWith("53")) {
                            values[1] = beacon.getDistance();
                            values[4] = beacon.getRssi();
                        } else {
                            values[2] = beacon.getDistance();
                            values[5] = beacon.getRssi();
                        }
                    }
                    if (training) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(BeaconService.this, "Pos : " + pos,
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                        values[12] = pos++ < 200 ? 1 : 0;
                        saveTextFile(Arrays.toString(values) + "\n");
                    } else {
                        final double[] features = mapFeature(values);
                        double z = 0;
                        for (int i = 0; i < NUM_OF_FEATURES; i++) {
                            z += features[i] * theta[i];
                        }
                        final double certainty = sigmoid(z);
                        final boolean inRoom = certainty >= 0.5;
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(BeaconService.this, "In room? " + inRoom +
                                                " (" + (inRoom ? (certainty * 100) : ((1 - certainty)) * 100) + "%)",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

//                    Intent msgIntent = new Intent(BeaconService.this, GcmIntentService.class);
//                    msgIntent.setAction(Constants.ACTION_BEACON_DATA);
//                    try {
//                        msgIntent.putExtra(Constants.KEY_MESSAGE_TXT,
//                                BeaconData.toJsonString(BeaconData.fromBeacons(beacons)));
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                    startService(msgIntent);
                }
            }
        });

        try {
            beaconManager.startRangingBeaconsInRegion(ALL_BEACONS_REGION);
        } catch (RemoteException ignored) {
        }
    }

    private double[] mapFeature(double[] features) {
        double x1 = features[0];
        double x2 = features[1];
        double x3 = features[2];
        double x4 = features[3];
        double x5 = features[4];
        double x6 = features[5];
        double[] out = new double[NUM_OF_FEATURES];
        System.arraycopy(features, 6, out, 65, 6);
        int pos = 0;
        out[pos] = 1;
        for (int i = 1; i <= 5; i++) {
            for (int j = 0; j <= i; j++) {
                for (int k = 0; k <= j; k++) {
                    out[++pos] =
                            FastMath.pow(x1, i - j) * FastMath.pow(x2, j - k) * FastMath.pow(x3, k);
                }
            }
        }
        for (int i = 1; i <= 2; i++) {
            for (int j = 0; j <= i; j++) {
                for (int k = 0; k <= j; k++) {
                    out[++pos] =
                            FastMath.pow(x4, i - j) * FastMath.pow(x5, j - k) * FastMath.pow(x6, k);
                }
            }
        }
        return out;
    }

    private double sigmoid(double z) {
        return 1 / (1 + FastMath.exp(-z));
    }

    public synchronized void saveTextFile(String content) {

        FileOutputStream fileOutputStream = null;

        try {
            fileOutputStream = openFileOutput("data.txt", Context.MODE_APPEND);
            byte[] bytes = content.getBytes();
            fileOutputStream.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}
