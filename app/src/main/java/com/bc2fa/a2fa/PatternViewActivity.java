package com.bc2fa.a2fa;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.kevalpatel.passcodeview.PatternView;
import com.kevalpatel.passcodeview.interfaces.AuthenticationListener;
import com.kevalpatel.passcodeview.patternCells.CirclePatternCell;
import com.kevalpatel.passcodeview.patternCells.PatternPoint;

public class PatternViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pattern_view);

        PatternView patternView = (PatternView) findViewById(R.id.pattern_view);

        //Set number of pattern counts.
        //REQUIRED
        patternView.setNoOfColumn(3);   //Number of columns
        patternView.setNoOfRows(3);     //Number of rows

        //Set the correct pin code.
        //Display row and column number of the pattern point sequence.
        //REQUIRED
        patternView.setCorrectPattern(new PatternPoint[]{
                new PatternPoint(0, 0),
                new PatternPoint(1, 0),
                new PatternPoint(2, 0),
                new PatternPoint(2, 1)
        });

        //Build the desired indicator shape and pass the theme attributes.
        //REQUIRED
        patternView.setPatternCell(new CirclePatternCell.Builder(patternView)
                .setRadius(R.dimen.pattern_cell_radius)
                .setCellColorResource(R.color.colorAccent)
                .build());

        patternView.setAuthenticationListener(new AuthenticationListener() {
            @Override
            public void onAuthenticationSuccessful() {
                //User authenticated successfully.
                //Navigate to secure screens.
                startActivity(new Intent(PatternViewActivity.this, AuthenticatedActivity.class));
                finish();
            }

            @Override
            public void onAuthenticationFailed() {
                //Calls whenever authentication is failed or user is unauthorized.
                //Do something
            }
        });
    }
}
