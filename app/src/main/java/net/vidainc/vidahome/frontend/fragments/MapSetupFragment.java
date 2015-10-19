package net.vidainc.vidahome.frontend.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.vidainc.vidahome.R;
import net.vidainc.vidahome.VidaHome;
import net.vidainc.vidahome.database.BeaconProvider;
import net.vidainc.vidahome.frontend.activities.UserSettingActivity;
import net.vidainc.vidahome.models.Room;

import java.util.ArrayList;


/**
 * A placeholder fragment containing a simple view.
 */

public class MapSetupFragment extends Fragment{

    private View rootView;
    private RelativeLayout mRelativeLayout;
    private DragView mDragView;
    private DisplayMetrics metrics;


    ImageButton addButton;
    TextView guideTxt;

    public MapSetupFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_one, container, false);

        mRelativeLayout = (RelativeLayout) rootView.findViewById(R.id.frameCanvasOne);

        //create Canvas and link it
        mDragView = new DragView(rootView.getContext(), metrics);
        mRelativeLayout.addView(mDragView);

        guideTxt = (TextView) rootView.findViewById(R.id.guideTxt);

        addButton = (ImageButton) rootView.findViewById(R.id.addButton);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                mDragView.onClick();
                guideTxt.setVisibility(View.INVISIBLE);


            }
        });





        return rootView;
    }

    /**
     * To metrod necessary to retrieve a result send from another activity by intent
     */

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        {
            if (resultCode != Activity.RESULT_CANCELED && data != null) {
                mDragView.updateCircle(data.getExtras().getInt("taggedCircle"));
            }
        }

    }

    /**
     * Inner Class in charge to draw on the mCanvas Layout
     */

    public class DragView extends View {

        public DisplayMetrics metrics;
        public Context contextDrag;
        public ArrayList<Room> mRoomList;

        int width;
        int height;

        private Room mTouchedRoom;

        public DragView(Context context, DisplayMetrics displayMetrics){

            super(context);


            contextDrag = context;
            metrics = displayMetrics;

            mRoomList = new ArrayList<>();

            width = this.getWidth();
            height = this.getHeight();
        }


        @Override
        protected void onDraw(Canvas canvas) {
            for (int i = 0; i < mRoomList.size(); i++) {

                Room b = mRoomList.get(i);
                if (b.getImage() == null) continue;
                canvas.drawBitmap(b.getImage(), b.getCurrent().x, b.getCurrent().y, null);
            }

            invalidate();

        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {

            int action = event.getAction();
            int x = (int) event.getX();
            int y = (int) event.getY();

            switch (action) {

                case MotionEvent.ACTION_DOWN:

                    mTouchedRoom = didTouchCircle(x,y);

                    if (mTouchedRoom != null) {

                        mTouchedRoom.setSelected(true);
                        Toast.makeText(contextDrag, mTouchedRoom.getCurrent().toString(), Toast.LENGTH_SHORT).show();
                    }

                    break;
                case MotionEvent.ACTION_MOVE:

                    if (mTouchedRoom != null && mTouchedRoom.getSelected()) {

                        /**
                         * To find the middle of the Bitmap and base the movement from there
                         */

                        int resultX = x - mTouchedRoom.getHalfWidth();
                        int resultY = y - mTouchedRoom.getHalfHeight();

                        mTouchedRoom.moveTo(resultX, resultY);
                    }


                    break;
                case MotionEvent.ACTION_UP:

                    Intent intent = new Intent(contextDrag, UserSettingActivity.class);
                    intent.putExtra("tagCircle", mRoomList.indexOf(mTouchedRoom));
                    MapSetupFragment.this.startActivityForResult(intent, VidaHome.RESULT_SETTINGS);

                    break;

            }

            invalidate();
            return true;

        }

        public Room didTouchCircle(int x, int y){
// to recognize which circle is it from the array.
            //First Width
            for (Room room : mRoomList) {

                if (room.getLeft() + room.getImageWidth() > x
                        && room.getLeft() < x
                        && room.getTop() + room.getImageHeight() > y
                        && room.getTop() < y) {

                    return room;

                }

            }

            return null;

        }

        public void onClick() {

            VidaHome.edited = true;

            Point p = new Point(getWidth() / 2, getHeight() / 2);
            mRoomList.add(new Room(contextDrag, p, R.drawable.living_room));

            invalidate();
        }

        /**
         * Method necessary to retrieve the shared preferences and modify the objs based on that
         */

        public void updateCircle(int tag) {
//TODO: put training activity in

            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(contextDrag);

            String uri = sharedPrefs.getString("prefRoom", "NULL");
            String roomName = sharedPrefs.getString("room_name", "NULL");

            mRoomList.get(tag).setName(roomName);

            int imageResource = contextDrag.getResources().getIdentifier(uri, "drawable", contextDrag.getPackageName());
            mRoomList.get(tag).setDrawable(imageResource);

            Bitmap newImage = BitmapFactory.decodeResource(contextDrag.getResources(), imageResource);

            mRoomList.get(tag).setImage(newImage);


            BeaconProvider.insertRoom(getActivity(), mRoomList.get(tag));  //inserting room to database

            invalidate();

        }

    }


}
