package net.vidainc.vidahome.frontend.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import  net.vidainc.vidahome.R;
import net.vidainc.vidahome.database.BeaconProvider;


/*
The rooms where devices are displayed and controlled
 */
public class RoomMapActivity extends AppCompatActivity {

    TextView typeBtn;
    ImageView CenterButton;
    ImageView beaconTop;

    String nameRoom;

    /**
     * Button from the Botton Nav
     */
    ImageView btn1;
    ImageView btn2;
    ImageView btn3;
    ImageView btn4;
    ImageView btn5;

    /**
     * Beacons buttons
     */

    ImageView btn_beacon1;
    ImageView btn_beacon2;
    ImageView btn_beacon3;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_map);

        typeBtn = (TextView) findViewById(R.id.roomType);

        //TODO: GET ID SENT FROM FRAGMENT TWO TO USE TO RETRIEVE THE PROPERLY DATA FROM THE BEACON DB
        Intent intent = getIntent();
        nameRoom = intent.getStringExtra("name_room");

        //TODO: GET from the database room name
        typeBtn.setText(BeaconProvider.getRoom(this, nameRoom).getName());




        /**
         * Button from the Bottom Nav
         */

        btn1 = (ImageView) findViewById(R.id.btn1);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                bntSelection(true, false, false, false, false);
                Toast.makeText(RoomMapActivity.this, "Clicked 1", Toast.LENGTH_LONG).show();

            }
        });

        btn2 = (ImageView) findViewById(R.id.btn2);
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                bntSelection(false,true,false,false,false);
                Toast.makeText(RoomMapActivity.this, "Clicked 2", Toast.LENGTH_LONG).show();

            }
        });

        btn3 = (ImageView) findViewById(R.id.btn3);
        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                bntSelection(false,false,true,false,false);
                Toast.makeText(RoomMapActivity.this, "Clicked 3", Toast.LENGTH_LONG).show();

            }
        });

        btn4 = (ImageView) findViewById(R.id.btn4);
        btn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                bntSelection(false,false,false,true,false);
                Toast.makeText(RoomMapActivity.this, "Clicked 4", Toast.LENGTH_LONG).show();

            }
        });

        btn5 = (ImageView) findViewById(R.id.btn5);
        btn5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                bntSelection(false,false,false,false,true);
                Toast.makeText(RoomMapActivity.this, "Clicked 5", Toast.LENGTH_LONG).show();

            }
        });

        /**
         * Beacons buttons
         */

        btn_beacon1 = (ImageView) findViewById(R.id.btn_beacon1);
        btn_beacon1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                beaconSelection(true, false, false);
                Toast.makeText(RoomMapActivity.this, "Beacon Clicked 1", Toast.LENGTH_LONG).show();
//TODO add control functions
            }
        });

        btn_beacon2 = (ImageView) findViewById(R.id.btn_beacon2);
        btn_beacon2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                beaconSelection(false, true ,false);
                Toast.makeText(RoomMapActivity.this, "Beacon Clicked 2", Toast.LENGTH_LONG).show();

            }
        });

        btn_beacon3 = (ImageView) findViewById(R.id.btn_beacon3);
        btn_beacon3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                beaconSelection(false,false,true);
                Toast.makeText(RoomMapActivity.this, "Beacon Clicked 3", Toast.LENGTH_LONG).show();

            }
        });

    }

    public void bntSelection(boolean btn1Status,boolean btn2Status,boolean btn3Status,boolean btn4Status,boolean btn5Status){

        btn1.setSelected(btn1Status);
        btn2.setSelected(btn2Status);
        btn3.setSelected(btn3Status);
        btn4.setSelected(btn4Status);
        btn5.setSelected(btn5Status);


    }

    public void beaconSelection(boolean btn1Status,boolean btn2Status,boolean btn3Status){

        btn_beacon1.setSelected(btn1Status);
        btn_beacon2.setSelected(btn2Status);
        btn_beacon3.setSelected(btn3Status);



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_room_map, menu);
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
