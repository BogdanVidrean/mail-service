# Email Service
[![Build Status](https://travis-ci.org/BogdanVidrean/mail-service.svg?branch=master)](https://travis-ci.org/BogdanVidrean/mail-service)


# Description
Email service that exposes a HTTP interface for sending emails, using SendGrid under the hood.

# How To Clone/Build
Requirements:
1. JDK 8
2. Maven (recommended version is 3.6.1)

Steps:
1. Clone the repository using ssh `git@github.com:BogdanVidrean/mail-service.git` or https 
`https://github.com/BogdanVidrean/mail-service.git`.
2. Open the project in your favorite IDE (IntelliJ Idea is recommended).
3. Run `mvn clean install` in a terminal/cmd that is opened at the root of the project in order to
build the project.

Note: `mvn test` will run al the tests and `mvn clean` will remove the results of the previous build.
# Running the service
**IMPORTANT: In order to be able to use the service for sending emails, you MUST set 
your SendGird API key as an environment variable having the following key: SENDGRID_API_KEY.
Otherwise you won't be able to authenticate to SendGrid and send emails.**

By default the service runs on port 8080. You can change this configuration by overriding
"server.port" property in application.properties file. 

The provided database is H2 (in memory database). If you want to use a separate database server,
add the driver in the pom.xml as a dependency and change the configuration in application.properties file with the appropriate values for: 
- spring.datasource.url
- spring.datasource.driverClassName
- spring.datasource.username
- spring.datasource.password
- spring.jpa.database-platform

You can run the service directly from the IDE or by using `java -jar <location to the jar generated by maven in target direcotry>`

# CI/CD

The tools used for Continuous Integration and Continuous Development are Travis CI and Heroku.
The configured pipeline can be found in .travis.yml.

The build status and history is available here: [https://travis-ci.org/BogdanVidrean/mail-service].

The swagger interface for the artifact deployed on Heroku can be found here: [https://mail-service321.herokuapp.com/swagger-ui.html], or you can send POST request to the following available endpoint [https://mail-service321.herokuapp.com/api/emails].\
**NOTE**: 

1) The testing service runs on a Free Plan of SendGrid, so there might be some limitations regarding api access,
and emails. Also check Spam folders because the Free Plan uses shared IP addresses not private ones, so it is
possible to share the same IP address with a scammer who also uses SendGrid.
2) The yahoo blocked all the incoming email so far (surprisingly I was able to send twice an email with an address
from their domain). I recommend using Gmail, and also check the spam folder. I was also able to use Microsoft's Outlook.

The process consists of the following steps:
1. Start a new build (including running all tests available) after every push or merge in master branch.
2. In case of successful build, deploy the new artifact on Heroku.


# Design and Implementation

This web service is implemented using Java with Spring Boot. In order to facilitate the development process,
I decided to use SendGrid as the mail service used to send emails. For the authentication to this service
I applied the recommended approach by using an Api Key of the account to authenticate to the service, and also
the v3 Api and official Java library that can be found here: [https://github.com/sendgrid/sendgrid-java].

The most important implementation decisions are the following ones:
1) The project is structured in a Layered Architecture (data layer, service layer with business logic,
   and presentation layer with the Apis). I went for this approach in order to separate the responsibilities and concerns.

2) Creating a simple caching mechanism for SendGrid client provided by the SendGrid java library,
in order to handle multiple requests at the same time. As we can observe here [https://github.com/sendgrid/sendgrid-java/issues/213] and
here [https://github.com/sendgrid/sendgrid-java/issues/435],
a SendGrid client cannot handle in parallel multiple request (they are executed in a sequential manner) and
also caching the configuration is not thread safe.
I implemented a mechanism that instantiates several clients and reuse them by synchronizing the access
using a Blocking Queue from java.util.concurrent. The code is implemented in the following class: [SendGridRequestHandler.java](./src/main/java/com/socialnetwork/mailservice/utils/sendgrid/SendGridRequestHandler.java).
Each time there is a new mail request, we try to obtain from the cache a SendGrid client. If all of them
are taken, we wait until one of them is returned. After we finish the request the client is
released and stored back in the cache.

3) Guaranteed delivery of emails. Because we use a third-party service that we cannot control,
there is always a possibility that it is unavailable or offline. In this case, if we managed to reach
our mail service, it was designed to store in its database all the messages that failed to be delivered to
SendGrid service (there was a network exception, or the service returned 500 which means it is unavailable,
or 503 which indicates that the v3 API is not currently available). I also created a Scheduled
[job](src/main/java/com/socialnetwork/mailservice/utils/schedulers/FailedEmailsRetryTask.java)
 that runs every 15 minutes and collects the pending messages that are stored in the database (processes
them in batches of 1000), and tries to resend them to SendGrid. All successfully sent messages, and the ones
that are rejected by SendGrid are collected in a list that will be removed from the database.
Here I decided to use Spring Data (which offers access to JpaRepositories) and @Transactional.
The library also uses Hibernate under the hood (to implement the Java Persistence Api), so we have
the mapped entity Email.

4) Using Liquibase to offer a better solution for feature updates of the schema. We definitely cannot rely
on the update plan offered by Hibernate, which tries to update the schema as much as it can. For this
reason I implemented the skeleton required to use liquibase for our mail service, and created an initial [changeset](src/main/resources/db/changelogs/db.changes.xml) which
creates the requried tables for Email entity and the additional ones for the join with the collections from email
(to, cc and bcc).

5) Swagger for easier testing and demo. I configured Swagger in order to offer a better interface for demo and testing
purposes, especially for the available [service](https://mail-service321.herokuapp.com/swagger-ui.html) from Heroku,
or by accessing `protocol://host:port/swagger-ui.html if you run the service on you computer.

6) For creating an instance of an Email that is accepted by SendGrid I created a
builder [class](src/main/java/com/socialnetwork/mailservice/utils/sendgrid/SendGridEmailBuilder.java) that allows you to customize the email as you wish.

7) Testing. For the testing part, I implements tests for all the layers (api, business and data). For the data
layer I used a H2 configuration that creates a new database before each suite of tests, and in this manner
we are capable to test our integration of the product with a real database.
    Libraries used for testing:
    - JUnit 4
    - Mockito (used for both business and api layer, to mock the connection to the previous layer, e.g. connection
    to the database)
    - Hamcrest (for a better approach for asserting using Hamcrest Matchers)
    
# API

The service offers one POST api which has the following structure:
   
METHOD: POST\
PATH: /api/emails
BODY:

    {
        "from": "address of the sender",
        "to":  ["array of strings separated by comma, which represents the receipents of the email"],
        "cc":  ["array of strings separted by comma, which represents the carbon copy recipients of the email"],
        "bcc": ["array of strings separted by comma, which represents the blind carbon copy recipients of the email"],
        "subject": "the subject of the email",
        "body": "the body of the email"
    }
CONSTRAINTS: \
The following fields cannot be empty: "from"
The following fields cannot be empty due to SendGrid limitation: "subject", "body"
The following array cannot be empty: "to"

RESPONSE:\
    - 202 Accepted: The email was accepted by the server (It also returns a JSON containing all the headers that were populated by SendGrid and that help to identify the email
    in the dashboard. It is empty for email cached on the mail service due to inactivity of SendGrid)\
    - 400 Bad Request: The provided information is not valid\
    - 500 Internal Server Error: Something went wrong on the server side.
    
# Possible Future Enhancement

We can make a second fail safe mechanism by filtering all the activity
(using messages API), and a scheduled task that collects all failed messages and sends them
again. Unfortunately this api is not available in the Free Plan offered by SendGrid and
requires additional billing [https://sendgrid.com/docs/for-developers/sending-email/getting-started-email-activity-api/].
In this manner it also depends on the client, and the billing plan that he wants to adopt. 