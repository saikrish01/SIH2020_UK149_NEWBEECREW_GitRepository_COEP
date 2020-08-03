package lbn.geospark.com.geosparknotify;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.geospark.lib.GeoSpark;
import com.geospark.lib.callback.GeoSparkCallBack;
import com.geospark.lib.model.GeoSparkError;
import com.geospark.lib.model.GeoSparkUser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {
    private EditText mEdt_Email;
    private EditText mEdt_Password;
    private FirebaseAuth mFirebaseAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_activity);
        mFirebaseAuth = FirebaseAuth.getInstance();
        mEdt_Email = (EditText) findViewById(R.id.edt_email);
        mEdt_Password = (EditText) findViewById(R.id.edt_password);
        TextView back = (TextView) findViewById(R.id.txt_back);
        Button btn_register = (Button) findViewById(R.id.btn_register);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mEdt_Email.getText().toString();
                String password = mEdt_Password.getText().toString();
                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplicationContext(), "Please fill in the required fields", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(password) || password.length() < 6) {
                    Toast.makeText(getApplicationContext(), "Please enter password or must be at least 6 characters", Toast.LENGTH_SHORT).show();
                } else {
                    mFirebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("users");
                                String userId = mDatabase.push().getKey();
                                mDatabase.child(userId).child("email").setValue(email);
                                mDatabase.child(userId).child("device_token").setValue(SharedPreference.getToken(RegisterActivity.this));
                                GeoSpark.createUser(RegisterActivity.this, email, new GeoSparkCallBack() {
                                    @Override
                                    public void onSuccess(GeoSparkUser geoSparkUser) {
                                        mDatabase.child(userId).child("geospark_token").setValue(geoSparkUser.getUserId());
                                        Intent i = new Intent(getApplicationContext(), MainActivity.class);
                                        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(i);
                                        finish();
                                    }

                                    @Override
                                    public void onFailure(GeoSparkError geoSparkError) {

                                    }
                                });
                            } else {
                                Toast.makeText(getApplicationContext(), "e-mail or password is wrong", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }
}
