package com.bc2fa.a2fa;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.github.gfx.util.encrypt.EncryptedSharedPreferences;
import com.github.gfx.util.encrypt.Encryption;
import com.kevalpatel.passcodeview.KeyNamesBuilder;
import com.kevalpatel.passcodeview.PinView;
import com.kevalpatel.passcodeview.indicators.CircleIndicator;
import com.kevalpatel.passcodeview.interfaces.AuthenticationListener;
import com.kevalpatel.passcodeview.keys.RoundKey;

/**
 * Created by Oleg Levitsky
 */

public class PinViewActivity extends AppCompatActivity {
    private static final String ARG_CURRENT_PIN = "current_pin";

    SharedPreferences mPrefs;

    private PinView mPinView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pinview);

        mPinView = findViewById(R.id.pattern_view);
        Button mRestoreButton = findViewById(R.id.restoreButton);
        mRestoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PinViewActivity.this, RegisterActivity.class);
                Bundle b = new Bundle();
                b.putInt("restore", 1); //Your id
                intent.putExtras(b); //Put your id to your next Intent
                startActivity(intent);
            }
        });

        Button mRegisterButton = findViewById(R.id.signUpButton);
        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PinViewActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        //Build the desired key shape and pass the theme parameters.
        //REQUIRED
        mPinView.setKey(new RoundKey.Builder(mPinView)
                .setKeyPadding(R.dimen.key_padding)
                .setKeyStrokeColorResource(R.color.colorPrimaryDarkest)
                .setKeyStrokeWidth(R.dimen.key_stroke_width)
                .setKeyTextColorResource(R.color.colorPrimaryDarkest)
                .setKeyTextSize(R.dimen.key_text_size)
                .build());

        //Build the desired indicator shape and pass the theme attributes.
        //REQUIRED
        mPinView.setIndicator(new CircleIndicator.Builder(mPinView)
                .setIndicatorRadius(R.dimen.indicator_radius)
                .setIndicatorFilledColorResource(R.color.colorPrimaryDarkest)
                .setIndicatorStrokeColorResource(R.color.colorPrimaryDarkest)
                .setIndicatorStrokeWidth(R.dimen.indicator_stroke_width)
                .build());

        //Set the name of the keys based on your locale.
        //OPTIONAL. If not passed key names will be displayed based on english locale.
        mPinView.setKeyNames(new KeyNamesBuilder()
                .setKeyOne(this, R.string.key_1)
                .setKeyTwo(this, R.string.key_2)
                .setKeyThree(this, R.string.key_3)
                .setKeyFour(this, R.string.key_4)
                .setKeyFive(this, R.string.key_5)
                .setKeySix(this, R.string.key_6)
                .setKeySeven(this, R.string.key_7)
                .setKeyEight(this, R.string.key_8)
                .setKeyNine(this, R.string.key_9)
                .setKeyZero(this, R.string.key_0));

        mPinView.setAuthenticationListener(new AuthenticationListener() {
            @Override
            public void onAuthenticationSuccessful() {
                //User authenticated successfully.
                //Navigate to secure screens.
                startActivity(new Intent(PinViewActivity.this, MainActivity.class));
                // finish();
            }

            @Override
            public void onAuthenticationFailed() {
                //Calls whenever authentication is failed or user is unauthorized.
                //Do something
            }
        });

    }

    @Override
    protected void onResume() {
        // Application preference
        super.onResume();
        mPrefs = new EncryptedSharedPreferences(Encryption.getDefaultCipher(), this);

        //Set the correct pin code.
        //REQUIRED
        String pin = mPrefs.getString("pin", "-1");
        if (Integer.parseInt(pin) < 0) {
            findViewById(R.id.welcome).setVisibility(View.VISIBLE);
            findViewById(R.id.pinEnter).setVisibility(View.GONE);
            //mPinView.setCorrectPin(new int[]{1, 2, 3, 4});
        } else {
            findViewById(R.id.welcome).setVisibility(View.GONE);
            findViewById(R.id.pinEnter).setVisibility(View.VISIBLE);
            mPinView.setCorrectPin(new int[]{
                    Character.getNumericValue(pin.charAt(0)),
                    Character.getNumericValue(pin.charAt(1)),
                    Character.getNumericValue(pin.charAt(2)),
                    Character.getNumericValue(pin.charAt(3))});
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putIntArray(ARG_CURRENT_PIN, mPinView.getCurrentTypedPin());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mPinView.setCurrentTypedPin(savedInstanceState.getIntArray(ARG_CURRENT_PIN));
    }
}
