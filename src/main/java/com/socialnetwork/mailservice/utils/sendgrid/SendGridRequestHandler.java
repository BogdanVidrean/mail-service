package com.socialnetwork.mailservice.utils.sendgrid;

import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static java.lang.System.getenv;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.IntStream.range;

@Component
public class SendGridRequestHandler {
    // we need this caching mechanism because the SendGridClient is not thread safe unless we run it with the same
    // configuration, but even in that case the requests are sequential
    // see https://github.com/sendgrid/sendgrid-java/issues/435 and https://github.com/sendgrid/sendgrid-java/issues/213
    private static final String SENDGRID_API_KEY = "SENDGRID_API_KEY";
    private static final int DEFAULT_NUMBER_OF_INSTANCES = 5;
    private final BlockingQueue<SendGrid> sendGridClientsCache = new LinkedBlockingQueue<>();
    @Value("${send.grid.clients}")
    private Integer numberOfSendGridClients;

    @PostConstruct
    public void init() {
        if (numberOfSendGridClients == null || numberOfSendGridClients < 1) {
            numberOfSendGridClients = DEFAULT_NUMBER_OF_INSTANCES;
        }
        final String sendGridApiKey = getenv(SENDGRID_API_KEY);
        range(0, numberOfSendGridClients).forEach(i -> sendGridClientsCache.add(new SendGrid(sendGridApiKey)));
    }

    /**
     * Sends a request to SendGrid service, by using one SendGrid client that is available in the cache
     *
     * @param request The specific request
     * @return An optional response for the request
     * @throws IOException If there is a problem with the connection to the service
     */
    public Optional<Response> executeRequest(final Request request) throws IOException {
        Optional<SendGrid> sendGridOptional = empty();
        Optional<Response> response = empty();
        try {
            while (!sendGridOptional.isPresent()) {
                try {
                    //  wait until a SendGrid client is available
                    sendGridOptional = of(sendGridClientsCache.take());
                    SendGrid sendGrid = sendGridOptional.get();
                    response = of(sendGrid.api(request));
                } catch (InterruptedException ignored) {
                    // ignore this exception because the interrupted flag might be set by JVM
                }
            }
        } finally {
            while (sendGridOptional.isPresent()) {
                try {
                    sendGridClientsCache.put(sendGridOptional.get());
                    sendGridOptional = empty();
                } catch (InterruptedException ignored) {
                    // ignore this exception because it can be caused by JVM and we need to "release" the sendGridClient
                }
            }
        }
        return response;
    }

}
