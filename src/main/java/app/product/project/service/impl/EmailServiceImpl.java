package app.product.project.service.impl;

import app.product.project.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    @Override
    public void sendPasswordResetEmail(String email, String verificationCode) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(senderEmail);
            message.setTo(email);
            message.setSubject("Mã xác nhận đặt lại mật khẩu - Password Reset Code");
            message.setText("Xin chào,\n\n" +
                    "Bạn đã yêu cầu đặt lại mật khẩu. Vui lòng sử dụng mã xác nhận dưới đây để hoàn tất quá trình:\n\n" +
                    "Mã xác nhận: " + verificationCode + "\n\n" +
                    "Mã này sẽ hết hạn trong 30 phút.\n" +
                    "Nếu bạn không yêu cầu đặt lại mật khẩu, hãy bỏ qua email này.\n\n" +
                    "Trân trọng,\nĐội ngũ hỗ trợ");

            mailSender.send(message);
            log.info("Password reset email sent successfully to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send password reset email to {}: {}", email, e.getMessage());
            throw new RuntimeException("Gửi email thất bại: " + e.getMessage());
        }
    }

    @Override
    public void sendPasswordResetSuccessEmail(String email) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(senderEmail);
            message.setTo(email);
            message.setSubject("Mật khẩu đã được thay đổi thành công - Password Changed Successfully");
            message.setText("Xin chào,\n\n" +
                    "Mật khẩu của bạn đã được thay đổi thành công.\n" +
                    "Nếu bạn không thực hiện thay đổi này, vui lòng liên hệ dteam support ngay.\n\n" +
                    "Trân trọng,\nĐội ngũ hỗ trợ");

            mailSender.send(message);
            log.info("Password reset success email sent to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send password reset success email to {}: {}", email, e.getMessage());
            throw new RuntimeException("Gửi email thất bại: " + e.getMessage());
        }
    }

    @Override
    public void sendEmail(String candidateEmail, String s, String emailBody) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(senderEmail);
            message.setTo(candidateEmail);
            message.setSubject(s);
            message.setText(emailBody);

            mailSender.send(message);
            log.info("Email sent successfully to: {}", candidateEmail);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", candidateEmail, e.getMessage());
            throw new RuntimeException("Gửi email thất bại: " + e.getMessage());
        }
    }
}

