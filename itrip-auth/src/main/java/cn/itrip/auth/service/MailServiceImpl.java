package cn.itrip.auth.service;

import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * @author wangshijun
 */
@Service
@Transactional
public class MailServiceImpl implements MailService {
    @Resource
    private SimpleMailMessage message;
    @Resource
    MailSender mailSender;

    @Override
    public void sendMail(String mailAddr, String code) {

        message.setTo(mailAddr);
        message.setText(code);
        mailSender.send(message);

    }
}
