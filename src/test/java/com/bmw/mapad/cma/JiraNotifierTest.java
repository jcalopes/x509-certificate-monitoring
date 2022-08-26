package com.bmw.mapad.cma;

import com.bmw.mapad.cma.entity.Cert;
import com.bmw.mapad.cma.entity.CertX509;
import com.bmw.mapad.cma.notifier.jiraService.JiraNotifierService;
import com.bmw.mapad.cma.utils.Utils;
import com.bmw.mapad.cma.utils.httpClient.JiraApi;
import com.bmw.mapad.cma.utils.httpClient.RestClientConfig;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Header;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.math.BigInteger;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@SpringBootTest(classes = {JiraNotifierService.class, RestClientConfig.class, JiraNotifierTest.TestConfig.class})
public class JiraNotifierTest {
    @TestConfiguration
    public static class TestConfig {

        @Bean
        public Utils utilsTestSetup() throws ParseException, IOException {
            Utils utils = Mockito.mock(Utils.class);
            List<Cert> certs = new ArrayList<>();
            CertX509 certX509 = CertX509.builder()
                    .serialNumber(BigInteger.ONE)
                    .alias("alias")
                    .startAfter(Date.from(LocalDate.now().minusDays(5).atStartOfDay(ZoneId.systemDefault()).toInstant()))
                    .finishBefore(Date.from(LocalDate.now().plusDays(5).atStartOfDay(ZoneId.systemDefault()).toInstant()))
                    .source("source/mockedCert.")
                    .project("project")
                    .issueID("NO_ISSUE")
                    .build();
            certs.add(certX509);

            Calendar targetDate = Calendar.getInstance();
            targetDate.setTime(new Date());
            targetDate.add(Calendar.DAY_OF_MONTH, 30);

            Mockito.when(utils.setLimitDateForNotification(30)).thenReturn(targetDate.getTime());
            Mockito.when(utils.importCsvFile(anyString())).thenReturn(certs);
            Mockito.doNothing().when(utils).exportToCsvFormat(certs, "jira_overview", "Alias", "Source", "Start_date", "Expiration_date", "IssueID");
            Mockito.when(utils.checkUnresolvedIssues(any(),anyString(),anyString(),eq(certs))).thenReturn(certs);
            return utils;
        }
    }

    static int port = 8082;

    @Autowired
    Utils utilsTestSetup;

    @Autowired
    JiraNotifierService jiraNotifier;
    static ClientAndServer mockServer;

    /**
     * Override the baseUrl from Jira for using the local mock server .
     *
     * @param registry
     */
    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("cma.notifier.jira.baseUrl", () -> "http://127.0.0.1:8082/");
    }

    /**
     * Initialize the mock server.
     */
    @BeforeAll
    public static void mockJiraApi() {
        mockServer = ClientAndServer.startClientAndServer(port);
    }

    /**
     * Create expectation for an OK HTTP response when is called endpoint to create one ticket in Jira.
     */
    private void expectationForOkStatusResponse() {
        //Mock the request to return a success response
        new MockServerClient("127.0.0.1", port)
                .when(
                        request()
                                .withMethod("POST")
                                .withPath("/issue")
                ).respond(
                        response()
                                .withStatusCode(201)
                                .withHeader(new Header("Content-Type", "application/json; charset=utf-8"))
                                .withBody("{}")
                );
    }

    /**
     * Configure 401 Http response when is called endpoint to create one ticket in Jira with incorrect auth.
     */
    private void expectationForUnauthorizedResponse() {
        //Mock the request to return a success response
        new MockServerClient("127.0.0.1", port)
                .when(
                        request()
                                .withMethod("POST")
                                .withPath("/issue")
                ).respond(
                        response()
                                .withStatusCode(401)
                                .withHeader(new Header("Content-Type", "application/json; charset=utf-8"))
                                .withBody("{message: 'Incorrect user'}")
                );
    }

    /**
     * Testing scenario where there is a need to notify by Jira one certificate which will expire soon and handle with
     * OK Status mocked response.
     */
    @Test
    public void shouldReturnOneCert_WhenNotifiedSuccessfully() {
        mockServer.reset();
        expectationForOkStatusResponse();

        //Given one certificate expiring in 5 days to be notified
        List<Cert> certsToBeEvaluated = new ArrayList<>();
        Date startDate = Date.from(LocalDate.now().minusDays(5).atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(LocalDate.now().plusDays(5).atStartOfDay(ZoneId.systemDefault()).toInstant());
        certsToBeEvaluated.add(
                CertX509.builder()
                        .serialNumber(BigInteger.ONE)
                        .alias("alias")
                        .startAfter(startDate)
                        .finishBefore( endDate)
                        .project("project")
                        .source("source/mockedCert.")
                        .issueID("NO_ISSUE")
                        .build());

        //When evaluate it for the need to create an issue in Jira
        List<Cert> notifiedSuccessfully = jiraNotifier.notifyCertsByDate(certsToBeEvaluated, 30);

        //Then the certificate aforementioned was notified in Jira and returned successfully
        assertThat(notifiedSuccessfully.size()).isEqualTo(1);
    }

    /**
     * Testing scenario where there is the need to notify by Jira one certificate which will expire soon and handle with
     * unauthorized status response.
     */
    @Test
    public void shouldNotReturn_WhenNotNotifiedSuccessfully() {
        mockServer.reset();
        expectationForUnauthorizedResponse();

        //Given one certificate expiring in 5 days to be notified
        List<Cert> certsToBeEvaluated = new ArrayList<>();
        Date startDate = Date.from(LocalDate.now().minusDays(5).atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(LocalDate.now().plusDays(5).atStartOfDay(ZoneId.systemDefault()).toInstant());
        certsToBeEvaluated.add(
                CertX509.builder()
                        .serialNumber(BigInteger.ONE)
                        .alias("alias")
                        .startAfter(startDate)
                        .finishBefore(endDate)
                        .source("source/mockedCert.")
                        .build());

        //When evaluate it for the need to create an issue in Jira
        List<Cert> notifiedSuccessfully = jiraNotifier.notifyCertsByDate(certsToBeEvaluated, 30);

        //Then the certificate aforementioned was not notified in Jira due incorrect authentication.
        assertThat(notifiedSuccessfully.size()).isEqualTo(0);
    }

    /**
     * Tear down the mock server.
     */
    @AfterAll
    public static void shutdownServer() {
        mockServer.stop();
    }
}