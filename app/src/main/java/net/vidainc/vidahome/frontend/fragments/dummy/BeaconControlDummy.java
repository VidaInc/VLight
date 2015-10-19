package net.vidainc.vidahome.frontend.fragments.dummy;


import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.squareup.otto.Subscribe;

import net.vidainc.vidahome.Constants;
import net.vidainc.vidahome.R;
import net.vidainc.vidahome.models.BeaconTrainEvent;
import net.vidainc.vidahome.service.LightDevice;
import net.vidainc.vidahome.utils.OttoBus;


/**
 * A simple {@link Fragment} subclass.
 */
public class BeaconControlDummy extends Fragment {
    private LightDevice mBeaconOne;
    private LightDevice mBeaconTwo;
    private LightDevice mBeaconThree;
    private boolean isBeaconOneConnected;
    private boolean isBeaconTwoConnected;
    private boolean isBeaconThreeConnected;
    private Handler mHandler;


    private EditText mRoomNumberEdit;
    private Button mChangeRoomButton;


    public BeaconControlDummy() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_beacon_control_dummy, container, false);
        ToggleButton beaconOneButton = (ToggleButton) rootView.findViewById(R.id.toggleButton_one);
        ToggleButton beaconTwoButton = (ToggleButton) rootView.findViewById(R.id.toggleButton_two);
        ToggleButton beaconThreeButton = (ToggleButton) rootView.findViewById(R.id.toggleButton_three);
        ToggleButton allControlButton = (ToggleButton) rootView.findViewById(R.id.all_control_toggle);
        ToggleButton trainButton = (ToggleButton) rootView.findViewById(R.id.toggleButton_train);


        mRoomNumberEdit = (EditText) rootView.findViewById(R.id.change_room_edit);
        mChangeRoomButton = (Button) rootView.findViewById(R.id.change_room_button);

        final Context context = getActivity();
        mHandler = new Handler();
        OttoBus.getInstance().register(this);

        mBeaconOne = new LightDevice(context, new LightDevice.OnLightDeviceConnectedListener() {
            @Override
            public void onConnected() {
                isBeaconOneConnected = true;
                Toast.makeText(context, "Beacon one is connected",
                        Toast.LENGTH_SHORT).show();
            }
        }, Constants.BEACON_MAC_ONE);

        mBeaconTwo = new LightDevice(context, new LightDevice.OnLightDeviceConnectedListener() {
            @Override
            public void onConnected() {
                isBeaconTwoConnected = true;
                Toast.makeText(context, "Beacon two is connected",
                        Toast.LENGTH_SHORT).show();
            }
        }, Constants.BEACON_MAC_TWO);

        mBeaconThree = new LightDevice(context, new LightDevice.OnLightDeviceConnectedListener() {
            @Override
            public void onConnected() {
                isBeaconThreeConnected = true;
                Toast.makeText(context, "Beacon three is connected",
                        Toast.LENGTH_SHORT).show();
            }
        }, Constants.BEACON_MAC_THREE);

        beaconOneButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (isBeaconOneConnected) {
                            byte value = isChecked ? (byte) 255 : (byte) 0;
                            boolean wasWritten = mBeaconOne.writeToLight(value);
                            if (wasWritten)
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(context, "Beacon one written to", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            else
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(context, "Beacon one not written to", Toast.LENGTH_SHORT).show();
                                    }
                                });
                        } else {
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(context, "Beacon one is not connected",
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }).start();
            }
        });
        beaconTwoButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (isBeaconTwoConnected) {
                            byte value = isChecked ? (byte) 255 : (byte) 0;
                            boolean wasWritten = mBeaconTwo.writeToLight(value);
                            if (wasWritten)
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(context, "Beacon two written to", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            else
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(context, "Beacon two not written to", Toast.LENGTH_SHORT).show();
                                    }
                                });
                        } else {
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(context, "Beacon two is not connected",
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }).start();
            }
        });
        beaconThreeButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (isBeaconThreeConnected) {
                            byte value = isChecked ? (byte) 255 : (byte) 0;
                            boolean wasWritten = mBeaconThree.writeToLight(value);
                            if (wasWritten)
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(context, "Beacon three written to", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            else
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(context, "Beacon three not written to", Toast.LENGTH_SHORT).show();
                                    }
                                });
                        } else {
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(context, "Beacon three is not connected",
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }).start();
            }
        });
        allControlButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (isBeaconOneConnected && isBeaconTwoConnected && isBeaconThreeConnected) {
                            byte value = isChecked ? (byte) 255 : (byte) 0;
                            boolean wasWritten = mBeaconOne.writeToLight(value) &
                                    mBeaconTwo.writeToLight(value) &
                                    mBeaconThree.writeToLight(value);
                            if (wasWritten)
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(context, "All were written to", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            else
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(context, "All were not written to", Toast.LENGTH_SHORT).show();
                                    }
                                });
                        } else {
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(context, "All beacons are not connected",
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }).start();
            }
        });
        trainButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                BeaconTrainEvent event = new BeaconTrainEvent();
                event.setIsTraining(isChecked);
                OttoBus.getInstance().post(event);
            }
        });
        mChangeRoomButton
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((ToggleButton)rootView.findViewById(R.id.toggleButton_train))
                                .setChecked(true);
                        BeaconTrainEvent event = new BeaconTrainEvent();
                        event.setIsTraining(true);
                        Bundle extras = new Bundle();
                        event.setExtras(extras);
                        extras.putInt(Constants.KEY_BEACON_SERVICE_ROOM_NUMBER,
                                Integer.parseInt(mRoomNumberEdit.getText().toString()));
                        OttoBus.getInstance()
                                .post(event);
                    }
                });

        return rootView;
    }

    @Subscribe
    public void onTrainEvent(BeaconTrainEvent event) {
        if (event.isTraining()) {
            mRoomNumberEdit.setVisibility(View.VISIBLE);
            mChangeRoomButton.setVisibility(View.VISIBLE);
            ((ToggleButton)getView().findViewById(R.id.toggleButton_train))
                    .setChecked(true);
        } else {
            mRoomNumberEdit.setVisibility(View.INVISIBLE);
            mChangeRoomButton.setVisibility(View.INVISIBLE);
            if (event.getExtras() != null) {
                Toast.makeText(getActivity(), "Room number: " + event.getExtras()
                        .getInt(Constants.KEY_BEACON_SERVICE_ROOM_NUMBER), Toast.LENGTH_SHORT).show();
                Toast.makeText(getActivity(), "certainty: " + event.getExtras()
                        .getDouble(Constants.KEY_BEACON_SERVICE_ROOM_CERTAINTY), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
