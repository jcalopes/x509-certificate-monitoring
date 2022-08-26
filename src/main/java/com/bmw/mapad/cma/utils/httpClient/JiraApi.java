package com.bmw.mapad.cma.utils.httpClient;

import com.bmw.mapad.cma.notifier.jiraService.dto.TicketJiraCreation;
import com.bmw.mapad.cma.notifier.jiraService.dto.TicketResponse;
import com.bmw.mapad.cma.notifier.jiraService.dto.TicketsInfo;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;

/**
 * Interface containing set of endpoints to interact with Jira API provided by Atlassian.
 */
public interface JiraApi {
    /**
     * Get request to retrieve a specific issue from Jira platform through its id.
     * The success of this request depends on the level of permissions (e.g Read,Write) available
     * for the user provided in request basic auth.
     *
     * @param issueId Target id for the desired issue.
     * @return All the information related to the issue requested if the request has been correctly submitted.
     */
    @GET("issue/{issueId}")
    Call<ResponseBody> getTaskInfo(@Path("issueId") String issueId);

    /**
     * Create an issue in Jira platform with the structure provided in body.*
     * The success of this request depends on the level of permissions (e.g Read,Write) available
     * for the user provided in request basic auth.
     *
     * @param taskBody Represents the structure of the issue.
     * @return Information about the issue created if the issue has been created successfully.
     */
    @POST("issue")
    Call<TicketResponse> createIssue(@Body TicketJiraCreation taskBody);

    /**
     * Get all issues currently unresolved belonging to a specific project and created within
     * certificate monitoring project scope.
     *
     * @return Tickets information.
     */
    @GET("search")
    Call<TicketsInfo> getIssues(@Query("jql") String filters);
}
