package com.bmw.mapad.cma.notifier.confluenceService;

import com.bmw.mapad.cma.entity.Cert;
import com.bmw.mapad.cma.notifier.NotifierStrategy;
import com.bmw.mapad.cma.notifier.NotifierType;
import com.bmw.mapad.cma.notifier.jiraService.dto.TicketsInfo;
import com.bmw.mapad.cma.utils.httpClient.ConfluenceAPI;
import com.bmw.mapad.cma.utils.httpClient.JiraApi;
import com.bmw.mapad.cma.utils.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import retrofit2.Response;
import retrofit2.Retrofit;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a service providing a way to notify a set of certificates updating a confluence page.
 */
@Slf4j
@Service("ConfluenceNotifier")
@RequiredArgsConstructor
public class ConfluenceNotifierService implements NotifierStrategy {
    @Value("${cma.notifier.confluence.contentID:0}")
    String contentId;
    @Value("${cma.notifier.confluence.attachmentID:0}")
    String attachmentId;
    @Value("${cma.notifier.jira.projectID:0}")
    String project;
    @Value("${cma.notifier.jira.label:certAlarm}")
    String label;
    @Value("${cma.notifier.confluence.priority:4}")
    int priority;
    final Utils utils;
    final ConfluenceAPI confluenceAPI;
    @Qualifier("jiraClient")
    final Retrofit httpClient;

    /**
     * Notify the state of each certificate passed as argument through Confluence page.
     *
     * @param listCerts Set of certificates to be notified regarding the date criteria aforementioned.
     * @param days      Threshold days that should be triggered a notification.
     * @return Certificates which have been notified.
     */
    @Override
    public List<Cert> notifyCertsByDate(List<Cert> listCerts, int days) {
        List<Cert> notifiedCerts = new ArrayList<>();
        JiraApi jiraApi = httpClient.create(JiraApi.class);

        try {
            listCerts = utils.checkUnresolvedIssues(jiraApi,project,label, listCerts);
            utils.exportToCsvFormat(listCerts, "confluence_overview", "Alias", "Project","Source", "Start_date", "Expiration_date", "IssueID");
            File fileCsv = new File("confluence_overview.csv");
            MultipartBody.Part file = MultipartBody.Part.createFormData("file", fileCsv.getName(),
                    RequestBody.create(MediaType.parse("text/csv"), fileCsv));
            Response<ResponseBody> response = confluenceAPI.updateAttachment(contentId, attachmentId, file).execute();

            if (response.code() == 200) {
                notifiedCerts.addAll(listCerts);
                log.info("Confluence page has been updated successfully!");
            } else {
                log.error("An error occurred notifying through Confluence Platform. Error: {},{}",
                        response.code(), response.errorBody());
            }
        } catch (IOException | ParseException ex) {
            log.error("An error occurred exporting info to cvs format. {}", ex.getMessage());
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
        return NotifierType.CONFLUENCE_NOTIFIER;
    }

    /**
     * Get the priority over other notifiers. For instance, notifier implementations with priority = 1 will trigger
     * before other notifiers. Notifiers with the same priority will be triggered randomly so make sure to coordinate
     * along with other priority notifiers to ensure the expected flow.
     *
     * @return The priority which the app should trigger this notifier among others.
     */
    @Override
    public int getPriority() {
        return priority;
    }

    /**
     * Compares this notifier with another notifier based with its priority.
     *
     * @param other the object to be compared.
     * @return The value 0 if x == y; a value less than 0 if x < y; and a value greater than 0 if x > y
     */
    @Override
    public int compareTo(NotifierStrategy other) {
        return Integer.compare(getPriority(), other.getPriority());
    }
}
