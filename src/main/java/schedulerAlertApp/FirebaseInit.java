package schedulerAlertApp;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.auth.oauth2.GoogleCredentials;

import java.io.FileInputStream;
import java.io.IOException;

public class FirebaseInit {
    public static void main(String[] args) {
        try {
            // Firebase 서비스 계정 키 경로
            FileInputStream serviceAccount = new FileInputStream("src/main/resources/serviceAccountKey.json");

            // Firebase 옵션 설정
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl("https://javaproject27-2c148-default-rtdb.firebaseio.com")
                    .build();

            // Firebase 초기화
            FirebaseApp.initializeApp(options);
            System.out.println("Firebase Initialized!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}