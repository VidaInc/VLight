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
import org.apache.commons.math3.analysis.function.Sigmoid;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;


public class BeaconService extends Service implements BeaconConsumer {
    public static final Region ALL_BEACONS_REGION = new Region("apr", null,
            null, null);
    private BeaconManager beaconManager;
    private Handler mHandler;
    private boolean training = false;
    private volatile int pos;
    private final double[] theta = {1.94081909218395,
            0.339058904936108,
            0.147966974249590,
            0.151488436608353,
            0.0512153513666704,
            0.0119592954490875,
            -0.00905754300953639,
            -0.0146702129322610,
            -0.00503690910646743,
            -0.0139296614558687,
            -0.00295732280081006,
            -0.00772218960444187,
            -0.0166994299356045,
            -0.00982096063212688,
            -0.00555183282500362,
            -0.0151618367136793,
            -0.0103057497785731,
            -0.00668262526968492,
            -0.00736990290291442,
            -0.00849624943928168,
            -0.00545923956554038,
            -0.00410379883982758,
            -0.00830856034358211,
            -0.00451066394733749,
            -0.00230250025432235,
            -0.00587119160740034,
            -0.00249320735245676,
            -0.000962649856581411,
            -0.00151744792712915,
            -0.00309974455840671,
            -0.00184957590414358,
            -0.000782288157351912,
            -0.00231854356418205,
            -0.00112077510456022,
            -0.00106866766551801,
            -0.00149187342840459,
            -0.000239247509055854,
            -0.00210458490154006,
            -0.000390446223409143,
            1.26078443756041e-05,
            -0.000776568894825754,
            -0.000231063036090529,
            2.37203499336077e-05,
            5.73896878024664e-05,
            -0.000161289745873839,
            0.000135409458856676,
            0.000730940717716324,
            -1.63409161629670e-05,
            0.000486712242160171,
            0.000323578796785641,
            0.000565370860594299,
            0.000755230304180715,
            -5.88176011234986e-06,
            -0.000233466883037558,
            0.000517979664147754,
            0.000658250665361188,
            0.000354540554728520,
            0.000786499658562875,
            0.000168431079251300,
            0.000720556811978894,
            0.000359500855077667,
            0.000672304117300618,
            0.000546459996757053,
            -0.000148545363181185,
            0.000118662792580403,
            0.000325510278869351,
            0.000200360313975825,
            -0.000160971605392941,
            -0.000434032446116844,
            -1.97701770401804e-05,
            -5.57462272752636e-05,
            -0.000228017201791650,
            0.000140972473464382,
            -9.97645118253400e-05,
            -0.000195221932766007,
            0.000158802249769929,
            6.99858094202199e-07,
            -1.79745161663396e-05,
            -3.83846487308909e-07,
            6.24339236659401e-05,
            -8.16029395661281e-05,
            -0.000107117818161214,
            0.000184361939282170,
            -0.000126120124178591,
            0,
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
            public void didRangeBeaconsInRegion(final Collection<Beacon> beacons, Region region) {
                if (beacons.size() > 0 && (!training || pos < 200)) {
                    //beacons.iterator().next().getDistance()
                    //beaconManager.setBackgroundMode(true);
                    double[] values = training ? new double[11] : new double[10];
                    for (Beacon beacon : beacons) {
                        if (beacon.getBluetoothAddress().endsWith("4A")) {
                            values[0] = beacon.getDistance();
                        } else if (beacon.getBluetoothAddress().endsWith("53")) {
                            values[1] = beacon.getDistance();
                        } else {
                            values[2] = beacon.getDistance();
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
                        values[10] = pos++ < 150 ? 1 : 0;
                        saveTextFile(Arrays.toString(values) + "\n");
                    } else {
                        double[] features = mapFeature(values);
                        double z = 0;
                        for (int i = 0; i < 91; i++) {
                            z += features[i] * theta[i];
                        }
                        final double certainity = new Sigmoid().value(z);
                        final boolean inRoom = certainity > 0.5;
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(BeaconService.this, "In room? " + inRoom +
                                                " (" + (inRoom ? (certainity * 100) : ((1 - certainity)) * 100) + "%)",
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
        double[] out = new double[91];
        System.arraycopy(features, 3, out, 84, 7);
        int pos = 0;
        out[pos] = 1;
        for (int i = 1; i <= 6; i++) {
            for (int j = 0; j <= i; j++) {
                for (int k = 0; k <= j; k++) {
                    out[++pos] = Math.pow(x1, i - j) * Math.pow(x2, j - k) * Math.pow(x3, k);
                }
            }
        }
        return out;
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
