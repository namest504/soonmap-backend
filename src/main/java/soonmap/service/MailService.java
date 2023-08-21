package soonmap.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import soonmap.exception.CustomException;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    public void mailSend(String receiver, String code) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(receiver);
        message.setSubject(createTitle());
        message.setText(createText(code));
        try {
            mailSender.send(message);
        } catch (RuntimeException e) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "메일 전송에 실패하였습니다.");
        }
    }

    private String createTitle() {
        String title = "순맵 이메일 인증";
        return title;
    }

    private String createText(String code) {
        String text = "순맵 이메일 인증 번호는 [" + code + "] 입니다.";
        return text;
    }
}
