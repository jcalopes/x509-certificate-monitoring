package com.bmw.mapad.cma.unit;

import com.bmw.mapad.cma.entity.Cert;
import com.bmw.mapad.cma.entity.CertX509;
import com.bmw.mapad.cma.utils.httpClient.ConfluenceAPI;
import com.bmw.mapad.cma.notifier.confluenceService.ConfluenceNotifierService;
import com.bmw.mapad.cma.utils.Utils;
import com.bmw.mapad.cma.utils.httpClient.JiraApi;
import okhttp3.MediaType;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;


import java.io.IOException;
import java.math.BigInteger;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test class aiming test scenarios in Confluence Service.
 */
@ExtendWith(MockitoExtension.class)
public class ConfluenceServiceTest {
    @Mock
    static Utils utils;

    @Mock
    static ConfluenceAPI confluenceAPI;

    @Mock
    static Retrofit jiraApi;

    @InjectMocks
    ConfluenceNotifierService confluenceNotifier;

    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(confluenceNotifier, "contentId", "000");
        ReflectionTestUtils.setField(confluenceNotifier, "attachmentId", "000");
        ReflectionTestUtils.setField(confluenceNotifier, "project", "000");
        ReflectionTestUtils.setField(confluenceNotifier, "label", "000");
        when(jiraApi.create(JiraApi.class)).thenReturn(null);
    }

    /**
     * Test case for an 200 ok status response from an update confluence request.
     * @param call Mock response from a request to confluence retrofit interface.
     * @throws IOException
     */
    @Test
    public void shouldReturnTwo_WhenExistTwoCert(@Mock Call<ResponseBody> call) throws IOException, ParseException {
        //Given 2 certs but one expire within 5 days.
        Date startDate = Date.from(LocalDate.now().minusDays(5).atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endDate1 = Date.from(LocalDate.now().plusDays(5).atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endDate2 = Date.from(LocalDate.now().plusDays(35).atStartOfDay(ZoneId.systemDefault()).toInstant());

        List<Cert> inputCerts = new ArrayList<>();
        inputCerts.add(CertX509.builder()
                .serialNumber(BigInteger.ONE)
                .alias("alias-cert-should-be-notified")
                .startAfter(startDate)
                .finishBefore(endDate1)
                .project("project")
                .source("source/mockedCert1")
                .build());

        inputCerts.add(CertX509.builder()
                .serialNumber(BigInteger.ONE)
                .alias("alias-cert-should-not-be-notified")
                .startAfter(startDate)
                .finishBefore(endDate2)
                .project("project")
                .source("source/mockedCert2")
                .build());

        //Mock external dependencies
        doNothing().when(utils).exportToCsvFormat(inputCerts,"confluence_overview", "Alias", "Project","Source", "Start_date", "Expiration_date", "IssueID");
        when(utils.checkUnresolvedIssues(any(),anyString(),anyString(),eq(inputCerts))).thenReturn(inputCerts);
        when(confluenceAPI.updateAttachment(any(), any(), any())).thenReturn(call);
        when(call.execute()).thenReturn(Response.success(200, ResponseBody.create(MediaType.parse("text/*"), "{}")));

        //When notify them
        List<Cert> notifiedCerts = confluenceNotifier.notifyCertsByDate(inputCerts, 30);

        verify(confluenceAPI, times(1)).updateAttachment(anyString(), anyString(), any());
        verify(call, times(1)).execute();

        //Then should return 2 with success
        Assertions.assertEquals(2, notifiedCerts.size());
    }

    /**
     * Test case scenario for an 401 unauthorized request regarding authentication issues.
     * @param call Mock response from a request to confluence retrofit interface.
     * @throws IOException
     */
    @Test
    public void shouldNotReturn_WhenNoAuth(@Mock Call<ResponseBody> call) throws IOException, ParseException {
        //Given 2 certs but one expire within 5 days.
        Date startDate = Date.from(LocalDate.now().minusDays(5).atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endDate1 = Date.from(LocalDate.now().plusDays(5).atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endDate2 = Date.from(LocalDate.now().plusDays(35).atStartOfDay(ZoneId.systemDefault()).toInstant());

        List<Cert> inputCerts = new ArrayList<>();
        inputCerts.add(CertX509.builder()
                .serialNumber(BigInteger.ONE)
                .alias("alias-cert-should-be-notified")
                .startAfter(startDate)
                .finishBefore(endDate1)
                .source("source/mockedCert1")
                .build());
        inputCerts.add(CertX509.builder()
                .serialNumber(BigInteger.ONE)
                .alias("alias-cert-should-not-be-notified")
                .startAfter(startDate)
                .finishBefore(endDate2)
                .source("source/mockedCert2")
                .build());

        //Mock external dependencies
        doNothing().when(utils).exportToCsvFormat(inputCerts,"confluence_overview", "Alias", "Project","Source", "Start_date", "Expiration_date", "IssueID");
        when(utils.checkUnresolvedIssues(any(),anyString(),anyString(),eq(inputCerts))).thenReturn(inputCerts);
        when(confluenceAPI.updateAttachment(any(), any(), any())).thenReturn(call);
        when(call.execute()).thenReturn(Response.error(403, ResponseBody.create(MediaType.parse("text/*"), "{}")));

        //When notify them
        List<Cert> notifiedCerts = confluenceNotifier.notifyCertsByDate(inputCerts, 30);

        verify(confluenceAPI, times(1)).updateAttachment(anyString(), anyString(), any());
        verify(call, times(1)).execute();

        //Then should not return any certificate because .
        Assertions.assertEquals(0, notifiedCerts.size());
        Assertions.assertDoesNotThrow(() -> confluenceNotifier.notifyCertsByDate(inputCerts, 30));
    }
}