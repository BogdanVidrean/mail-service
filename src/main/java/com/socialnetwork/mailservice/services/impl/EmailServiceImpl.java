package com.socialnetwork.mailservice.services.impl;

import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.helpers.mail.Mail;
import com.socialnetwork.mailservice.services.api.EmailService;
import com.socialnetwork.mailservice.utils.sendgrid.SendGridEmailBuilder;
import com.socialnetwork.mailservice.utils.sendgrid.SendGridRequestHandler;
import com.socialnetwork.mailservice.utils.vos.EmailInputVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;

import static com.sendgrid.Method.POST;

@Service
public class EmailServiceImpl implements EmailService {

    private static final String SEND_ENDPOINT = "mail/send";

    private final SendGridRequestHandler sendGridRequestHandler;

    @Autowired
    public EmailServiceImpl(SendGridRequestHandler sendGridRequestHandler) {
        this.sendGridRequestHandler = sendGridRequestHandler;
    }

    @Override
    public void sendEmail(final EmailInputVo emailInputVo) {
        Mail newEmail = createNewEmail(emailInputVo);
        try {
            Request newSendRequest = createNewSendRequest(newEmail);
            Optional<Response> response = sendGridRequestHandler.executeRequest(newSendRequest);
            if (response.isPresent()) {

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Mail createNewEmail(final EmailInputVo emailInputVo) {
        final SendGridEmailBuilder sendGridEmailBuilder = new SendGridEmailBuilder();
        return sendGridEmailBuilder
                .from(emailInputVo.getFrom())
                .to(emailInputVo.getTo())
                .cc(emailInputVo.getCc())
                .bcc(emailInputVo.getBcc())
                .subject(emailInputVo.getSubject())
                .body(emailInputVo.getBody())
                .buildEmail();
    }

    private Request createNewSendRequest(final Mail mail) throws IOException {
        Request request = new Request();
        request.setMethod(POST);
        request.setEndpoint(SEND_ENDPOINT);
        request.setBody(mail.build());
        return request;
    }

}
