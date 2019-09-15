package com.socialnetwork.mailservice.services.api;

import com.socialnetwork.mailservice.utils.vos.EmailInputVo;

public interface EmailService {

    void sendEmail(final EmailInputVo emailInputVo);

    void retrySendFailedEmails();
}
