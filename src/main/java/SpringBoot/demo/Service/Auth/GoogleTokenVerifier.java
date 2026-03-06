package SpringBoot.demo.Service.Auth;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class GoogleTokenVerifier {

    @Value("${google.oauth2.client-id}")
    private String googleClientId;

    /**
     * Xác minh Google ID Token
     * @param tokenString Google ID Token từ client
     * @return GoogleIdToken nếu hợp lệ, null nếu không hợp lệ
     */
    public GoogleIdToken verifyToken(String tokenString) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(),
                    new GsonFactory()
            )
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(tokenString);
            
            if (idToken != null) {
                return idToken;
            } else {
                System.out.println("Invalid ID token.");
                return null;
            }
        } catch (Exception e) {
            System.out.println("Token verification failed: " + e.getMessage());
            return null;
        }
    }

    /**
     * Lấy email từ Google ID Token
     * @param idToken Google ID Token đã xác minh
     * @return Email của user
     */
    public String getEmailFromToken(GoogleIdToken idToken) {
        if (idToken == null) {
            return null;
        }
        GoogleIdToken.Payload payload = idToken.getPayload();
        return payload.getEmail();
    }

    /**
     * Lấy tên từ Google ID Token
     * @param idToken Google ID Token đã xác minh
     * @return Tên của user
     */
    public String getNameFromToken(GoogleIdToken idToken) {
        if (idToken == null) {
            return null;
        }
        GoogleIdToken.Payload payload = idToken.getPayload();
        return (String) payload.get("name");
    }

    /**
     * Lấy ảnh đại diện từ Google ID Token
     * @param idToken Google ID Token đã xác minh
     * @return URL ảnh đại diện
     */
    public String getPictureFromToken(GoogleIdToken idToken) {
        if (idToken == null) {
            return null;
        }
        GoogleIdToken.Payload payload = idToken.getPayload();
        return (String) payload.get("picture");
    }
}
