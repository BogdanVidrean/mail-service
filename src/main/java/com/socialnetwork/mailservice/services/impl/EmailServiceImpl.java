package com.socialnetwork.mailservice.services.impl;

import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.helpers.mail.Mail;
import com.socialnetwork.mailservice.data.entities.Email;
import com.socialnetwork.mailservice.data.repositories.EmailRepository;
import com.socialnetwork.mailservice.services.api.EmailService;
import com.socialnetwork.mailservice.utils.exceptions.MailServiceHttpException;
import com.socialnetwork.mailservice.utils.sendgrid.SendGridEmailBuilder;
import com.socialnetwork.mailservice.utils.sendgrid.SendGridRequestHandler;
import com.socialnetwork.mailservice.utils.vos.EmailInputVo;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static com.sendgrid.Method.POST;
import static org.apache.commons.lang3.StringUtils.contains;
import static org.springframework.data.domain.PageRequest.of;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.valueOf;

@Service
public class EmailServiceImpl implements EmailService {

    private static final String SEND_ENDPOINT = "mail/send";
    private static final Set<Integer> SENDGRID_SERVER_ERROR_RESPONSE_CODES = newHashSet(500, 503);
    // status code 200 is just for testing (sandbox mode for SendGrid)
    private static final Set<Integer> SENDGRID_OK_RESPONSE_CODES = newHashSet(200, 202);
    private static final int RETRY_SEND_EMAILS_BATCH_SIZE = 1000;

    private final ModelMapper modelMapper = new ModelMapper();
    private final SendGridRequestHandler sendGridRequestHandler;
    private final EmailRepository emailRepository;

    @Autowired
    public EmailServiceImpl(SendGridRequestHandler sendGridRequestHandler,
                            EmailRepository emailRepository) {
        this.sendGridRequestHandler = sendGridRequestHandler;
        this.emailRepository = emailRepository;
    }

    @Override
    @Transactional
    public void sendEmail(final EmailInputVo emailInputVo) {
        Mail newEmail = createNewEmail(emailInputVo);
        Request newSendRequest = createNewSendRequest(newEmail);
        try {
            Optional<Response> response = sendGridRequestHandler.executeRequest(newSendRequest);
            response.ifPresent(sendGridResponse -> validateResponse(sendGridResponse, emailInputVo));
        } catch (UnknownHostException e) {
            // network error while connecting to SendGrid service, so we save the message and try to send again later
            persistEmail(emailInputVo);
        } catch (IOException e) {
            String message = e.getMessage();
            if (contains(message, "400Body")) {
                //  that's some bad handling from SendGrid because they throw an IOException for invalid arguments
                //  e.g. email address
                throw new MailServiceHttpException(e.getMessage(), BAD_REQUEST);
            }
        }
    }

    @Override
    @Transactional
    public void retrySendFailedEmails() {
        int currentPage = 0;
        Page<Email> emailsPage = emailRepository.findAll(of(currentPage, RETRY_SEND_EMAILS_BATCH_SIZE));
        // safe from NPE because ti returns an empty list if there are no items
        while (emailsPage.getContent().size() > 0) {
            List<Email> emails = emailsPage.getContent();
            List<Email> successfullySentEmails = new ArrayList<>();
            emails.forEach(email -> {
                Mail newEmail = createNewEmail(modelMapper.map(email, EmailInputVo.class));
                Request newSendRequest = createNewSendRequest(newEmail);
                try {
                    Optional<Response> response = sendGridRequestHandler.executeRequest(newSendRequest);
                    response.ifPresent(sendGridResponse -> {
                        if (SENDGRID_OK_RESPONSE_CODES.contains(sendGridResponse.getStatusCode())) {
                            successfullySentEmails.add(email);
                        }
                    });
                } catch (IOException e) {
                    if (contains(e.getMessage(), "400Body")) {
                        // this point will be reached for an invalid email, so we add it in the collection that will
                        // be removed from the database
                        successfullySentEmails.add(email);
                    }
                }
            });
            emailRepository.deleteAll(successfullySentEmails);
            currentPage++;
            emailsPage = emailRepository.findAll(of(currentPage, RETRY_SEND_EMAILS_BATCH_SIZE));
        }
    }

    private void validateResponse(final Response response, final EmailInputVo emailInputVo) {
        int statusCode = response.getStatusCode();
        if (!SENDGRID_OK_RESPONSE_CODES.contains(statusCode)) {
            if (SENDGRID_SERVER_ERROR_RESPONSE_CODES.contains(statusCode)) {
                // SendGrid service is not available so we save the request in order to try again later
                persistEmail(emailInputVo);
            } else {
                // because this is an unchecked exception, it will trigger the rollback for the transaction
                throw new MailServiceHttpException(response.getBody(), valueOf(response.getStatusCode()));
            }
        }
    }

    private void persistEmail(EmailInputVo emailInputVo) {
        emailRepository.save(modelMapper.map(emailInputVo, Email.class));
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

    private Request createNewSendRequest(final Mail mail) {
        Request request = new Request();
        request.setMethod(POST);
        request.setEndpoint(SEND_ENDPOINT);
        try {
            request.setBody(mail.build());
            return request;
        } catch (IOException e) {
            //  Jackson failed the JSON marshal process for the mail due to invalid format
            //  It will be handled in ExceptionHandlingController
            throw new MailServiceHttpException(e.getMessage(), BAD_REQUEST);
        }
    }

}
