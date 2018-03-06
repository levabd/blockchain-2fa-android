package com.bc2fa.a2fa;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.github.gfx.util.encrypt.EncryptedSharedPreferences;
import com.github.gfx.util.encrypt.Encryption;

import java.util.HashMap;

/**
 * Created by Oleg Levitsky
 */

public class MainActivity extends AppCompatActivity {
    @SuppressWarnings("unused")
    private static final String TAG = "MainActivity";

    SharedPreferences mPrefs;

    private RefreshTask mRefreshTask = null;
    private VerifyTask mVerifyTask = null;

    // UI references.
    private View mValidateFormView;
    private View mProgressView;
    private TextView mQuestionDescription;
    private TextView mQuestionTitle;
    private Button mRejectButton;
    private Button mVerifyButton;

    HashMap<String, String> services;
    HashMap<String, String> events;

    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadEventsAndServicesCatalog();

        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);

        // Application preference
        mPrefs = new EncryptedSharedPreferences(Encryption.getDefaultCipher(), this);

        Toolbar topToolBar = findViewById(R.id.toolbar);
        setSupportActionBar(topToolBar);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(false);

        mQuestionDescription = findViewById(R.id.questionDescription);
        mQuestionTitle = findViewById(R.id.questionTitle);

        mValidateFormView = findViewById(R.id.verifyView);
        mProgressView = findViewById(R.id.reloadProgress);

        ImageButton mReloadButton = findViewById(R.id.reloadPushButton);
        mReloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFullProgress(true);
                mRefreshTask = new RefreshTask();
                mRefreshTask.execute((Void) null);
            }
        });

        mRejectButton = findViewById(R.id.rejectButton);
        mRejectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFullProgress(true);
                mVerifyTask = new VerifyTask(true);
                mVerifyTask.execute((Void) null);
            }
        });

        mVerifyButton = findViewById(R.id.verifyButton);
        mVerifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFullProgress(true);
                mVerifyTask = new VerifyTask(false);
                mVerifyTask.execute((Void) null);
            }
        });

        // Load new requests
        showFullProgress(true);
        mRefreshTask = new RefreshTask();
        mRefreshTask.execute((Void) null);
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mVerifyButton.getVisibility() == View.GONE){ // Load received requests from server
                showFullProgress(true);
                mRefreshTask = new RefreshTask();
                mRefreshTask.execute((Void) null);
            } else { // Tell user to load request after answer on current
                showModal(getString(R.string.action_new_request));
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver((mMessageReceiver),
                new IntentFilter(getString(R.string.default_notification_broadcast_name))
        );
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    }

    private void loadEventsAndServicesCatalog(){
        Resources res = this.getResources();
        String[] hashMapData = res.getStringArray(R.array.services_map);
        services = new HashMap<>();
        for(int i=0; i<hashMapData.length; i=i+2) {
            services.put(hashMapData[i], hashMapData[i+1]);
        }

        hashMapData = res.getStringArray(R.array.events_map);
        events = new HashMap<>();
        for(int i=0; i<hashMapData.length; i=i+2) {
            events.put(hashMapData[i], hashMapData[i+1]);
        }
    }

    private String getService(String key){
        String value = services.get(key);
        return  (value != null) ? value : key;
    }

    private String getEvent(String key){
        String value = events.get(key);
        return  (value != null) ? value : key;
    }

    private void showFullProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        // Sdk is always >= 23(HONEYCOMB)
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        setAnimatedHide(show, shortAnimTime, mValidateFormView);

        setAnimatedHide(!show, shortAnimTime, mProgressView);
    }

    private void setAnimatedHide(final boolean hide, int shortAnimTime, final View target) {
        target.setVisibility(hide ? View.GONE : View.VISIBLE);
        target.animate().setDuration(shortAnimTime).alpha(
                hide ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                target.setVisibility(hide ? View.GONE : View.VISIBLE);
            }
        });
    }

    private void showModal(String message) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        // alert.setTitle("Modal");
        alert.setMessage(message);

        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Nothing to fire
            }
        });

        alert.show();
    }

    /**
     * Represents an asynchronous refresh PUSH messages task
     */
    @SuppressLint("StaticFieldLeak")
    public class RefreshTask extends AsyncTask<Void, Void, Boolean> {

        RefreshTask() { }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt refresh against a network service.

            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mRefreshTask = null;
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            if (success) {
                showFullProgress(false);
                mQuestionDescription.setText(String.format(getString(R.string.request_description_format), getService("kazakhtelecom"), getEvent("login")));
                mQuestionTitle.setText(String.format(getString(R.string.request_title_format), getService("kazakhtelecom")));

                setAnimatedHide(false, shortAnimTime, mRejectButton);
                setAnimatedHide(false, shortAnimTime, mVerifyButton);

                // showModal(getString(R.string.no_new_request));
            } else {
                showFullProgress(false);

                // Display default main activity
                mQuestionDescription.setText(getString(R.string.main_default_description));
                mQuestionTitle.setText(getString(R.string.main_default_title));
                setAnimatedHide(true, shortAnimTime, mRejectButton);
                setAnimatedHide(true, shortAnimTime, mVerifyButton);

                showModal(getString(R.string.error_request));
            }
        }

        @Override
        protected void onCancelled() {
            mRefreshTask = null;
            showFullProgress(false);
        }
    }

    /**
     * Represents an asynchronous verify request task
     */
    @SuppressLint("StaticFieldLeak")
    public class VerifyTask extends AsyncTask<Void, Void, Boolean> {

        private boolean mRejected;

        VerifyTask(boolean rejected) {
            mRejected = rejected;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            if (mRejected) {
                try {
                    // Simulate network access.
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    return false;
                }

                return true;
            } else {
                // TODO: attempt verify request a network service.

                try {
                    // Simulate network access.
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    return false;
                }

                return true;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mVerifyTask = null;

            if (success) {
                int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

                showFullProgress(false);
                mQuestionDescription.setText("");
                mQuestionTitle.setText(mRejected ? getString(R.string.success_rejected) : getString(R.string.success_verified));

                setAnimatedHide(true, shortAnimTime, mRejectButton);
                setAnimatedHide(true, shortAnimTime, mVerifyButton);
            } else {
                showFullProgress(false);
                showModal(getString(R.string.error_verified));
            }
        }

        @Override
        protected void onCancelled() {
            mVerifyTask = null;
            showFullProgress(false);
        }
    }
}
