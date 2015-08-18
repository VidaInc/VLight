package net.vidainc.vidahome.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.widget.Toast;

import com.jmatio.io.MatFileReader;
import com.jmatio.types.MLDouble;

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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;


public class BeaconService extends Service implements BeaconConsumer {
    public static final Region ALL_BEACONS_REGION = new Region("apr", null,
            null, null);
    private static final int NUM_OF_ROOMS = 4;
    private static double[][] theta1;
    private static double[][] theta2;
    private BeaconManager beaconManager;
    private volatile Handler mHandler;
    private boolean training = false;
    private volatile int pos;

    public BeaconService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        File dir = Environment.getExternalStorageDirectory();
        File thetas = new File(dir, "thetas.mat");
        MatFileReader matfilereader;
        try {
            matfilereader = new MatFileReader(thetas);
            theta1 = ((MLDouble) matfilereader.getMLArray("Theta1")).getArray();
            theta2 = ((MLDouble) matfilereader.getMLArray("Theta2")).getArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                        if (pos < 100) {
                            values[12] = 1;
                        } else if (pos < 200) {
                            values[12] = 2;
                        } else if (pos < 300) {
                            values[12] = 3;
                        } else {
                            values[12] = 4;
                        }
                        pos++;
                        saveTextFile(Arrays.toString(values) + "\n");
                    } else {
                        final double[] features = mapFeature(values);
                        final int room = predict(features);
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(BeaconService.this, "In room " + room,
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

    private static double[] mapFeature(double[] features) {
        double x1 = features[0];
        double x2 = features[1];
        double x3 = features[2];
        double[] out = new double[28];
        System.arraycopy(features, 3, out, 19, 9);
        int pos = 0;
        for (int i = 1; i <= 3; i++) {
            for (int j = 0; j <= i; j++) {
                for (int k = 0; k <= j; k++) {
                    out[pos++] =
                            FastMath.pow(x1, i - j) * FastMath.pow(x2, j - k) * FastMath.pow(x3, k);
                }
            }
        }
        return out;
    }

    private static int predict(double[] features) {
        double[] actv1 = new double[features.length + 1];
        double[] actv2 = new double[theta1.length + 1];
        double[] sigmoid = new double[theta1.length];
        double[] hyp = new double[NUM_OF_ROOMS];
        actv1[0] = 1;
        actv2[0] = 1;
        System.arraycopy(features, 0, actv1, 1, features.length);
        for (int i = 0; i < theta1.length; i++) {
            double z = 0;
            for (int j = 0; j < theta1[0].length; j++) {
                z += theta1[i][j] * actv1[j];
            }
            sigmoid[i] = sigmoid(z);
        }
        System.arraycopy(sigmoid, 0, actv2, 1, sigmoid.length);
        for (int i = 0; i < theta2.length; i++) {
            double z = 0;
            for (int j = 0; j < theta2[0].length; j++) {
                z += theta2[i][j] * actv2[j];
            }
            hyp[i] = sigmoid(z);
        }
        int max = 0;
        for (int i = 0; i < hyp.length; i++) {
            if (hyp[i] > hyp[max])
                max = i;
        }
        return max + 1;
    }

    private static double sigmoid(double z) {
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
