package com.example.jc.personalaccount;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;

public class RegisterActivity extends AppCompatActivity {

    private Context mContext = this;
    private UserRegisterTask mAuthTask = null;

    private View mProgressView;
    private View mRegistView;

    private AutoCompleteTextView mUserView;
    private EditText mPasswordView;
    private EditText mRePasswordView;
    private AutoCompleteTextView mEmailView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mProgressView = (View) findViewById(R.id.register_repassword);
        mRegistView = (View) findViewById(R.id.register_form_LL);
        mUserView = (AutoCompleteTextView) findViewById(R.id.regist_user);
        mPasswordView = (EditText) findViewById(R.id.register_password);
        mRePasswordView = (EditText) findViewById(R.id.register_repassword);
        mEmailView = (AutoCompleteTextView) findViewById(R.id.register_email);

        Button mSignInButton = (Button) findViewById(R.id.register_sign_in_button);
        mSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this,LoginActivity.class);
                startActivity(intent);
            }
        });

        Button mRegisterButton = (Button) findViewById(R.id.register_register_button);
        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptRegister();
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mRegistView.setVisibility(show ? View.GONE : View.VISIBLE);
            mRegistView.animate().setDuration(shortAnimTime)
                    .alpha(show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mRegistView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime)
                    .alpha(show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mRegistView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private void attemptRegister() {
        if (mAuthTask != null) {
            return;
        }

        //Reset errors
        mUserView.setError(null);
        mPasswordView.setError(null);
        mRePasswordView.setError(null);
        mEmailView.setError(null);

        String user = mUserView.getText().toString();
        String password = mPasswordView.getText().toString();
        String repassword = mRePasswordView.getText().toString();
        String email = mEmailView.getText().toString();

        mAuthTask = new UserRegisterTask(user, password, email);
        mAuthTask.execute((Void) null);
    }

    //异步执行注册功能
    public class UserRegisterTask extends AsyncTask<Void, Void, Boolean> {

        private final String mUser;
        private final String mPassword;
        private final String mEmail;

        UserRegisterTask(String user, String password, String email) {
            mUser = user;
            mPassword = password;
            mEmail = email;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }

            return GlobalData.DataStoreHelper.register(mUser, mPassword, mEmail);
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
//                finish();
            } else {
                new AlertDialog.Builder(mContext).setTitle(getString(R.string.common_str_information))
                        .setMessage(getString(R.string.error_register_failed))
                        .setPositiveButton(getString(R.string.common_btn_ok),null)
                        .show();
            }
        }

    }
}
