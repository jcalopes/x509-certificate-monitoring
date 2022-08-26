package com.bmw.mapad.cma.notifier.jiraService.dto;

import lombok.Data;
import lombok.Getter;

import java.util.ArrayList;

/**
 * This class hold a Jira issue structure in order to match with request payload.
 */
public class TicketJiraCreation {
    @Getter
    final Fields fields;

    public TicketJiraCreation(String projectKey, String issueType, String summary, String description, ArrayList<String> labels) {
        this.fields = new Fields(summary, new IssueType(issueType), new Project(projectKey), description, labels);
    }

    @Data
    public class Fields{
        final String summary;
        final IssueType issuetype;
        final Project project;
        final String description;
        final ArrayList<String> labels;
    }

    @Data
    public class IssueType{
        final String id;
    }

    @Data
    public class Project{
        final String id;
    }
}
