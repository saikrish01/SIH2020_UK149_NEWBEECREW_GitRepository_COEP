package lbn.geospark.com.geosparknotify;

import com.geospark.lib.GeoSpark;
import com.google.firebase.messaging.FirebaseMessagingService;

public class MyFireBaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        GeoSpark.setDeviceToken(this, token);
        SharedPreference.setToken(this, token);
    }
}
