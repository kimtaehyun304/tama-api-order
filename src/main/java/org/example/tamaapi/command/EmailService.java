package org.example.tamaapi.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender javaMailSender;

    public void sendSignedUpEmail(String toMailAddr) {
        String subject = "[TAMA] 회원가입 완료 안내";
        String body = String.format("<p>TAMA 쇼핑몰에 오신 것을 환영합니다</p>");
        sendEmail(toMailAddr, subject, body);
    }


    public void sendAuthenticationEmail(String toMailAddr, String authString) {
        String subject = "[TAMA] 회원가입 인증문자 안내";
        String body = String.format("인증문자 : %s <p>본 메일이 생성된 이유는 해당 메일로 인증하려는 시도가 있었기 때문입니다.</p>", authString);
        sendEmail(toMailAddr, subject, body);
    }

    /*
    public void sendGuestOrderEmail(String toMailAddr, String buyerName, Long orderId) {
        String subject = "[TAMA] 비회원 주문 결제 안내";
        String body = String.format("주문자 이름: %s, 주문 번호: %s <p>TAMA 사이트에서 주문 상세정보를 볼 수 있습니다.</p>", buyerName, orderId);
        sendEmail(toMailAddr, subject, body);
    }
     */

    @Async("emailExecutor")
    @Retryable(backoff = @Backoff(delay = 500, multiplier = 2), recover = "recover")
    public void sendGuestOrderEmailAsync(String toMailAddr, String buyerName, Long orderId) {
        String subject = "[TAMA] 비회원 주문 결제 안내";
        String body = String.format("주문자 이름: %s, 주문 번호: %s <p>TAMA 사이트에서 주문 상세정보를 볼 수 있습니다.</p>", buyerName, orderId);
        sendEmail(toMailAddr, subject, body);
    }

    private void sendEmail(String toMailAddr, String subject, String body) {
        MimeMessagePreparator mimeMessagePreparator = createMimeMessagePreparator(toMailAddr, subject, body);
        javaMailSender.send(mimeMessagePreparator);
    }

    private MimeMessagePreparator createMimeMessagePreparator(String toMailAddr, String subject, String body) {
        return mimeMessage -> {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setTo(toMailAddr);
            helper.setSubject(subject);
            helper.setText(body, true);
        };
    }

    @Recover
    //파라미터 안 필요해도 비동기 메서드랑 파라미터 같게 맞춰줘야, recover 메서드를 찾을 수 있음
    public void recover(Exception e, String toMailAddr, String buyerName, Long orderId) {
        log.error("재시도한 모든 비동기 작업(메일 발송)을 실패했습니다. " +
                        "toMailAddr={}, buyerName={}, orderId={}, 원인={}",
                toMailAddr, buyerName, orderId, e.getMessage());
    }

}
