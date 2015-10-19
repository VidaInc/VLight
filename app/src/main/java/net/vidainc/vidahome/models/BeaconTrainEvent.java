package net.vidainc.vidahome.models;

import android.os.Bundle;

/**
 * Created by Staple on 10/18/2015.
 */
public class BeaconTrainEvent {
    private boolean isTraining;
    private Bundle mExtras;

    public boolean isTraining() {
        return isTraining;
    }

    public void setIsTraining(boolean isTraining) {
        this.isTraining = isTraining;
    }

    public Bundle getExtras() {
        return mExtras;
    }

    public void setExtras(Bundle extras) {
        mExtras = extras;
    }
}
