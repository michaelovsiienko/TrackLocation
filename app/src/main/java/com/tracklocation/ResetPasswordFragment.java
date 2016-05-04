package com.tracklocation;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.mobsandgeeks.saripaar.annotation.Password;

import java.util.List;

/**
 * Created by mykhail on 03.05.16.
 */
public class ResetPasswordFragment extends DialogFragment implements DialogInterface.OnClickListener, View.OnClickListener, Validator.ValidationListener {
    private View mView;

    private Button mGeneratePassword;
    private TextInputLayout mNewPasswordInputLayout;
    @NotEmpty
    @Password
    private EditText mNewPasswordText;

    private Firebase mFirebaseRef;

    private AlertDialog mDialog;
    private Validator mValidator;

    public static ResetPasswordFragment newInstance() {
        ResetPasswordFragment resetPasswordFragment = new ResetPasswordFragment();
        return resetPasswordFragment;
    }

    public Dialog onCreateDialog(Bundle bundle) {
        Firebase.setAndroidContext(getActivity());
        mFirebaseRef = new Firebase(Constants.DATABASE_URL);
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();

        mView = layoutInflater.inflate(R.layout.fragment_resetpassword, null);
        mNewPasswordInputLayout = (TextInputLayout) mView.findViewById(R.id.password_fragmentResetPasswordLayout);
        mNewPasswordText = (EditText) mView.findViewById(R.id.newPassword_fragmentResetPassword);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(getResources().getString(R.string.chanche_password))
                .setView(mView)
                .setNegativeButton(R.string.cancel_dialogfragment, this)
                .setPositiveButton(R.string.update, this);

        mGeneratePassword = (Button) mView.findViewById(R.id.generatePassword_fragmentResetPassword);
        mGeneratePassword.setOnClickListener(this);

        mValidator = new Validator(this);
        mValidator.setValidationListener(this);

        return builder.create();

    }

    public void onResume() {
        super.onResume();
        mDialog = (AlertDialog) getDialog();
        mDialog.getButton(Dialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mValidator.validate();
            }
        });

        mNewPasswordText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
            }
        });
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
    }

    @Override
    public void onClick(View v) {
        mNewPasswordText.setText(PasswordGenerate.generatePass());
    }

    @Override
    public void onValidationSucceeded() {
        Toast.makeText(getActivity(), getResources().getString(R.string.succes_reset), Toast.LENGTH_LONG).show();
        mFirebaseRef
                .child(Singleton.getInstance()
                        .getUserPhone())
                .child(Constants.PASSWORD)
                .setValue(mNewPasswordText.getText().toString());
        mDialog.dismiss();
    }

    @Override
    public void onValidationFailed(List<ValidationError> errors) {
        Toast.makeText(getActivity(), errors.get(0).getCollatedErrorMessage(getActivity()), Toast.LENGTH_LONG).show();

    }
}
