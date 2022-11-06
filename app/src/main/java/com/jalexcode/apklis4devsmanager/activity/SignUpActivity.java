package com.jalexcode.apklis4devsmanager.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;

import com.jalexcode.apklis4devsmanager.R;

public class SignUpActivity extends AppCompatActivity {
    private Spinner provincesSpinner;
    private ImageView signUpBackButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        // provinces
        provincesSpinner = findViewById(R.id.provinces_spinner);
        ArrayAdapter<CharSequence> academicCategorySpinnerAdapter  = ArrayAdapter.createFromResource(this, R.array.provinces, android.R.layout.simple_list_item_1);
        provincesSpinner.setAdapter(academicCategorySpinnerAdapter);
        // back button
        signUpBackButton = findViewById(R.id.signUpBackButton);
        signUpBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }
}