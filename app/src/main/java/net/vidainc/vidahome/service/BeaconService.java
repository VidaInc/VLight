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
    private final double[] theta = {0.468911760641924,
            0.0253078120412154,
            0.0893917752371334,
            -0.144413313214988,
            -0.0647937182535065,
            0.0166672988136377,
            -0.158916230735999,
            0.0765139534697612,
            -0.0310143666764122,
            -0.196266199948791,
            -0.102170871269151,
            -0.0235900565469994,
            -0.131250152149365,
            0.00302484126069016,
            -0.0501178520084614,
            -0.123589567678203,
            0.0295083822669376,
            0.0141588320049618,
            -0.0326395525734895,
            -0.0904913181092089,
            -0.0659488637091351,
            -0.0130114045348839,
            -0.0686699595961154,
            -0.0122750382184766,
            -0.0382111186877080,
            -0.0583701079797298,
            -0.00178094874654884,
            -0.00436703312571950,
            -0.0254265496376209,
            -0.0365269097638787,
            0.00564448408152303,
            0.0127058157436064,
            0.00293755867972040,
            -0.0111525202735888,
            -0.0198673507798012,
            0.0143437008874961,
            0.0243121765173170,
            -6.72291691879858e-05,
            0.00482867401377030,
            -0.00414311429811783,
            -0.00158634390678137,
            -0.000501822625538651,
            0.00577989536469710,
            -0.00306675407975270,
            0.00430039044294505,
            -0.00318927425194603,
            0.00626781802006138,
            0.00332802069364097,
            -0.00123629805702700,
            0.00607340730422167,
            -0.00474634864829963,
            0.00287181921942395,
            0.00225467414815961,
            0.000470624319979251,
            -0.00312213113510719,
            0.00386843126988525,
            -0.199051257910636,
            -0.230256142810304,
            -0.230013897151313,
            -0.00197787327367626,
            -0.00237551740766969,
            0.00128607159193201,
            0.00125781079630418,
            -0.00433988434283404,
            -0.00185691699201435,
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
        beaconManager.setForegroundScanPeriod(5000);
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
                if (beacons.size() > 0 && (!training || pos < 200)) {
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
                        values[12] = pos++ < 100 ? 1 : 0;
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

    public void saveTextFile(String content) {

        FileOutputStream fileOutputStream = null;

        try {
            fileOutputStream = openFileOutput("BEACON_DATA", Context.MODE_APPEND);
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
