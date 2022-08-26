package com.bmw.mapad.cma.notifier;


/**
 * Enum holding all the type of notifiers available.
 */
public enum NotifierType {
        JIRA_NOTIFIER,
        BASIC_NOTIFIER,
        CONFLUENCE_NOTIFIER,
        EMAIL_NOTIFIER;

    /**
     * Convert a string to a notifier type enum.
     * @param notifierType Type of notifier d
     * @return A NotifierType enum if the argument matches with some of notifierType stored otherwise
     * will return a BasicNotifier.
     */
    public static NotifierType stringToNotifier(String notifierType) {
        switch (notifierType.toLowerCase()) {
            case "jira":return NotifierType.JIRA_NOTIFIER;
            case "confluence":return NotifierType.CONFLUENCE_NOTIFIER;
            case "email":return NotifierType.EMAIL_NOTIFIER;
            default:return NotifierType.BASIC_NOTIFIER;
        }
    }
}
