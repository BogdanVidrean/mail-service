package com.socialnetwork.mailservice.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.socialnetwork.mailservice.services.api.EmailService;
import com.socialnetwork.mailservice.utils.exceptions.MailServiceHttpException;
import com.socialnetwork.mailservice.utils.vos.EmailInputVo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@RunWith(SpringRunner.class)
public class EmailControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @MockBean
    private EmailService emailService;

    @Before
    public void before() {
        mockMvc = standaloneSetup(new EmailController(emailService))
                .build();
    }

    @Test
    public void testSendEmailWithToAsDestination() throws Exception {
        EmailInputVo emailInputVoOnlyWithTo = createEmailInputVoWithTo();
        doReturn(newHashMap()).when(emailService).sendEmail(any(EmailInputVo.class));

        mockMvc.perform(post("/api/emails")
                .content(objectMapper.writeValueAsString(emailInputVoOnlyWithTo))
                .contentType("application/json"))
                .andExpect(status().isAccepted());

    }

    @Test
    public void testSendEmailWithToAndCcAsDestination() throws Exception {
        EmailInputVo emailInputVoWithToAndCc = createEmailInputVoWithToAndCc();
        doReturn(newHashMap()).when(emailService).sendEmail(any(EmailInputVo.class));

        mockMvc.perform(post("/api/emails")
                .content(objectMapper.writeValueAsString(emailInputVoWithToAndCc))
                .contentType("application/json"))
                .andExpect(status().isAccepted());
    }

    @Test
    public void testSendEmailWithToAndCcAndBccAsDestination() throws Exception {
        EmailInputVo emailInputVoWithToAndCcAndBcc = createEmailInputVoWithToAndCcAndBcc();
        doReturn(newHashMap()).when(emailService).sendEmail(any(EmailInputVo.class));

        mockMvc.perform(post("/api/emails")
                .content(objectMapper.writeValueAsString(emailInputVoWithToAndCcAndBcc))
                .contentType("application/json"))
                .andExpect(status().isAccepted());
    }

    @Test
    public void testSendMailWithoutSubject() throws Exception {
        EmailInputVo email = createEmailInputVoWithTo();
        email.setSubject(null);

        mockMvc.perform(post("/api/emails")
                .content(objectMapper.writeValueAsString(email))
                .contentType("application/json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testSendMailWithoutBody() throws Exception {
        EmailInputVo email = createEmailInputVoWithTo();
        email.setBody(null);

        mockMvc.perform(post("/api/emails")
                .content(objectMapper.writeValueAsString(email))
                .contentType("application/json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testSendMailWithoutFrom() throws Exception {
        EmailInputVo email = createEmailInputVoWithTo();
        email.setFrom(null);

        mockMvc.perform(post("/api/emails")
                .content(objectMapper.writeValueAsString(email))
                .contentType("application/json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testMailServerHttpExceptionInvocation() throws Exception {
        EmailInputVo email = createEmailInputVoWithTo();
        doThrow(new MailServiceHttpException("message", BAD_REQUEST)).when(emailService).sendEmail(any(EmailInputVo.class));
        mockMvc.perform(post("/api/emails")
                .content(objectMapper.writeValueAsString(email))
                .contentType("application/json"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("message"));
    }

    private EmailInputVo createEmailInputVoWithTo() {
        EmailInputVo emailInputVo = new EmailInputVo();
        emailInputVo.setSubject("Subject");
        emailInputVo.setBody("body");
        emailInputVo.setFrom("from@domain.com");
        emailInputVo.setTo(newHashSet("to1@domain.com", "to2@domain.com"));
        return emailInputVo;
    }

    private EmailInputVo createEmailInputVoWithToAndCc() {
        EmailInputVo emailInputVo = new EmailInputVo();
        emailInputVo.setSubject("Subject");
        emailInputVo.setBody("body");
        emailInputVo.setFrom("from@domain.com");
        emailInputVo.setTo(newHashSet("to1@domain.com", "to2@domain.com"));
        emailInputVo.setCc(newHashSet("cc1@domain.com", "cc2@domain.com"));
        return emailInputVo;
    }

    private EmailInputVo createEmailInputVoWithToAndCcAndBcc() {
        EmailInputVo emailInputVo = new EmailInputVo();
        emailInputVo.setSubject("Subject");
        emailInputVo.setBody("body");
        emailInputVo.setFrom("from@domain.com");
        emailInputVo.setTo(newHashSet("to1@domain.com", "to2@domain.com"));
        emailInputVo.setCc(newHashSet("cc1@domain.com", "cc2@domain.com"));
        emailInputVo.setBcc(newHashSet("bcc1@domain.com", "bcc2@domain.com"));
        return emailInputVo;
    }
}
