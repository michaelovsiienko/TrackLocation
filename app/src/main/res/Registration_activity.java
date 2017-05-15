package com.example.mykhail.tracklocationv20;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.mobsandgeeks.saripaar.annotation.Password;

import java.util.ArrayList;
import java.util.List;

public class Registration_activity extends AppCompatActivity implements View.OnClickListener, Validator.ValidationListener {
    private Toolbar mToolbar;
    private Firebase mFirebaseRef;

    private ArrayList<String> listNumbers;

    private FloatingActionButton mActionButtonDone;

    @NotEmpty
    private EditText mEditTextNumber;

    @NotEmpty
    @Password
    private EditText mEditTextPassword;

    private ViewPropertyAnimatorCompat mViewAnimator;

    private boolean mIsPhoneConfirm;

    private Validator mValidator;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle(getResources().getString(R.string.registration));
        setSupportActionBar(mToolbar);

        Firebase.setAndroidContext(getApplicationContext());
        mFirebaseRef = new Firebase(Constants.DATABASE_URL);

        mActionButtonDone = (FloatingActionButton) findViewById(R.id.doneButton_registration);
        mActionButtonDone.setOnClickListener(this);
        mEditTextNumber = (EditText) findViewById(R.id.numberPhone_registration);
        mEditTextPassword = (EditText) findViewById(R.id.password_registration);

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
        mValidator = new Validator(this);
        mValidator.setValidationListener(this);

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }

    public void registration() {
        if (listNumbers != null) {
            if (!listNumbers.contains(mEditTextNumber.getText().toString())) {
                mFirebaseRef.child(mEditTextNumber.getText().toString()).child(Constants.PASSWORD).setValue(mEditTextPassword.getText().toString());
                mFirebaseRef.child(mEditTextNumber.getText().toString()).child(Constants.LONGITUDE).setValue(30);
                mFirebaseRef.child(mEditTextNumber.getText().toString()).child(Constants.LATITUDE).setValue(30);
                mFirebaseRef.child(mEditTextNumber.getText().toString()).child(Constants.GROUPS).child("1").setValue("Друзья");
                mFirebaseRef.child(mEditTextNumber.getText().toString()).child(Constants.GROUPS).child("2").setValue("Семья");
                mFirebaseRef.child(mEditTextNumber.getText().toString()).child(Constants.GROUPS).child("3").setValue("Работа");
                mFirebaseRef.child(mEditTextNumber.getText().toString()).child(Constants.STATUS).setValue("online");

                Intent intent = getIntent();
                intent.putExtra("number", mEditTextNumber.getText().toString());
                setResult(RESULT_OK, intent);
                finish();
            } else {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.hint), Toast.LENGTH_SHORT).show();
                mEditTextNumber.setEnabled(true);
                mEditTextNumber.setFocusable(true);
                mEditTextNumber.setFocusableInTouchMode(true);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.doneButton_registration:
                confirmData();
                break;
        }

    }

    private void confirmData() {
        if (!mIsPhoneConfirm) {
            confirmPhoneNumber();
            return;
        }
        mValidator.validate();
    }

    private void updateUi() {
        showRegistrationCodeView();
        changeConfirmButton();
    }

    private void showRegistrationCodeView() {
        View registrationCodeView = findViewById(R.id.password_registrationlayout);
        if (registrationCodeView!=null)
        registrationCodeView.setVisibility(View.VISIBLE);

        mViewAnimator = ViewCompat.animate(registrationCodeView);
        mViewAnimator.setDuration(300).alpha(1);

        mEditTextNumber.setEnabled(false);
        mEditTextNumber.setFocusable(false);
        mEditTextNumber.setFocusableInTouchMode(false);
    }

    private void changeConfirmButton() {
        mViewAnimator = ViewCompat.animate(mActionButtonDone);
        mViewAnimator.setDuration(300).alpha(0).withEndAction(new Runnable() {
            @Override
            public void run() {
                mActionButtonDone.setImageDrawable(
                        getResources().getDrawable(R.drawable.ic_done_all)
                );
                mViewAnimator.setDuration(300).alpha(1);
            }
        });
    }

    private void confirmPhoneNumber() {
        if (!mEditTextNumber.getText().toString().isEmpty()) {
            mIsPhoneConfirm = true;
            mEditTextPassword.setText(PasswordGenerate.generatePass());
            updateUi();
        }
        else
            Toast.makeText(this,getString(R.string.infoString),Toast.LENGTH_LONG).show();
    }

    @Override
    public void onValidationSucceeded() {
        if (!mEditTextPassword.getText().toString().isEmpty())
        registration();
        else
            Toast.makeText(this,getString(R.string.infoString),Toast.LENGTH_LONG).show();
    }

    @Override
    public void onValidationFailed(List<ValidationError> errors) {
            Toast.makeText(this,errors.get(0).getCollatedErrorMessage(this),Toast.LENGTH_LONG).show();
    }
}
