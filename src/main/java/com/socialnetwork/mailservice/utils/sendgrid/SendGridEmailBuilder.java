package com.socialnetwork.mailservice.utils.sendgrid;

import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;

import java.util.HashSet;
import java.util.Set;

public class SendGridEmailBuilder {
    private static final String CONTENT_TYPE = "text/plain";
    private String from;
    private Set<String> to = new HashSet<>();
    private Set<String> cc = new HashSet<>();
    private Set<String> bcc = new HashSet<>();
    private String subject;
    private String body;

    public SendGridEmailBuilder from(final String from) {
        this.from = from;
        return this;
    }

    public SendGridEmailBuilder to(final Set<String> to) {
        this.to = to;
        return this;
    }

    public SendGridEmailBuilder cc(final Set<String> cc) {
        this.cc = cc;
        return this;
    }

    public SendGridEmailBuilder bcc(final Set<String> bcc) {
        this.bcc = bcc;
        return this;
    }

    public SendGridEmailBuilder subject(final String subject) {
        this.subject = subject;
        return this;
    }

    public SendGridEmailBuilder body(final String body) {
        this.body = body;
        return this;
    }

    public Mail buildEmail() {
        Mail mail = new Mail();
        addSender(mail);
        addReceivers(mail);
        addSubject(mail);
        addContent(mail);
        return mail;
    }

    private void addSubject(final Mail mail) {
        if (subject != null) {
            mail.setSubject(subject);
        }
    }

    private void addContent(final Mail mail) {
        if (body != null) {
            Content content = new Content(CONTENT_TYPE, body);
            mail.addContent(content);
        }
    }

    private void addSender(final Mail mail) {
        Email fromEmail = new Email();
        fromEmail.setEmail(from);
        mail.setFrom(fromEmail);
    }

    private void addReceivers(final Mail mail) {
        Personalization personalization = new Personalization();
        to.forEach(to -> personalization.addTo(new Email(to)));
        cc.forEach(cc -> personalization.addCc(new Email(cc)));
        bcc.forEach(bcc -> personalization.addBcc(new Email(bcc)));
        mail.addPersonalization(personalization);
    }

}
