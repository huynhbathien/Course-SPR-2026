package com.mycompany.service.Impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.mycompany.enums.OtpType;
import com.mycompany.service.EmailService;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void sendOtpEmail(String to, String otpCode, OtpType otpType) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);

            if (OtpType.EMAIL_VERIFICATION.equals(otpType)) {
                helper.setSubject("Verify Your Email - CourseApp");
                helper.setText(buildVerificationEmailHtml(otpCode), true);
            } else {
                helper.setSubject("Reset Your Password - CourseApp");
                helper.setText(buildPasswordResetEmailHtml(otpCode), true);
            }

            mailSender.send(message);
            log.info("OTP email ({}) sent to {}", otpType, to);
        } catch (MessagingException | MailException e) {
            log.error("Failed to send OTP email to {}: {}", to, e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to send OTP email. Please try again.");
        }
    }

    private String buildVerificationEmailHtml(String otpCode) {
        return """
                <!DOCTYPE html>
                <html>
                <head><meta charset="UTF-8"></head>
                <body style="margin:0;padding:0;font-family:Arial,Helvetica,sans-serif;background:#f4f7f6;">
                  <table width="100%%" cellpadding="0" cellspacing="0" style="background:#f4f7f6;padding:40px 0;">
                    <tr><td align="center">
                      <table width="600" cellpadding="0" cellspacing="0" style="background:#ffffff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,0.1);">
                        <tr><td style="background:#4F46E5;padding:32px 40px;text-align:center;">
                          <h1 style="margin:0;color:#ffffff;font-size:24px;font-weight:700;">CourseApp</h1>
                        </td></tr>
                        <tr><td style="padding:40px;">
                          <h2 style="margin:0 0 16px;color:#1a1a2e;font-size:20px;">Verify Your Email Address</h2>
                          <p style="margin:0 0 24px;color:#555;line-height:1.6;">
                            Thank you for registering! Use the code below to verify your email address.
                            The code is valid for <strong>10 minutes</strong>.
                          </p>
                          <div style="background:#f4f7f6;border:2px dashed #4F46E5;border-radius:8px;padding:24px;text-align:center;margin:0 0 24px;">
                            <span style="font-size:36px;font-weight:700;letter-spacing:12px;color:#4F46E5;">%s</span>
                          </div>
                          <p style="margin:0;color:#999;font-size:13px;">
                            If you did not create an account, please ignore this email.
                          </p>
                        </td></tr>
                        <tr><td style="background:#f4f7f6;padding:20px 40px;text-align:center;">
                          <p style="margin:0;color:#aaa;font-size:12px;">&copy; 2026 CourseApp. All rights reserved.</p>
                        </td></tr>
                      </table>
                    </td></tr>
                  </table>
                </body>
                </html>
                """
                .formatted(otpCode);
    }

    private String buildPasswordResetEmailHtml(String otpCode) {
        return """
                <!DOCTYPE html>
                <html>
                <head><meta charset="UTF-8"></head>
                <body style="margin:0;padding:0;font-family:Arial,Helvetica,sans-serif;background:#f4f7f6;">
                  <table width="100%%" cellpadding="0" cellspacing="0" style="background:#f4f7f6;padding:40px 0;">
                    <tr><td align="center">
                      <table width="600" cellpadding="0" cellspacing="0" style="background:#ffffff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,0.1);">
                        <tr><td style="background:#DC2626;padding:32px 40px;text-align:center;">
                          <h1 style="margin:0;color:#ffffff;font-size:24px;font-weight:700;">CourseApp</h1>
                        </td></tr>
                        <tr><td style="padding:40px;">
                          <h2 style="margin:0 0 16px;color:#1a1a2e;font-size:20px;">Reset Your Password</h2>
                          <p style="margin:0 0 24px;color:#555;line-height:1.6;">
                            We received a request to reset your password. Use the code below to proceed.
                            The code is valid for <strong>10 minutes</strong>.
                          </p>
                          <div style="background:#fef2f2;border:2px dashed #DC2626;border-radius:8px;padding:24px;text-align:center;margin:0 0 24px;">
                            <span style="font-size:36px;font-weight:700;letter-spacing:12px;color:#DC2626;">%s</span>
                          </div>
                          <p style="margin:0;color:#999;font-size:13px;">
                            If you did not request a password reset, please ignore this email. Your password will not be changed.
                          </p>
                        </td></tr>
                        <tr><td style="background:#f4f7f6;padding:20px 40px;text-align:center;">
                          <p style="margin:0;color:#aaa;font-size:12px;">&copy; 2026 CourseApp. All rights reserved.</p>
                        </td></tr>
                      </table>
                    </td></tr>
                  </table>
                </body>
                </html>
                """
                .formatted(otpCode);
    }
}
