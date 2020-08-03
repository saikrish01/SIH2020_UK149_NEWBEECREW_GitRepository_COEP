package lbn.geospark.com.geosparknotify;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.geospark.lib.GeoSpark;
import com.geospark.lib.callback.GeoSparkLogoutCallBack;
import com.geospark.lib.model.GeoSparkError;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        ignoreBatteryOptimization(this);
        TextView txt_Label = (TextView) findViewById(R.id.txt_label);
        Button btn_DeleteUser = (Button) findViewById(R.id.btn_delete);
        Button btn_Logout = (Button) findViewById(R.id.btn_logout);
        mFirebaseAuth = FirebaseAuth.getInstance();
        final FirebaseUser user = mFirebaseAuth.getCurrentUser();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    Intent i = new Intent(getApplicationContext(), MainActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                    finish();
                }
            }
        };
        if (user != null && user.getEmail() != null) {
            txt_Label.setText("Hi " + user.getEmail());
        }
        btn_DeleteUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (user != null) {
                    user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                GeoSpark.stopTracking(MainActivity.this);
                                Toast.makeText(getApplicationContext(), "User deleted", Toast.LENGTH_SHORT).show();
                                Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(i);
                                finish();
                            }
                        }
                    });
                }
            }
        });
        btn_Logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GeoSpark.logout(MainActivity.this, new GeoSparkLogoutCallBack() {
                    @Override
                    public void onSuccess(String s) {
                        mFirebaseAuth.signOut();
                        Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(i);
                        finish();
                    }

                    @Override
                    public void onFailure(GeoSparkError geoSparkError) {

                    }
                });
            }
        });
        if (!GeoSpark.checkLocationPermission(this)) {
            GeoSpark.requestLocationPermission(this);
        } else if (!GeoSpark.checkLocationServices(this)) {
            GeoSpark.requestLocationServices(this);
        } else {
            GeoSpark.startTracking(this);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
    }

    public static void ignoreBatteryOptimization(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent();
            String packageName = context.getPackageName();
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            if (pm != null && !pm.isIgnoringBatteryOptimizations(packageName)) {
                intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + packageName));
                context.startActivity(intent);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case GeoSpark.REQUEST_CODE_LOCATION_PERMISSION:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this,
                            android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        GeoSpark.startTracking(this);
                    }
                }
                break;
        }
    }
}
