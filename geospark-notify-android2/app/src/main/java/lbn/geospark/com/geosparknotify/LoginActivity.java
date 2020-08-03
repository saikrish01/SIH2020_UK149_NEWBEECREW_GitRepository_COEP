package lbn.geospark.com.geosparknotify;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.geospark.lib.GeoSpark;
import com.geospark.lib.callback.GeoSparkCallBack;
import com.geospark.lib.model.GeoSparkError;
import com.geospark.lib.model.GeoSparkUser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {
    private EditText mEdt_Email, mEdt_Password;
    private FirebaseAuth mFirebaseAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GeoSpark.initialize(getApplication(), "YOUR-PUBLISHABLE-KEY");
        FirebaseApp.initializeApp(LoginActivity.this);
        mFirebaseAuth = FirebaseAuth.getInstance();
        if (mFirebaseAuth != null && mFirebaseAuth.getCurrentUser() != null) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        } else {
            setContentView(R.layout.login_activity);
            mEdt_Email = (EditText) findViewById(R.id.edt_email);
            mEdt_Password = (EditText) findViewById(R.id.edt_password);
            Button btn_Login = (Button) findViewById(R.id.btn_login);
            Button btn_Register = (Button) findViewById(R.id.btn_register);
            Button btn_newPassword = (Button) findViewById(R.id.btn_forgot_password);
            btn_Login.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String email = mEdt_Email.getText().toString();
                    String password = mEdt_Password.getText().toString();
                    if (TextUtils.isEmpty(email)) {
                        Toast.makeText(getApplicationContext(), "Please enter e-mail", Toast.LENGTH_SHORT).show();
                    } else if (TextUtils.isEmpty(password)) {
                        Toast.makeText(getApplicationContext(), "Please enter password", Toast.LENGTH_SHORT).show();
                    } else {
                        authWithUserPassword(email, password);
                    }
                }
            });
            btn_Register.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
                }
            });
            btn_newPassword.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(getApplicationContext(), ForgotPasswordActivity.class));
                }
            });
        }
    }

    private void authWithUserPassword(String username, String password) {
        mFirebaseAuth.signInWithEmailAndPassword(username, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("users");
                    mDatabase.orderByChild("email").equalTo(username).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot datas : dataSnapshot.getChildren()) {
                                String userId = datas.child("geospark_token").getValue().toString();
                                GeoSpark.getUser(LoginActivity.this, userId, new GeoSparkCallBack() {
                                    @Override
                                    public void onSuccess(GeoSparkUser geoSparkUser) {
                                        Log.e("Login Success", geoSparkUser.getUserId());
                                        Intent i = new Intent(getApplicationContext(), MainActivity.class);
                                        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(i);
                                        finish();
                                    }

                                    @Override
                                    public void onFailure(GeoSparkError geoSparkError) {
                                        Log.e("Login Error", geoSparkError.getErrorMessage());
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                } else {
                    Toast.makeText(getApplicationContext(), "Auth Error", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
