package com.tracklocation;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

public class Login_activity extends AppCompatActivity implements View.OnClickListener {
    private Toolbar mToolbar;

    private Firebase mFirebaseRef;

    private TextInputLayout mInputNumber;
    private TextInputLayout mInputPassword;
    private EditText mEditTextNumber;
    private EditText mEditTextPassword;
    private Button mButtonLogin;
    private Button mButtonRegistration;

    public static String mUserPhoneNumber;
    private SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Firebase.setAndroidContext(getApplicationContext());
        mToolbar = (Toolbar) findViewById(R.id.include);
        mToolbar.setTitle(getResources().getString(R.string.login));
        setSupportActionBar(mToolbar);
        mFirebaseRef = new Firebase(Constants.DATABASE_URL);
        mInputNumber = (TextInputLayout) findViewById(R.id.input_name_login_layout);
        mInputPassword = (TextInputLayout) findViewById(R.id.input_password_login_layout);
        mEditTextNumber = (EditText) findViewById(R.id.input_number_login);
        mEditTextPassword = (EditText) findViewById(R.id.input_password_login);
        mButtonLogin = (Button) findViewById(R.id.button_login);
        mButtonRegistration = (Button) findViewById(R.id.button_registration);
        mButtonLogin.setOnClickListener(this);
        mButtonRegistration.setOnClickListener(this);
        mEditTextNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mInputNumber.setHint(getResources().getString(R.string.number));
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        mEditTextPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mInputPassword.setHint(getResources().getString(R.string.password_generate));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.button_login:
                mFirebaseRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child(mEditTextNumber.getText().toString()).exists()) {
                            String password = dataSnapshot.child(mEditTextNumber.getText().toString()).child(Constants.PASSWORD).getValue().toString();
                            String buff = mEditTextPassword.getText().toString();
                            if (password.equals(buff)) {
                                Intent intent = getIntent();
                                intent.putExtra("number", mEditTextNumber.getText().toString());
                                setResult(RESULT_OK, intent);
                                finish();
                            } else {
                                mInputPassword.setHint(getResources().getString(R.string.incorrect_password));
                            }

                        } else
                            mInputNumber.setHint(getResources().getString(R.string.incorrect_number));
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });

                break;
            case R.id.button_registration:
                startActivityForResult(new Intent(this, Registration_activity.class), 1);

                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == 1) {
            mUserPhoneNumber = data.getStringExtra("number");
            mSharedPreferences = getPreferences(MODE_PRIVATE);
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putBoolean("login", false);
            editor.putString("number", mUserPhoneNumber);
            editor.apply();
            Intent intent = getIntent();
            intent.putExtra("number", mUserPhoneNumber);
            setResult(RESULT_OK, intent);
            finish();
        }
    }
}
