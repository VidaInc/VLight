package net.vidainc.vidahome.frontend.activities;

import android.content.Intent;
import android.os.Bundle;

import net.vidainc.vidahome.R;
import net.vidainc.vidahome.service.BeaconService;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startService(new Intent(this, BeaconService.class));
    }
}
