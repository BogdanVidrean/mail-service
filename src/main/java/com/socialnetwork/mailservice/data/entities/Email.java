package com.socialnetwork.mailservice.data.entities;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static java.util.Objects.hash;
import static javax.persistence.GenerationType.SEQUENCE;

@Entity
@Table(name = "EMAILS")
@SequenceGenerator(name = "idgen", sequenceName = "email_seq")
public class Email {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = SEQUENCE, generator = "idgen")
    protected Long id;

    // change the mapping to the column due to the fact that FROM is a reserved keyword
    @Column(name = "SENDER", nullable = false)
    private String from;

    @Column(name = "SUBJECT", nullable = false)
    private String subject;

    @Column(name = "BODY", nullable = false)
    private String body;

    @ElementCollection
    @CollectionTable(name = "EMAILS_TO", joinColumns = @JoinColumn(name = "EMAIL_ID"))
    @Column(name = "TO")
    private Set<String> to = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "EMAILS_CC", joinColumns = @JoinColumn(name = "EMAIL_ID"))
    @Column(name = "CC")
    private Set<String> cc = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "EMAILS_BCC", joinColumns = @JoinColumn(name = "EMAIL_ID"))
    @Column(name = "BCC")
    private Set<String> bcc = new HashSet<>();

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Email email = (Email) o;
        return Objects.equals(from, email.from) &&
                Objects.equals(subject, email.subject) &&
                Objects.equals(body, email.body) &&
                Objects.equals(to, email.to) &&
                Objects.equals(cc, email.cc) &&
                Objects.equals(bcc, email.bcc);
    }

    @Override
    public int hashCode() {
        return hash(from, subject, body, to, cc, bcc);
    }
}
