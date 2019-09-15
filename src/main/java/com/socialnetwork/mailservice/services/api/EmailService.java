package com.socialnetwork.mailservice.services.api;

import com.socialnetwork.mailservice.utils.vos.EmailInputVo;

import java.util.Map;

public interface EmailService {

    Map<String, String> sendEmail(final EmailInputVo emailInputVo);

    void retrySendFailedEmails();
}
