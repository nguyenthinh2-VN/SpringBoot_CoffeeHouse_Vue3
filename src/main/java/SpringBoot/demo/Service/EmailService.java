package SpringBoot.demo.Service;

import com.google.gson.Gson;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class EmailService {

    @Value("${resend.api.key}")
    private String resendApiKey;

    @Value("${resend.from.email:onboarding@resend.dev}")
    private String fromEmail;

    private static final String RESEND_API_URL = "https://api.resend.com/emails";
    private final OkHttpClient httpClient = new OkHttpClient();
    private final Gson gson = new Gson();

    public void sendOtpEmail(String toEmail, String otp) {
        try {
            // Tạo request body
            Map<String, Object> emailData = new HashMap<>();
            emailData.put("from", fromEmail);
            emailData.put("to", new String[]{toEmail});
            emailData.put("subject", "Mã OTP đặt lại mật khẩu - Coffee House");
            emailData.put("html", buildEmailHtmlContent(otp));

            String jsonBody = gson.toJson(emailData);

            // Tạo HTTP request
            RequestBody body = RequestBody.create(
                    jsonBody,
                    MediaType.parse("application/json")
            );

            Request request = new Request.Builder()
                    .url(RESEND_API_URL)
                    .addHeader("Authorization", "Bearer " + resendApiKey)
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build();

            // Gửi request
            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                    throw new RuntimeException("Resend API error: " + response.code() + " - " + errorBody);
                }
            }

        } catch (IOException e) {
            throw new RuntimeException("Không thể gửi email qua Resend: " + e.getMessage());
        }
    }

    private String buildEmailHtmlContent(String otp) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                ".container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
                ".header { background-color: #6F4E37; color: white; padding: 20px; text-align: center; border-radius: 5px 5px 0 0; }" +
                ".content { background-color: #f9f9f9; padding: 30px; border-radius: 0 0 5px 5px; }" +
                ".otp-box { background-color: #fff; border: 2px dashed #6F4E37; padding: 20px; text-align: center; margin: 20px 0; border-radius: 5px; }" +
                ".otp-code { font-size: 32px; font-weight: bold; color: #6F4E37; letter-spacing: 5px; }" +
                ".footer { text-align: center; margin-top: 20px; color: #666; font-size: 12px; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<div class='header'>" +
                "<h1>☕ Coffee House</h1>" +
                "</div>" +
                "<div class='content'>" +
                "<h2>Đặt lại mật khẩu</h2>" +
                "<p>Xin chào,</p>" +
                "<p>Bạn đã yêu cầu đặt lại mật khẩu cho tài khoản Coffee House của mình.</p>" +
                "<div class='otp-box'>" +
                "<p style='margin: 0; font-size: 14px; color: #666;'>Mã OTP của bạn là:</p>" +
                "<div class='otp-code'>" + otp + "</div>" +
                "</div>" +
                "<p><strong>⏰ Mã này sẽ hết hạn sau 10 phút.</strong></p>" +
                "<p>Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.</p>" +
                "<p>Trân trọng,<br><strong>Coffee House Team</strong></p>" +
                "</div>" +
                "<div class='footer'>" +
                "<p>Email này được gửi tự động, vui lòng không trả lời.</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }
}
