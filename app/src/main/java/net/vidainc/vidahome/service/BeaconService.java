package net.vidainc.vidahome.service;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.widget.Toast;

import com.jmatio.io.MatFileReader;
import com.jmatio.types.MLDouble;
import com.squareup.otto.Subscribe;

import net.vidainc.vidahome.Constants;
import net.vidainc.vidahome.R;
import net.vidainc.vidahome.models.BeaconTrainEvent;
import net.vidainc.vidahome.utils.OttoBus;

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
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Collection;


public class BeaconService extends Service implements BeaconConsumer {
    public static final Region ALL_BEACONS_REGION = new Region("apr", null,
            null, null);
    private static final int NUM_OF_ROOMS = 3;
    private static double[][] theta1;
    private static double[][] theta2;
    private static double[][] theta3;
    private BeaconManager beaconManager;
    private volatile Handler mHandler;
    private volatile boolean training;
    private volatile int room;
    private volatile int pos;
    private double[] threshold = new double[NUM_OF_ROOMS];
    public BeaconService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        File dir = Environment.getExternalStorageDirectory();
        File thetas = new File(dir, "thetas.mat");
        try {
            MatFileReader matfilereader = new MatFileReader(thetas);
            theta1 = ((MLDouble) matfilereader.getMLArray("Theta1")).getArray();
            theta2 = ((MLDouble) matfilereader.getMLArray("Theta2")).getArray();
            theta3 = ((MLDouble) matfilereader.getMLArray("Theta3")).getArray();
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
        mHandler = new Handler(getMainLooper());
        Intent regIntent = new Intent(this, GcmIntentService.class);
        regIntent.setAction(Constants.ACTION_REGISTER);
        startService(regIntent);
        OttoBus.getInstance().register(this);
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
        beaconManager.setRangeNotifier(new RangeNotifier() {

            @Override
            public synchronized void didRangeBeaconsInRegion(final Collection<Beacon> beacons, Region region) {
                if (beacons.size() > 0) {
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
                                Toast.makeText(BeaconService.this, "Pos: " + pos + " Room: " + room,
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                        values[12] = room;
                        pos++;
                        saveTextFile(Arrays.toString(values) + "\n");
                    } else {
                        //TODO implement the control methods
                        for (Beacon beacon : beacons) {
                            if (beacon.getBluetoothAddress().equals(Constants.BEACON_MAC_ONE)) {
                                if(predict(mapFeature(values))[0]>(threshold[0]+0.7)  ){

                                }
                            } else if (beacon.getBluetoothAddress().equals(Constants.BEACON_MAC_TWO)  ) {

                            } else if (beacon.getBluetoothAddress().equals(Constants.BEACON_MAC_THREE)){

                            }
                        }
                        for (int i=0;i<NUM_OF_ROOMS;i++){
                            if(predict(mapFeature(values))[i]>(threshold[i]+0.7)){
                                //turn on i

                            }
                            else if(predict(mapFeature(values))[i]<(threshold[i]+0.7)){
                                //turn off i
                            }
                        }
                        //call lightDevice WriteToLight() that's hardcoded
                        //final double[] features = mapFeature(values);
                        //final int room = predict(features);
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(BeaconService.this, "In room " + "?",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    //
////                    Intent msgIntent = new Intent(BeaconService.this, GcmIntentService.class);
////                    msgIntent.setAction(Constants.ACTION_BEACON_DATA);
////                    try {
////                        msgIntent.putExtra(Constants.KEY_MESSAGE_TXT,
////                                BeaconData.toJsonString(BeaconData.fromBeacons(beacons)));
////                    } catch (JSONException e) {
////                        e.printStackTrace();
////                    }
////                    startService(msgIntent);
                } else {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(BeaconService.this, "No beacons detected",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

        try {
            beaconManager.startMonitoringBeaconsInRegion(ALL_BEACONS_REGION);
            beaconManager.startRangingBeaconsInRegion(ALL_BEACONS_REGION);
        } catch (RemoteException ignored) {
        }
    }

    @Subscribe
    public void onReceiveTrainEvent(BeaconTrainEvent event) {
        Toast.makeText(this, "Training? " + event.isTraining(), Toast.LENGTH_SHORT).show();
        training = event.isTraining();
        if (event.isTraining() && event.getExtras() != null) {
            Toast.makeText(this, "Room number: " + event.getExtras()
                    .getInt(Constants.KEY_BEACON_SERVICE_ROOM_NUMBER), Toast.LENGTH_SHORT).show();
            room = event.getExtras()
                    .getInt(Constants.KEY_BEACON_SERVICE_ROOM_NUMBER);
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

    private static double[] predict(double[] features) {
        double[] firstActivation = new double[features.length + 1];
        double[] secondActivation = new double[theta1.length + 1];
        double[] thirdActivation = new double[theta2.length + 1];
        double[] secondActivationHypothesis = new double[theta1.length];
        double[] thirdActivationHypothesis = new double[theta2.length];
        double[] finalHypothesis = new double[NUM_OF_ROOMS];
        firstActivation[0] = 1;
        secondActivation[0] = 1;
        thirdActivation[0] = 1;
        System.arraycopy(features, 0, firstActivation, 1, features.length);
        for (int i = 0; i < theta1.length; i++) {
            double z = 0;
            for (int j = 0; j < theta1[0].length; j++) {
                z += theta1[i][j] * firstActivation[j];
            }
            secondActivationHypothesis[i] = sigmoid(z);
        }
        System.arraycopy(secondActivationHypothesis, 0, secondActivation, 1,
                secondActivationHypothesis.length);
        for (int i = 0; i < theta2.length; i++) {
            double z = 0;
            for (int j = 0; j < theta2[0].length; j++) {
                z += theta2[i][j] * secondActivation[j];
            }
            thirdActivationHypothesis[i] = sigmoid(z);
        }
        System.arraycopy(thirdActivationHypothesis, 0, thirdActivation, 1,
                thirdActivationHypothesis.length);
        for (int i = 0; i < theta3.length; i++) {
            double z = 0;
            for (int j = 0; j < theta3[0].length; j++) {
                z += theta3[i][j] * thirdActivation[j];
            }
            finalHypothesis[i] = sigmoid(z);
        }
//        int max = 0;
//        for (int i = 0; i < finalHypothesis.length; i++) {
//            if (finalHypothesis[i] > finalHypothesis[max])
//                max = i;
//        }
//        final int prediction = max + 1;
//        Log.d("CLASSIFIER CONFIDENCE", "Room : " + prediction + "\n " +
//                Arrays.toString(finalHypothesis));
        return finalHypothesis;
    }

    private static double sigmoid(double z) {
        return 1 / (1 + FastMath.exp(-z));
    }

    public synchronized void saveTextFile(String content) {
        File externalStorageDir = Environment.getExternalStorageDirectory();
        File myFile = new File(externalStorageDir, "data.txt");

        if (myFile.exists()) {
            try {
                FileOutputStream fOut = new FileOutputStream(myFile);
                OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                myOutWriter.append(content);
                myOutWriter.close();
                fOut.close();
            } catch (Exception ignored) {
            }
        } else {
            try {
                myFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
