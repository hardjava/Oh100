package oh100.firebase;

import java.io.FileInputStream;
import java.io.IOException;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import oh100.util.StringLoader;

public class Firebase {
    public static boolean isInitialized = false;

    public static void init()
    {
        StringLoader string_loader = new StringLoader();
        String credential_path = string_loader.getProperty("credential_path");

        try {
            FileInputStream serviceAccount =
            new FileInputStream(credential_path);

            FirebaseOptions options = FirebaseOptions.builder().
            setCredentials(GoogleCredentials.fromStream(serviceAccount))
            .build();

            FirebaseApp.initializeApp(options);
            
            isInitialized = true;
            CloudFirestore.init();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}
