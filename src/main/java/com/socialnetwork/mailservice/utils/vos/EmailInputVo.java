package com.socialnetwork.mailservice.utils.vos;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.HashSet;
import java.util.Set;

public class EmailInputVo {

    @NotBlank(message = "The email address of the sender is mandatory.")
    private String from;
    @NotEmpty(message = "There must be at least one recipient for to: field.")
    private Set<String> to = new HashSet<>();
    private Set<String> cc = new HashSet<>();
    private Set<String> bcc = new HashSet<>();
    // sendgrid limitation
    @NotBlank(message = "The subject is required.")
    private String subject;
    // sendgrid limitation
    @NotBlank(message = "The body is required.")
    private String body;

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public Set<String> getTo() {
        return to;
    }

    public void setTo(Set<String> to) {
        this.to = to;
    }

    public Set<String> getCc() {
        return cc;
    }

    public void setCc(Set<String> cc) {
        this.cc = cc;
    }

    public Set<String> getBcc() {
        return bcc;
    }

    public void setBcc(Set<String> bcc) {
        this.bcc = bcc;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
