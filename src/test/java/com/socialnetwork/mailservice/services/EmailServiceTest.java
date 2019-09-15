package com.socialnetwork.mailservice.services;

import com.sendgrid.Request;
import com.sendgrid.Response;
import com.socialnetwork.mailservice.data.entities.Email;
import com.socialnetwork.mailservice.data.repositories.EmailRepository;
import com.socialnetwork.mailservice.services.api.EmailService;
import com.socialnetwork.mailservice.services.impl.EmailServiceImpl;
import com.socialnetwork.mailservice.utils.exceptions.MailServiceHttpException;
import com.socialnetwork.mailservice.utils.sendgrid.SendGridRequestHandler;
import com.socialnetwork.mailservice.utils.vos.EmailInputVo;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.IntStream.range;
import static org.junit.rules.ExpectedException.none;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.springframework.data.domain.PageRequest.of;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {EmailServiceTest.EmailServiceConfiguration.class})
@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
@TestPropertySource(locations = "classpath:application-test.properties")
public class EmailServiceTest {
    @Rule
    public ExpectedException thrown = none();
    @Autowired
    private EmailRepository emailRepository;
    @Autowired
    private SendGridRequestHandler sendGridRequestHandler;
    @Autowired
    private EmailService emailService;

    @Test
    public void testSendEmailAndThrowIOExceptionFromSendGrid() throws IOException {
        EmailInputVo emailInputVo = createEmailInputVo();
        doThrow(new IOException("message")).when(sendGridRequestHandler).executeRequest(any(Request.class));

        emailService.sendEmail(emailInputVo);

        verify(sendGridRequestHandler).init();
        verify(sendGridRequestHandler).executeRequest(any(Request.class));
        verifyNoMoreInteractions(sendGridRequestHandler);
        verifyNoMoreInteractions(emailRepository);
    }

    @Test
    public void testSendEmailAndNoConnectionAvaiableToSendGrid() throws IOException {
        EmailInputVo emailInputVo = createEmailInputVo();
        doThrow(new UnknownHostException("message")).when(sendGridRequestHandler).executeRequest(any(Request.class));

        emailService.sendEmail(emailInputVo);

        verify(sendGridRequestHandler).init();
        verify(sendGridRequestHandler).executeRequest(any(Request.class));
        verifyNoMoreInteractions(sendGridRequestHandler);
        verify(emailRepository).save(any(Email.class));
        verifyNoMoreInteractions(emailRepository);
    }

    @Test
    public void testSendEmailAndReceive200Status() throws IOException {
        EmailInputVo emailInputVo = createEmailInputVo();
        Response response = createResponse(200, "body");
        doReturn(of(response)).when(sendGridRequestHandler).executeRequest(any(Request.class));

        emailService.sendEmail(emailInputVo);

        verify(sendGridRequestHandler).init();
        verify(sendGridRequestHandler).executeRequest(any(Request.class));
        verifyNoMoreInteractions(sendGridRequestHandler);
        verifyZeroInteractions(emailRepository);
    }

    @Test
    public void testSendEmailAndReceive202Status() throws IOException {
        EmailInputVo emailInputVo = createEmailInputVo();
        Response response = createResponse(202, "body");
        doReturn(of(response)).when(sendGridRequestHandler).executeRequest(any(Request.class));

        emailService.sendEmail(emailInputVo);

        verify(sendGridRequestHandler).init();
        verify(sendGridRequestHandler).executeRequest(any(Request.class));
        verifyNoMoreInteractions(sendGridRequestHandler);
        verifyZeroInteractions(emailRepository);
    }

    @Test
    public void testSendEmailAndReceive4xxError() throws IOException {
        EmailInputVo emailInputVo = createEmailInputVo();
        Response response = createResponse(400, "400 Error");
        doReturn(of(response)).when(sendGridRequestHandler).executeRequest(any(Request.class));
        thrown.expect(MailServiceHttpException.class);
        thrown.expectMessage("400 Error");

        emailService.sendEmail(emailInputVo);

        verify(sendGridRequestHandler).init();
        verify(sendGridRequestHandler).executeRequest(any(Request.class));
        verifyNoMoreInteractions(sendGridRequestHandler);
        verifyZeroInteractions(emailRepository);
    }

    @Test
    public void testSendEmailAndReceive500Status() throws IOException {
        EmailInputVo emailInputVo = createEmailInputVo();
        Response response = createResponse(500, "500 Error");
        doReturn(of(response)).when(sendGridRequestHandler).executeRequest(any(Request.class));

        emailService.sendEmail(emailInputVo);

        verify(sendGridRequestHandler).init();
        verify(sendGridRequestHandler).executeRequest(any(Request.class));
        verifyNoMoreInteractions(sendGridRequestHandler);
        verify(emailRepository).save(any(Email.class));
        verifyNoMoreInteractions(emailRepository);
    }

    @Test
    public void testSendEmailAndReceive503Status() throws IOException {
        EmailInputVo emailInputVo = createEmailInputVo();
        Response response = createResponse(503, "503 Error");
        doReturn(of(response)).when(sendGridRequestHandler).executeRequest(any(Request.class));

        emailService.sendEmail(emailInputVo);

        verify(sendGridRequestHandler).init();
        verify(sendGridRequestHandler).executeRequest(any(Request.class));
        verifyNoMoreInteractions(sendGridRequestHandler);
        verify(emailRepository).save(any(Email.class));
        verifyNoMoreInteractions(emailRepository);
    }

    @Test
    public void testRetrySendEmailsWithNoEmailsAvailable() {
        doReturn(new PageImpl<>(emptyList())).when(emailRepository).findAll(any(Pageable.class));

        emailService.retrySendFailedEmails();

        verify(emailRepository).findAll(any(Pageable.class));
        verifyNoMoreInteractions(emailRepository);
    }

    @Test
    public void testRetrySendEmailsWhenAllSucceed() throws IOException {
        List<Email> listOfEmails = createListOfEmails(30);
        doReturn(new PageImpl<>(listOfEmails)).when(emailRepository).findAll(of(0, 1000));
        doReturn(new PageImpl<>(emptyList())).when(emailRepository).findAll(of(1, 1000));
        doReturn(of(createResponse(202, "Accepted"))).when(sendGridRequestHandler).executeRequest(any(Request.class));

        emailService.retrySendFailedEmails();

        verify(emailRepository).findAll(of(0, 1000));
        verify(emailRepository).deleteAll(listOfEmails);
        verify(emailRepository).findAll(of(1, 1000));
        verifyNoMoreInteractions(emailRepository);
    }

    @Test
    public void testRetrySendEmailWhenAllSucceedAndMultiplePages() throws IOException {
        List<Email> listOfEmails = createListOfEmails(20);
        doReturn(new PageImpl<>(listOfEmails)).when(emailRepository).findAll(of(0, 1000));
        doReturn(new PageImpl<>(listOfEmails)).when(emailRepository).findAll(of(1, 1000));
        doReturn(new PageImpl<>(emptyList())).when(emailRepository).findAll(of(2, 1000));
        doReturn(of(createResponse(202, "Accepted"))).when(sendGridRequestHandler).executeRequest(any(Request.class));

        emailService.retrySendFailedEmails();

        verify(emailRepository).findAll(of(0, 1000));
        verify(emailRepository, times(2)).deleteAll(listOfEmails);
        verify(emailRepository).findAll(of(1, 1000));
        verify(emailRepository).findAll(of(2, 1000));
        verifyNoMoreInteractions(emailRepository);
    }

    private Response createResponse(int statusCode, String body) {
        Response response = new Response();
        response.setStatusCode(statusCode);
        response.setBody(body);
        return response;
    }

    private EmailInputVo createEmailInputVo() {
        EmailInputVo emailInputVo = new EmailInputVo();
        emailInputVo.setFrom("from@domain.com");
        emailInputVo.setSubject("subject");
        emailInputVo.setBody("body");
        emailInputVo.setTo(range(0, 3).boxed().map(i -> "to" + i + "@domain.com").collect(toSet()));
        emailInputVo.setCc(range(0, 3).boxed().map(i -> "cc" + i + "@domain.com").collect(toSet()));
        emailInputVo.setBcc(range(0, 3).boxed().map(i -> "bcc" + i + "@domain.com").collect(toSet()));
        return emailInputVo;
    }

    private List<Email> createListOfEmails(int numberOfItems) {
        return range(0, numberOfItems).boxed()
                .map(i -> {
                    Email email = new Email();
                    email.setFrom("from@domain.com");
                    email.setSubject("Subject");
                    email.setBody("Body");
                    email.setTo(range(0, 4).boxed().map(j -> "to@domain.com").collect(toSet()));
                    email.setCc(range(0, 4).boxed().map(j -> "cc@domain.com").collect(toSet()));
                    email.setBcc(range(0, 4).boxed().map(j -> "bcc@domain.com").collect(toSet()));
                    return email;
                })
                .collect(toList());
    }

    @Configuration
    public static class EmailServiceConfiguration {

        @Bean
        public EmailRepository emailRepository() {
            return mock(EmailRepository.class);
        }

        @Bean
        public SendGridRequestHandler sendGridRequestHandler() {
            return mock(SendGridRequestHandler.class);
        }

        @Bean
        public EmailService emailService() {
            return new EmailServiceImpl(sendGridRequestHandler(), emailRepository());
        }

    }
}
