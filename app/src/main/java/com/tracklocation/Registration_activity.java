package com.tracklocation;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;

public class Registration_activity extends AppCompatActivity {
    private Toolbar mToolbar;
    private Firebase mFirebaseRef;
    private TextInputLayout mInputNumber;
    private EditText mEditTextNumber;
    private EditText mEditTextNickname;
    private TextView mTextViewPassword;
    private Button mButtonGenerate;
    private Button mButtonRegistration;
    private boolean isGenerate = false;
    private ArrayList<String> listNumbers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle(getResources().getString(R.string.registration));
        setSupportActionBar(mToolbar);
        mEditTextNickname = (EditText) findViewById(R.id.editText2);
        mEditTextNumber = (EditText) findViewById(R.id.editText);
        mTextViewPassword = (TextView) findViewById(R.id.textView5);
        mButtonGenerate = (Button) findViewById(R.id.button);
        mButtonRegistration = (Button) findViewById(R.id.button2);
        mInputNumber = (TextInputLayout) findViewById(R.id.input_name_layout);
        mButtonRegistration.setEnabled(false);
        mButtonGenerate.setEnabled(false);
        Firebase.setAndroidContext(getApplicationContext());
        mFirebaseRef = new Firebase("https://boiling-torch-9376.firebaseio.com/");

        listNumbers = new ArrayList<>();
        mFirebaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listNumbers.clear();
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    listNumbers.add(child.getKey());
                }
            }
            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        mEditTextNickname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (mEditTextNumber.getTextSize() != 0)
                    mButtonGenerate.setEnabled(true);
            }
        });
        mEditTextNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mInputNumber.setHint(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button:
                mTextViewPassword.setText(generatePassword());
                mButtonRegistration.setEnabled(true);
                break;
            case R.id.button2:
                if (!listNumbers.contains(mEditTextNumber.getText().toString())) {
                    registration();
                    finish();
                } else {
                    mInputNumber.setHint(getResources().getString(R.string.hint));
                }
                break;
        }
    }

    public String generatePassword() {
        String password;
        password = PasswordGenerate.generatePass();
        return password;
    }

    public void registration() {
        mFirebaseRef.child(mEditTextNumber.getText().toString()).child(Constants.PASSWORD).setValue(mTextViewPassword.getText());
        mFirebaseRef.child(mEditTextNumber.getText().toString()).child(Constants.NICKNAME).setValue(mEditTextNickname.getText().toString());
        mFirebaseRef.child(mEditTextNumber.getText().toString()).child(Constants.LONGITUDE).setValue(30);
        mFirebaseRef.child(mEditTextNumber.getText().toString()).child(Constants.LATITUDE).setValue(30);

        mFirebaseRef.child(mEditTextNumber.getText().toString()).child(Constants.GROUPS).child("1").setValue("Друзья");
        mFirebaseRef.child(mEditTextNumber.getText().toString()).child(Constants.GROUPS).child("2").setValue("Семья");
        mFirebaseRef.child(mEditTextNumber.getText().toString()).child(Constants.GROUPS).child("3").setValue("Работа");

        Intent intent = getIntent();
        intent.putExtra("number", mEditTextNumber.getText().toString());
        setResult(RESULT_OK, intent);
        finish();
    }
}
