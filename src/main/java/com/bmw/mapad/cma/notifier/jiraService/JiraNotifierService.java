package com.bmw.mapad.cma.notifier.jiraService;

import com.bmw.mapad.cma.entity.Cert;
import com.bmw.mapad.cma.notifier.NotifierStrategy;
import com.bmw.mapad.cma.notifier.NotifierType;
import com.bmw.mapad.cma.notifier.jiraService.dto.TicketJiraCreation;
import com.bmw.mapad.cma.notifier.jiraService.dto.TicketResponse;
import com.bmw.mapad.cma.utils.Utils;
import com.bmw.mapad.cma.utils.httpClient.JiraApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import retrofit2.Response;
import retrofit2.Retrofit;


import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class represents an implementation of a notifier.Provide a way to notify the development teams by creating issues
 * alerting the need to replace some certificates.
 */
@Slf4j
@Service("JiraNotifier")
@RequiredArgsConstructor
public class JiraNotifierService implements NotifierStrategy {
    final Utils utils;
    @Qualifier("jiraClient")
    final Retrofit httpClient;
    @Value("${cma.notifier.jira.projectID}")
    String projectId;
    @Value("${cma.notifier.jira.label:certAlarm}")
    String label;
    @Value("${cma.notifier.jira.issueType}")
    String issueType;
    @Value("${cma.notifier.jira.priority:1}")
    int priority;

    /**
     * Return a body structure with the information from a certificate passed as an argument to be used .
     *
     * @param certData Certificate to be notified.
     * @return Jira issue structure to be sent through GET request.
     */
    TicketJiraCreation buildBodyRequest(Cert certData) {
        String summaryText = "Certificate expiring on " +
                new Date() + ": " +
                certData.getAlias();

        String descriptionText = " \n" +
                "Info:" + "\n Source: " + certData.getProject() +
                "\n Alias: " + certData.getAlias() +
                "\n Expire before: " + certData.getFinishBefore();

        ArrayList<String> labels = new ArrayList<>();
        labels.add(label);

        return new TicketJiraCreation(projectId, issueType, summaryText, descriptionText,labels);
    }

    /**
     * Notify by creating a Jira issue only if the validity of some certificate is covered by the timeframe
     * defined by argument. The Jira issue creation is performed asynchronously by using Jira Rest api.
     * Ex: If days = 10, threshold to be notified will be set as certificate validity <= Date.now() + 10.
     *
     * @param listCerts Set of certificates to be notified regarding the date criteria aforementioned.
     * @param days      Threshold of the days to define the timeframe that will trigger a notification.
     * @return List of certificates which has been notified successfully.
     */
    @Override
    public List<Cert> notifyCertsByDate(List<Cert> listCerts, int days) {
        JiraApi jiraApi = httpClient.create(JiraApi.class);
        Date limitDate = utils.setLimitDateForNotification(days);

        List<Cert> targetCerts = listCerts.stream()
                .filter(cert -> cert.getFinishBefore().compareTo(limitDate) <= 0)
                .collect(Collectors.toList());

        List<Cert> notifiedCerts = new ArrayList<>();
        try {
            targetCerts = utils.checkUnresolvedIssues(jiraApi,projectId,label,targetCerts);
            for (Cert cert : targetCerts) {
                if (cert.getIssueID().equals("NO_ISSUE")) {
                    Response<TicketResponse> response = jiraApi.createIssue(buildBodyRequest(cert)).execute();
                    if (response.code() == 201 && response.body() != null) {
                        notifiedCerts.add(cert);
                        cert.setIssueID(response.body().getKey());
                        log.info("Notified successfully via Jira.Issue ID:{}", cert.getIssueID());
                    } else {
                        log.error("An error occurred notifying via Jira.Code: {} Msg:{}", response.code(), response.errorBody());
                    }
                }
            }
            utils.exportToCsvFormat(targetCerts, "jira_overview", "Alias", "Source", "Start_date", "Expiration_date", "IssueID");
        } catch (IOException | ParseException e) {
            log.error("An error occurred exporting the Jira Notification report. " +
                    "This situation could lead duplicate Jira tickets in the future. ,{}", e.getMessage());
        }
        return notifiedCerts;
    }

    /**
     * Identify the type of this notifier.
     *
     * @return Notifier type.
     */
    @Override
    public NotifierType getNotifierStrategy() {
        return NotifierType.JIRA_NOTIFIER;
    }

    /**
     * Return the priority that this notifier should be triggered among other selected notifiers.
     *
     * @return Priority level.
     */
    @Override
    public int getPriority() {
        return priority;
    }

    /**
     * Method to compare with another notifier strategy based its priority level.
     *
     * @param other The notifier strategy to be compared.
     * @return The value 0 if x == y; a value less than 0 if x < y; and a value greater than 0 if x > y
     */
    @Override
    public int compareTo(NotifierStrategy other) {
        return Integer.compare(getPriority(), other.getPriority());
    }
}
