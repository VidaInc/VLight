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
    private BeaconManager beaconManager;
    private volatile Handler mHandler;
    private boolean training = false;
    private volatile int pos;
    private final double[] theta = {0.177362342745932,
            -0.00948387473371134,
            -0.00892526060630129,
            -0.0613012225276673,
            -0.0109070168168383,
            -0.0106584336464829,
            -0.0238932067460397,
            -0.00858778235973839,
            -0.0200640501214532,
            -0.0286185892536547,
            -0.00434652885702610,
            -0.00209416819511826,
            -0.00817408307719628,
            -0.00375848704359208,
            -0.00358681623113955,
            -0.00791252123727548,
            -0.00294595750123593,
            -0.00314842011485157,
            -0.00592497098210962,
            -0.00798118685807685,
            -0.000889879147863026,
            0.000795498326010308,
            -0.00196932131482547,
            -8.32338520666585e-05,
            2.05619637346746e-05,
            -0.00191108745497298,
            -0.000924781445845410,
            0.000641377974955719,
            0.000130163486094343,
            -0.00119735676319867,
            -0.000954534590632203,
            0.000186831819765894,
            -0.000201784323139331,
            -0.000467961344216356,
            -0.00143541055314418,
            6.84166221025518e-05,
            0.00106336534105902,
            1.67414566562315e-05,
            0.000497355346305160,
            0.000475525257564166,
            -0.000163463280440217,
            0.000187494463453873,
            0.000738134579685657,
            0.000544338556704355,
            5.63578334452534e-05,
            -0.000294342036278243,
            0.000661929246488227,
            0.000678920526341073,
            0.000655817716279891,
            0.000196615130408280,
            -0.000622846514296990,
            0.000240415377218872,
            0.000214902058774400,
            0.000299810668540878,
            0.000339870137839195,
            -0.000322779258409821,
            -0.000163083909259562,
            0.000342856760339601,
            0.000220911513761782,
            -3.74139377128906e-05,
            -0.000172315523637400,
            -8.47039469411833e-05,
            -8.07140425564795e-05,
            -6.73737685924112e-05,
            -0.000122534250615342,
            -7.39589941248457e-05,
            -4.47884532899315e-05,
            5.52829530999633e-05,
            5.22894429444150e-07,
            -1.93391580882415e-05,
            -2.12390657850743e-05,
            -0.000110834201549041,
            5.48089489388649e-05,
            -7.44073503856315e-06,
            -1.02153094357376e-05,
            -2.24705023714861e-05,
            9.02898750501079e-06,
            5.56511667410644e-05,
            2.10154777677080e-05,
            -6.79103150411068e-05,
            -5.74632193170123e-05,
            -2.43453925249629e-05,
            -2.08306290942832e-05,
            1.76228946500166e-05,
            -0.0115989292654284,
            -0.0119365748290792,
            -0.0215310239351622,
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
                        for (int i = 0; i < 91; i++) {
                            z += features[i] * theta[i];
                        }
                        final double certainty = sigmoid(z);
                        final boolean inRoom = certainty > 0.5;
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
        double[] out = new double[93];
        System.arraycopy(features, 3, out, 84, 9);
        int pos = 0;
        out[pos] = 1;
        for (int i = 1; i <= 6; i++) {
            for (int j = 0; j <= i; j++) {
                for (int k = 0; k <= j; k++) {
                    out[++pos] =
                            FastMath.pow(x1, i - j) * FastMath.pow(x2, j - k) * FastMath.pow(x3, k);
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
