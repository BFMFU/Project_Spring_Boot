package app.product.project.service;

public interface EmailService {
    void sendPasswordResetEmail(String email, String verificationCode);
    void sendPasswordResetSuccessEmail(String email);

    void sendEmail(String candidateEmail, String s, String emailBody);
}

