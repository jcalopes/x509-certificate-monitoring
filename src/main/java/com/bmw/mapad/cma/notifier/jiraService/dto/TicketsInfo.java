package com.bmw.mapad.cma.notifier.jiraService.dto;

import com.bmw.mapad.cma.entity.Cert;
import com.bmw.mapad.cma.entity.CertX509;
import lombok.Data;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Class to store the tickets' information retrieved by Jira API response regarding issues created by this project.
 */
@Data
public class TicketsInfo {
    public int maxResults;
    public int total;
    public ArrayList<Issue> issues;

    @Data
    public class Issue{
        public String key;
        public Fields fields;
    }

    @Data
    public class Fields{
        public Issuetype issuetype;
        public Project project;
        public Date created;
        public Priority priority;
        public ArrayList<String> labels;
        public Status status;
        public String description;
        public String summary;
        public String getAlias(){
            String alias = "";
            alias = summary.substring(summary.lastIndexOf(":") + 2);
            return alias;
        }
    }

    @Data
    public class Issuetype{
        public String self;
        public String id;
        public String description;
        public String iconUrl;
        public String name;
        public boolean subtask;
    }
    @Data
    public class Priority{
        public String name;
        public String id;
    }

    @Data
    public class Project{
        public String id;
        public String key;
        public String name;
        public String projectTypeKey;
    }

    @Data
    public class Status{
        public String name;
        public String id;
        public StatusCategory statusCategory;
    }

    @Data
    public class StatusCategory{
        public int id;
        public String key;
    }

    public List<Cert> mapToCert(){
        List<Cert> certsFound = new ArrayList<>();
        for(Issue issue:issues){
            certsFound.add(CertX509.builder()
                    .serialNumber(BigInteger.ONE)
                    .alias(issue.fields.getAlias())
                    .issueID(issue.key)
                    .finishBefore(new Date())
                    .startAfter(new Date())
                    .source("jiraApi")
                    .build());
        }
        return certsFound;
    }
}
