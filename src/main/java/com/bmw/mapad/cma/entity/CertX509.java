package com.bmw.mapad.cma.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import java.math.BigInteger;
import java.util.Comparator;
import java.util.Date;

/**
 * This class store data related to certificate X.509 standard structure.
 */
@ToString(callSuper = true)
@Getter
public class CertX509 extends Cert {
    private final BigInteger serialNumber;

    @Builder
    public CertX509(@NonNull BigInteger serialNumber, @NonNull String alias, @NonNull Date startAfter,
                    @NonNull Date finishBefore, String project, String source, String issueID) {
        super(project, alias, startAfter, finishBefore, source, issueID);
        this.serialNumber = serialNumber;
    }
}
