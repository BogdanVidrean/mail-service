package com.socialnetwork.mailservice.utils.schedulers;

import com.socialnetwork.mailservice.services.api.EmailService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class FailedEmailsRetryTask {

    private final EmailService emailService;

    public FailedEmailsRetryTask(EmailService emailService) {
        this.emailService = emailService;
    }

    //  we want fixedDelay and not fixedRate because we don't want the scheduled tasks to overlap
    @Scheduled(fixedDelay = 900000, initialDelay = 5000)
    public void retrySendFailedEmails() {
        emailService.retrySendFailedEmails();
    }

}
