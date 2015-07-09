package net.vidainc.vidahome.frontend.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import net.vidainc.vidahome.R;

/**
 * Created by Aaron on 28/02/2015.
 */
public abstract class BaseActivity extends AppCompatActivity {
    private Toolbar mToolbar;

    @Override
    protected void onStart() {
        super.onStart();
        activateToolbar();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    protected Toolbar activateToolbar() {
        if (mToolbar == null) {
            mToolbar = (Toolbar) findViewById(R.id.app_bar);
            setSupportActionBar(mToolbar);
        }

        return mToolbar;
    }

    protected Toolbar activateToolbarWithHome(boolean isOn) {
        activateToolbar();
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(isOn);
        }
        return mToolbar;
    }

    public void showError(String title, String message, final boolean killActivity) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (killActivity)
                    finish();
            }
        });
        builder.setMessage(message);
        builder.show();
    }
}
