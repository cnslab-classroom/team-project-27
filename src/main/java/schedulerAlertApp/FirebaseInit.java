package schedulerAlertApp;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.auth.oauth2.GoogleCredentials;

import java.io.InputStream;
import java.io.IOException;

public class FirebaseInit {
    public static void main(String[] args) {
        try {
            // Firebase 서비스 계정 키 경로
            InputStream serviceAccount = FirebaseInit.class.getClassLoader().getResourceAsStream("serviceAccountKey.json");

            if (serviceAccount == null) {
                throw new IOException("Service Account Key file not found.");
            }

            // Firebase 옵션 설정
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl("https://javaproject27-2c148-default-rtdb.firebaseio.com")
                    .build();

            // Firebase 초기화
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                System.out.println("Firebase Initialized!");
            } else {
                System.out.println("Firebase already initialized.");
            }
        } catch (IOException e) {
            System.err.println("Firebase Initialization Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}