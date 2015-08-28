package net.vidainc.vidahome;

import android.app.Application;

import net.vidainc.vidahome.service.BeaconService;

import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;
import org.altbeacon.beacon.startup.BootstrapNotifier;
import org.altbeacon.beacon.startup.RegionBootstrap;

/**
 * Created by Aaron on 10/07/2015.
 */
public class VidaHome extends Application implements BootstrapNotifier {
    public static boolean edited = false;
    private RegionBootstrap regionBootstrap;
    private BackgroundPowerSaver backgroundPowerSaver;

    @Override
    public void onCreate() {
        super.onCreate();
        BeaconManager beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers()
                .add(new BeaconParser().
                        setBeaconLayout(getString(R.string.ibeacon_layout)));
        // wake up the app when any beacon is seen (you can specify specific id filers in the parameters below)
        regionBootstrap = new RegionBootstrap(this, BeaconService.ALL_BEACONS_REGION);
        backgroundPowerSaver = new BackgroundPowerSaver(this);
    }

    @Override
    public void didEnterRegion(Region region) {

    }

    @Override
    public void didExitRegion(Region region) {

    }

    @Override
    public void didDetermineStateForRegion(int i, Region region) {

    }
}
