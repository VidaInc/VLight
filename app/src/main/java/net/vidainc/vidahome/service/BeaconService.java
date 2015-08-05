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
    private Handler mHandler;
    private boolean training = false;
    private volatile int pos;
    private final double[] theta = {1.74167142965129,
            0.358419381525668,
            0.179131087072422,
            0.165363047227542,
            0.0753475207274787,
            0.0335465236076664,
            0.0127472364892021,
            -0.00242688030212337,
            0.0122262689793569,
            -0.00751712444223575,
            0.00753157318440223,
            -0.00157706433390313,
            -0.00669185484149311,
            -0.00536900838975497,
            0.00382775412915042,
            -0.0133073749586671,
            -0.00889981837263626,
            -0.00388073515027683,
            -0.00489183984461220,
            -0.0122754599612471,
            -0.00273544802184205,
            -0.00475931388500774,
            -0.00469317847785387,
            -0.00579499580484991,
            3.00149733842053e-05,
            -0.00735130051286360,
            -0.00284775985309856,
            0.000490501841481083,
            -0.000343168717513339,
            -0.00483935788722494,
            -0.00263699457184922,
            -0.000876894339341238,
            -0.00322218757983647,
            -0.00172276273690931,
            -0.00312132371158349,
            -0.00157824526790151,
            -0.000783257727014414,
            -0.000602480187188912,
            -0.00258598635976952,
            0.000388390722506373,
            -0.00149913828527974,
            -0.00172239660965038,
            -2.51296647735969e-05,
            -0.00137364186717386,
            -0.00102204925101316,
            -0.000431301959474638,
            0.00114922357386192,
            -0.000113690749190809,
            0.000933730660146815,
            0.000412102989850058,
            0.000299264469419536,
            0.000743437454740195,
            -0.000300684570609229,
            -0.000882416164531408,
            0.00105152211886541,
            0.000845297785514138,
            -0.00160019159849515,
            0.00302019975494485,
            0.000710290220982100,
            0.000774674508124915,
            0.00136498757797451,
            0.000929949565249273,
            -0.000197253422850219,
            0.000424863452524941,
            -0.000935346554120533,
            0.000430686737446407,
            -0.000225335155494696,
            0.000300732214503822,
            -0.00114089435068196,
            -0.00100923559446067,
            -0.000203674206493420,
            -0.000266761200206003,
            0.000765221118000287,
            0.000210260272460261,
            -0.000120435981727317,
            0.000348192206482604,
            2.93951589869158e-05,
            2.22402445925672e-05,
            4.03529628377162e-05,
            -1.25975275757109e-05,
            -0.000210520636816177,
            -0.000230511650366743,
            0.000235080605345394,
            -7.99611002591979e-05,
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
        double[] out = new double[91];
        System.arraycopy(features, 3, out, 84, 7);
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
