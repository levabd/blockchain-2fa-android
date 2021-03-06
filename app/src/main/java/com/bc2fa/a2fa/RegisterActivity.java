package com.bc2fa.a2fa;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.annotation.RequiresApi;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.github.gfx.util.encrypt.EncryptedSharedPreferences;
import com.github.gfx.util.encrypt.Encryption;
import com.google.firebase.iid.FirebaseInstanceId;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.IOException;
import java.util.Objects;

/**
 * Created by Oleg Levitsky
 */

public class RegisterActivity extends AppCompatActivity {
    @SuppressWarnings("unused")
    private static final String TAG = "RegisterActivity";

    SharedPreferences mPrefs;

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
    private String pushToken;

    final boolean[] requestFailure = {false};

    @SuppressLint("Assert")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);

        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);

        Bundle b = getIntent().getExtras();
        int restore = -1; // or other values
        if(b != null)
            restore = b.getInt("restore");

        // Set up the form form.
        mPinView = findViewById(R.id.pinEdit);
        mPinLayoutView = findViewById(R.id.pinEditLayout);
        mPinDescriptionView = findViewById(R.id.pinDescription);
        mPhoneView = findViewById(R.id.phoneEdit);
        mPhoneDescriptionView = findViewById(R.id.phoneDescription);
        mCodeView = findViewById(R.id.codeEdit);
        mCodeDescriptionView = findViewById(R.id.code_description_text);
        mResendDescription = findViewById(R.id.didntReceiveCode);

        if (restore > 0) { // Restore PIN, not register
            this.setTitle(getString(R.string.title_activity_restore));
            mPinView.setHint(R.string.restore_pin_small);
        } else {
            mPinView.setHint(R.string.enter_pin_small);
        }

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

        // Application preference
        mPrefs = new EncryptedSharedPreferences(Encryption.getDefaultCipher(), this);

        String phone = mPrefs.getString("phone", "-1");
        if (!Objects.equals(phone, "-1")) {
            mPhoneView.setText(phone);
        }
    }

    // Successfully registered
    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    private void attemptStart() {
        String code = mCodeView.getText().toString();
        phone = mPhoneView.getText().toString();

        if (mShowResendTask != null) {
            mShowResendTask.cancel(true);
        }
        if (mSendCodeTask != null) {
            mSendCodeTask.cancel(true);
        }
        showFullProgress(true);

        mRegisterTask = new RegisterTask(phone, pin, code);
        mRegisterTask.execute((Void) null);
    }

    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
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
        return phone.matches("^[+]?[0-9]{10,14}$");
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

    private void showModal(String message) {
        AlertDialog.Builder alert = new AlertDialog.Builder(RegisterActivity.this);
        // alert.setTitle("Modal");
        alert.setMessage(message);

        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Nothing to fire
            }
        });

        alert.show();
    }

    private void registerSuccess(String phone2Save, String pin2Save) {
        mPrefs.edit()
                .putString("phone", phone2Save)
                .putString("pin", pin2Save)
                .putString("pushToken", pushToken)
                .apply();
        Toast toast = Toast.makeText(this, getString(R.string.register_success), Toast.LENGTH_LONG);
        toast.show();
        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
        finish();
    }


    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getExtras() != null) {
                showModal(intent.getExtras().getString("message"));
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

    /**
     * Represents an asynchronous send code task used to register the user.
     */
    @SuppressLint("StaticFieldLeak")
    public class SendCodeTask extends AsyncTask<Void, Void, Boolean> {

        private final String mPhone;
        private final String mPin;

        SendCodeTask(String phone, String pin) {
            mPhone = phone;
            mPin = pin;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            // Simulate network access.
            // Thread.sleep(2000);
            mPhoneView.setError(null);
            requestFailure[0] = false;
            Long tsLong = System.currentTimeMillis()/1000;
            String ts = tsLong.toString();
            App.getApi().getVerifyNumber(mPhone, ts).enqueue(new Callback<DummyDTO>() {
            //App.getApi().postVerifyNumber(new PostVerifyNumberDTO(mPhone, "", 1111)).enqueue(new Callback<DummyDTO>() {
                @Override
                public void onResponse(Call<DummyDTO> call, Response<DummyDTO> response) {
                    if (!response.isSuccessful()) {
                        //request not successful (like 400,401,403 etc)
                        requestFailure[0] = true;
                        String errorMsg;
                        if (response.code() == 404) {
                            mPhoneView.setError("Пользователь с таким номером не зарегистрирован в системе");
                            errorMsg = "Пользователь с таким номером не зарегистрирован в системе";
                        } else {
                            errorMsg = "Не удалось совершить запрос. Проверьте параметры и попробуйте позже.";
                        }
                        showCodeProgress(false, false);
                        showModal(errorMsg);
                        //showModal(getString(R.string.error_internet_sms));
                    } else {
                        showCodeProgress(false, true);
                    }

                }

                @Override
                public void onFailure(Call<DummyDTO> call, Throwable t) {
                    requestFailure[0] = true;
                    showModal("Проблемы с сетевым соединением. Попробуйте чуть позже");
                    showCodeProgress(false, true);
                    // Toast.makeText(RegisterActivity.this, "Проблемы с сетевым соединением. Попробуйте чуть позже", Toast.LENGTH_LONG).show();
                }
            });

            return !requestFailure[0];
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mSendCodeTask = null;
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
    @SuppressLint("StaticFieldLeak")
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

            // Simulate network access.
            // Thread.sleep(2000);
            pushToken = FirebaseInstanceId.getInstance().getToken();
            requestFailure[0] = false;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mPhoneView.setError(null);
                    mCodeView.setError(null);
                }
            });

            Integer code;

            try {
                code = Integer.parseInt(mCode);
            } catch (NumberFormatException e) {
                String errorMsg = "Код подтверждения не может быть пустым";
                mCodeView.setError(errorMsg);
                return false;
            }

            Log.d("Blockchain", "Token: " + pushToken);

            App.getApi().postVerifyNumber(new PostVerifyNumberDTO(mPhone, pushToken, code)).enqueue(new Callback<DummyDTO>() {
                @Override
                public void onResponse(Call<DummyDTO> call, Response<DummyDTO> response) {
                    //showModal("Success: " + response.code()); Thread sleep ??
                    if (!response.isSuccessful()) {
                        //request not successful (like 400,401,403 etc)
                        requestFailure[0] = true;
                        String errorMsg;
                        if (response.code() == 422) {
                            errorMsg = "Код подтверждения устарел или неверен";
                            mCodeView.setError(errorMsg);
                        } else if (response.code() == 404) {
                            errorMsg = "Пользователь с таким номером не зарегистрирован в системе";
                            mPhoneView.setError(errorMsg);
                        } else {
                            errorMsg = "Не удалось совершить запрос. Проверьте параметры и попробуйте позже.";
                        }
                        showFullProgress(false);
                        showModal(errorMsg);
                    } else {
                        showFullProgress(false);
                        registerSuccess(mPhone, mPin);
                    }
                }

                @Override
                public void onFailure(Call<DummyDTO> call, Throwable t) {
                    //showModal("Failed: " + t.getMessage()); Thread sleep ??
                    requestFailure[0] = true;
                    showFullProgress(false);
                    showModal("Проблемы с сетевым соединением. Попробуйте чуть позже");
                }
            });

            return !requestFailure[0];
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mRegisterTask = null;
            showFullProgress(false);
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
    @SuppressLint("StaticFieldLeak")
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

