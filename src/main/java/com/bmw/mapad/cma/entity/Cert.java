package com.bmw.mapad.cma.entity;

import lombok.*;

import java.util.Date;

/**
 * This class store details about a digital certificate (i.e expiring date).
 */
@Getter
@ToString
@AllArgsConstructor
public abstract class Cert {
    private String project;
    private final String alias;
    private final Date startAfter;
    private final Date finishBefore;
    private final String source;
    @Setter
    private String issueID;

    @Override
    public boolean equals(Object o) {
        if (o instanceof CertX509) {
            CertX509 temp = (CertX509) o;
            return getAlias().equals(temp.getAlias()) && getProject().equals(temp.getProject());
        }
        return false;
    }
}
