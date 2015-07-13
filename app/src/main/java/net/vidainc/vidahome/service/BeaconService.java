package net.vidainc.vidahome.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.widget.Toast;

import net.vidainc.vidahome.Constants;
import net.vidainc.vidahome.R;
import net.vidainc.vidahome.models.BeaconData;
import net.vidainc.vidahome.utils.TimedBeaconSimulator;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.json.JSONException;

import java.util.Collection;
import java.util.HashSet;


public class BeaconService extends Service implements BeaconConsumer {
    public static final Region ALL_BEACONS_REGION = new Region("apr", null,
            null, null);
    private BeaconManager beaconManager;
    private HashSet<Beacon> mBeacons;
    private Handler mHandler;

    public BeaconService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        beaconManager = BeaconManager.getInstanceForApplication(getApplicationContext());
        BeaconManager.setBeaconSimulator(new TimedBeaconSimulator());
        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout(getString(R.string.ibeacon_layout)));
        beaconManager.bind(this);
        //beaconManager.setDebug(true);
        mHandler = new Handler();
        mBeacons = new HashSet<>();
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
                if (beacons.size() > 0) {
                    //beacons.iterator().next().getDistance()
                    mBeacons.addAll(beacons);
                    Intent msgIntent = new Intent(BeaconService.this, GcmIntentService.class);
                    msgIntent.setAction(Constants.ACTION_BEACON_DATA);
                    try {
                        msgIntent.putExtra(Constants.KEY_MESSAGE_TXT,
                                BeaconData.toJsonString(BeaconData.fromBeacons(beacons)));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    startService(msgIntent);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(BeaconService.this, beacons.size() + " beacons found",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

        try {
            beaconManager.startRangingBeaconsInRegion(ALL_BEACONS_REGION);
        } catch (RemoteException e) {
        }
    }
}
