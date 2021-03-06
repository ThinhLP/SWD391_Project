package com.thinhlp.cocshopapp.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.thinhlp.cocshopapp.R;
import com.thinhlp.cocshopapp.commons.ApiUtils;
import com.thinhlp.cocshopapp.commons.Const;
import com.thinhlp.cocshopapp.entities.User;
import com.thinhlp.cocshopapp.services.UserService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private UserService userService;
    private EditText edtUsername;
    private EditText edtPassword;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get service
        userService = ApiUtils.getUserService();
        setContentView(R.layout.activity_login);
        edtUsername = (EditText) findViewById(R.id.edtUsername);
        edtPassword = (EditText) findViewById(R.id.edtPassword);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        // Set event
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkLogin();
            }
        });
    }

    public void checkLogin() {
        String username = edtUsername.getText().toString();
        String password = edtPassword.getText().toString();
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(getBaseContext(), "Username and password must be filled", Toast.LENGTH_SHORT).show();
            return;
        }
        final ProgressDialog dialog = ProgressDialog.show(this, "Loading", "Please wait...", true);
        userService.checkLogin(username, password).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {

                int statusCode = response.code();
                switch (statusCode) {
                    case Const.HTTP_STATUS.OK:
                        User user = response.body();
                        storeUserInfo(user);
                        // Authorize
                        switch (user.getRole()) {
                            case Const.ROLE.CUSTOMER:
                                switchToCustomerActivity();
                                break;
                            case Const.ROLE.EMPLOYEE:
                            case Const.ROLE.ADMIN:
                                switchToEmployeeActivity();
                                break;
                        }
                        break;
                    case Const.HTTP_STATUS.UNAUTHORIZED:
                        makeToast("Login failed");
                        break;
                }
                dialog.dismiss();

            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                dialog.dismiss();
                makeToast("Can't connect to server");
            }
        });
    }

    public void makeToast(String msg) {
        Toast.makeText(this.getBaseContext(), msg, Toast.LENGTH_SHORT).show();
    }

    public void signup(View view) {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
    }

    public void switchToCustomerActivity() {
        Intent i = new Intent(this, CustomerActivity.class);
        startActivity(i);
    }

    public void switchToEmployeeActivity() {
        Intent i = new Intent(this, EmployeeActivity.class);
        startActivity(i);
    }

    private void storeUserInfo(User user) {
        SharedPreferences sp = getSharedPreferences(Const.APP_SHARED_PREFERENCE.SP_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        editor.putInt(Const.APP_SHARED_PREFERENCE.KEY_USER_ID, user.getUserId());
        editor.putString(Const.APP_SHARED_PREFERENCE.KEY_FULLNAME, user.getFirstname() + " " + user.getLastname());
        editor.putInt(Const.APP_SHARED_PREFERENCE.KEY_USER_ROLE, user.getRole());
        editor.commit();
    }

}
