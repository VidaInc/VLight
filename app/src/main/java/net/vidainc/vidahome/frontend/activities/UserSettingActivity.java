package net.vidainc.vidahome.frontend.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.View;
import android.widget.Button;

import net.vidainc.vidahome.R;

public class UserSettingActivity extends PreferenceActivity {

	int tagCircle;

	Button mBackBtn;
	Button mTrainBtn;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.settings);
		setContentView(R.layout.activity_usersetting);

		tagCircle = getIntent().getExtras().getInt("tagCircle");

		/**
		 * Setting List Preference Dynamically
		 */

		// TODO: Connect Data base in this part to retrieve the data necessary to populate the list on Preference Activity

/*		ListPreference listPreferenceCategory = (ListPreference) findPreference("default_category");

		if (listPreferenceCategory != null) {

			myDB = openOrCreateDatabase("BEACONS", MODE_PRIVATE, null);
			Cursor crs = myDB.rawQuery("SELECT * FROM BEACON", null);

			CharSequence entries[] = new String[crs.getCount()];
			CharSequence entryValues[] = new String[crs.getCount()];

			int i = 0;
			while(crs.moveToNext()){
				String name = crs.getString(crs.getColumnIndex("NAME"));
				entries[i] = name;

				String macAdress = crs.getString(crs.getColumnIndex("MACADRESS"));
				entries[i] = macAdress;


				i++;
			}

			listPreferenceCategory.setEntries(entries);
			listPreferenceCategory.setEntryValues(entryValues);
		}*/


		mBackBtn = (Button) findViewById(R.id.back_btn);
		mBackBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				Intent resultIntent = new Intent();
				resultIntent.putExtra("taggedCircle", tagCircle);
				setResult(Activity.RESULT_OK, resultIntent);
				finish();
			}
		});

		mTrainBtn = (Button) findViewById(R.id.train_btn);
		mTrainBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				Intent intent = new Intent(new Intent(UserSettingActivity.this, TrainingActivity.class));
				startActivity(intent);
			}
		});
	}
}
