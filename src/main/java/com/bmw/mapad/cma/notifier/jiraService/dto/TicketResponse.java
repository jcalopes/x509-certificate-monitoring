package com.bmw.mapad.cma.notifier.jiraService.dto;

import lombok.Data;

/**
 * This class hold the response structure retrieved by Jira Rest API when is created an issue.
 */
@Data
public class TicketResponse {
    final String id;
    final String key;
    final String self;
}
