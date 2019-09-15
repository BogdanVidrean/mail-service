package com.socialnetwork.mailservice.repositories;

import com.socialnetwork.mailservice.MailserviceApplication;
import com.socialnetwork.mailservice.configuration.H2DbTestConfiguration;
import com.socialnetwork.mailservice.data.entities.Email;
import com.socialnetwork.mailservice.data.repositories.EmailRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.springframework.data.domain.PageRequest.of;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {MailserviceApplication.class, H2DbTestConfiguration.class})
public class EmailRepositoryTest {

    private static final int NUMBER_OF_EMAILS = 30;

    @Autowired
    private EmailRepository emailRepository;

    @Before
    public void init() {
        emailRepository.saveAll(createEmails());
    }

    @Test
    public void testFindAll() {
        List<Email> all = emailRepository.findAll();
        assertThat(all, hasSize(NUMBER_OF_EMAILS));
    }

    @Test
    public void testPagination() {
        Page<Email> page = emailRepository.findAll(of(0, 5));
        assertThat(page.nextPageable(), notNullValue());
        assertThat(page.getContent().size(), is(5));
        assertThat(page.getTotalElements(), is(30L));
        assertThat(page.getTotalPages(), is(NUMBER_OF_EMAILS / 5));
    }

    @After
    public void after() {
        emailRepository.deleteAll();
    }

    private List<Email> createEmails() {
        return range(0, NUMBER_OF_EMAILS).boxed()
                .map(i -> {
                    Email email = new Email();
                    email.setSubject("Subject" + i);
                    email.setBody("Body" + i);
                    email.setFrom("from@domain.com");
                    email.setTo(newHashSet("firstTo@domain.com", "secondTo@domain.com", "thirdTo@domain.com"));
                    email.setCc(newHashSet("firstCc@domain.com", "secondCc@domain.com", "thirdCc@domain.com"));
                    email.setBcc(newHashSet("firstBcc@domain.com", "secondBcc@domain.com", "thirdBcc@domain.com"));
                    return email;
                })
                .collect(toList());
    }

}
