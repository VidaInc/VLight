package net.vidainc.vidahome.frontend.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import net.vidainc.vidahome.R;
import net.vidainc.vidahome.service.BeaconService;

public class MainActivity extends Activity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startService(new Intent(this, BeaconService.class));

//
//        //TODO activity selection logic is here
//        if(!net.vidainc.vidahome.VidaHome.isLoggedIn){
//            //goto login activity
//            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
//            startActivity(intent);
//        }
//        else if(!VidaHome.edited){
//            //goto setup activity
//            Intent intent = new Intent(getApplicationContext(), SetupActivity.class);
//            startActivity(intent);
//        }
//        else{
//            //goto roommap activity
//            Intent intent = new Intent(getApplicationContext(), FloorMapActivity.class);
//            startActivity(intent);
//        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}


//    Fragment fr;
//    FragmentManager fm;
//    FragmentTransaction fragmentTransaction;
//
//    ImageButton FAB;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//        startService(new Intent(this, BeaconService.class));
//
//        FAB = (ImageButton) findViewById(R.id.imageButton);
//
//        FAB.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                if (VidaHome.edited) {
//
//                    MainActivity.this.recreate();
//                    //startActivity(new Intent(MainActivity.this, EnteredRoom.class));
//
//                } else {
//
//                   // startActivity(new Intent(MainActivity.this, EnteredRoom.class));
//
//                    new AlertDialog.Builder(MainActivity.this)
//                            .setTitle("Alert")
//                            .setMessage("You Have to add a room")
//                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
//                                public void onClick(DialogInterface dialog, int which) {
//                                    // continue with delete
//                                }
//                            })
//                            .setIcon(android.R.drawable.ic_dialog_alert)
//                            .show();
//                }
//
//            }
//        });
//
//        if (VidaHome.edited || true){
//
//
//            fr = new FragmentOne();
//
//            fm = getFragmentManager();
//            fragmentTransaction = fm.beginTransaction();
//            fragmentTransaction.replace(R.id.fragment_place, fr);
//            fragmentTransaction.commit();
//
//        } else {
//
//            FAB.setVisibility(View.INVISIBLE);
//
//            fr = new FragmentTwo();
//
//            fm = getFragmentManager();
//            fragmentTransaction = fm.beginTransaction();
//            fragmentTransaction.replace(R.id.fragment_place, fr);
//            fragmentTransaction.commit();
//
//        }
//    }
//
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
//}
