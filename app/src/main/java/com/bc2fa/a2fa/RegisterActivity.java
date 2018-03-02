package com.bc2fa.a2fa;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class RegisterActivity extends AppCompatActivity {

    private SendCodeTask mSendCodeTask = null;
    private ShowResendTask mShowResendTask = null;
    private RegisterTask mRegisterTask = null;

    // UI references.
    private View mCodeProgressView;
    private View mRegisterProgressView;
    private View mRegisterFormView;
    private EditText mPinView;
    private View mPinLayoutView;
    private View mPinDescriptionView;
    private View mPhoneDescriptionView;
    private EditText mPhoneView;
    private View mCodeDescriptionView;
    private EditText mCodeView;

    private Button mSendCodeButton;
    private Button mRegisterButton;
    private View mResendDescription;
    private Button mResendButton;

    private String pin;
    private String phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        setupActionBar();

        // Set up the form form.
        mPinView = findViewById(R.id.pinEdit);
        mPinLayoutView = findViewById(R.id.pinEditLayout);
        mPinDescriptionView = findViewById(R.id.pinDescription);
        mPhoneView = findViewById(R.id.phoneEdit);
        mPhoneDescriptionView = findViewById(R.id.phoneDescription);
        mCodeView = findViewById(R.id.codeEdit);
        mCodeDescriptionView = findViewById(R.id.code_description_text);
        mResendDescription = findViewById(R.id.didntReceiveCode);

        mSendCodeButton = findViewById(R.id.send_code_button);
        mSendCodeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptSendCode(false);
            }
        });

        mRegisterButton = findViewById(R.id.register_button);
        mRegisterButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptStart();
            }
        });

        mResendButton = findViewById(R.id.resendButton);
        mResendButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptSendCode(true);
            }
        });

        mRegisterFormView = findViewById(R.id.register_form);
        mCodeProgressView = findViewById(R.id.code_progress);
        mRegisterProgressView = findViewById(R.id.register_progress);
    }

    // Successfully registered
    private void attemptStart() {
        String code = mCodeView.getText().toString();
        showFullProgress(true);
        mRegisterTask = new RegisterTask(phone, pin, code);
        mRegisterTask.execute((Void) null);
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        // Sdk is always >= 23(HONEYCOMB)
        // Show the Up button in the action bar.
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void attemptSendCode(boolean resend) {
        // Reset errors.
        mPinView.setError(null);
        mPhoneView.setError(null);

        phone = mPhoneView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (!resend) {
            pin = mPinView.getText().toString();

            if (TextUtils.isEmpty(pin)) {
                mPinView.setError(getString(R.string.error_empty_pin));
                focusView = mPinView;
                cancel = true;
            } else if (!isPinValid(pin)) {
                mPinView.setError(getString(R.string.error_invalid_pin));
                focusView = mPinView;
                cancel = true;
            }
        }

        if (TextUtils.isEmpty(phone)) {
            mPhoneView.setError(getString(R.string.error_empty_phone));
            focusView = mPhoneView;
            cancel = true;
        } else if (!isPhoneValid(phone)) {
            mPhoneView.setError(getString(R.string.error_invalid_phone));
            focusView = mPhoneView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt send code and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform send code attempt.
            showCodeProgress(true, true);
            mSendCodeTask = new SendCodeTask(phone, pin);
            mSendCodeTask.execute((Void) null);
        }
    }

    private boolean isPinValid(String pin) {
        return ((pin.length() == 4) && (pin.matches("\\d+(?:\\.\\d+)?")));
    }

    private boolean isPhoneValid(String phone) {
        return phone.matches("^[+]?[0-9]{10,13}$");
    }

    /**
     * Shows the progress UI and hides the form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showCodeProgress(final boolean show, final boolean success) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        // Sdk is always >= 23(HONEYCOMB)
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        if (show) {
            // Hide Send Code.
            setAnimatedHide(true, shortAnimTime, mSendCodeButton);

            setAnimatedHide(true, shortAnimTime, mCodeDescriptionView);
            setAnimatedHide(true, shortAnimTime, mCodeView);
            setAnimatedHide(true, shortAnimTime, mRegisterButton);
            setAnimatedHide(true, shortAnimTime, mResendDescription);
            setAnimatedHide(true, shortAnimTime, mResendButton);
        } else {
            if (success) {
                // Hide Pin and Description. Show SMS Code verification
                setAnimatedHide(true, shortAnimTime, mPinView);
                setAnimatedHide(true, shortAnimTime, mPinLayoutView);
                setAnimatedHide(true, shortAnimTime, mPinDescriptionView);
                setAnimatedHide(true, shortAnimTime, mPhoneDescriptionView);

                setAnimatedHide(false, shortAnimTime, mCodeDescriptionView);
                setAnimatedHide(false, shortAnimTime, mCodeView);
                setAnimatedHide(false, shortAnimTime, mRegisterButton);

                // Timer to show resend button
                mShowResendTask = new ShowResendTask();
                mShowResendTask.execute((Void) null);
                // setAnimatedHide(false, shortAnimTime, mResendDescription);
                // setAnimatedHide(false, shortAnimTime, mResendButton);
            } else {
                // Show again Send Code
                setAnimatedHide(false, shortAnimTime, mSendCodeButton);
            }
        }

        // Show or hide progres bar
        setAnimatedHide(!show, shortAnimTime, mCodeProgressView);
    }

    private void showFullProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        // Sdk is always >= 23(HONEYCOMB)
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        setAnimatedHide(show, shortAnimTime, mRegisterFormView);

        setAnimatedHide(!show, shortAnimTime, mRegisterProgressView);
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

    /**
     * Represents an asynchronous send code task used to register the user.
     */
    public class SendCodeTask extends AsyncTask<Void, Void, Boolean> {

        private final String mPhone;
        private final String mPin;

        SendCodeTask(String phone, String pin) {
            mPhone = phone;
            mPin = pin;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt send code against a network service.

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
            mSendCodeTask = null;

            if (success) {
                showCodeProgress(false, true);
            } else {
                showCodeProgress(false, false);
                Context context = getApplicationContext();
                Toast toast = Toast.makeText(context, getString(R.string.error_internet_sms), Toast.LENGTH_LONG);
                toast.show();
            }
        }

        @Override
        protected void onCancelled() {
            mSendCodeTask = null;
            showCodeProgress(false, false);
        }
    }

    /**
     * Represents an asynchronous register user task
     */
    public class RegisterTask extends AsyncTask<Void, Void, Boolean> {

        private final String mPhone;
        private final String mPin;
        private final String mCode;

        RegisterTask(String phone, String pin, String code) {
            mPhone = phone;
            mPin = pin;
            mCode = code;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt register against a network service.

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
            mRegisterTask = null;
            Context context = getApplicationContext();

            if (success) {
                showFullProgress(false);
                Toast toast = Toast.makeText(context, getString(R.string.register_success), Toast.LENGTH_LONG);
                toast.show();
            } else {
                showFullProgress(false);
                Toast toast = Toast.makeText(context, getString(R.string.enter_internet_register), Toast.LENGTH_LONG);
                toast.show();
                startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                finish();
            }
        }

        @Override
        protected void onCancelled() {
            mRegisterTask = null;
            showFullProgress(false);
        }
    }

    /**
     * Represents an asynchronous task to show resend button and description
     */
    public class ShowResendTask extends AsyncTask<Void, Void, Boolean> {

        ShowResendTask() { }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                // Wait 1 minute
                Thread.sleep(60 * 1000);
            } catch (InterruptedException e) {
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mShowResendTask = null;

            if (success) {
                int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
                setAnimatedHide(false, shortAnimTime, mResendDescription);
                setAnimatedHide(false, shortAnimTime, mResendButton);
            }
        }

        @Override
        protected void onCancelled() {
            mSendCodeTask = null;
        }
    }
}

