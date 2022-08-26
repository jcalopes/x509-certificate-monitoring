package com.bmw.mapad.cma.notifier.basicNotifier;

import com.bmw.mapad.cma.entity.Cert;
import com.bmw.mapad.cma.notifier.NotifierStrategy;
import com.bmw.mapad.cma.notifier.NotifierType;
import com.bmw.mapad.cma.utils.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service("BasicNotifier")
@RequiredArgsConstructor
/**
 * Class represents an implementation of the notifier. Only send the data for the screen of the injected certificates.
 * Useful to check the behavior of the whole application and used just for test purposes.
 */
public class BasicNotifierService implements NotifierStrategy {
    final Utils utils;
    @Value("${cma.notifier.basic.priority:2}")
    int priority;

    /**
     * Returns all certificates from this key file that will expire within 30 days.
     *
     * @param allExtractedCert Certificates to be evaluated the need to notify them.
     * @param days Days representing the time frame which certificates should be notified.
     */
    @Override
    public List<Cert> notifyCertsByDate(List<Cert> allExtractedCert, int days) {
        Date limitDate = utils.setLimitDateForNotification(days);

        List<Cert> certsFound = allExtractedCert.stream()
                .filter(cert -> cert.getFinishBefore().compareTo(limitDate) <= 0)
                .collect(Collectors.toList());

        try {
            utils.exportToCsvFormat(certsFound, "certificatesOverview", "Alias", "Project","Source", "Start_date", "Expiration_date", "IssueID");
            log.info("certificatesOverview.csv was created successfully.");
        } catch (IOException | ParseException e) {
            log.error("An error occurred while exporting the csv file. {}",e.getMessage());
        }

        return certsFound;
    }

    /**
     * Return the notification strategy name.
     * @return Notifier strategy name.
     */
    @Override
    public NotifierType getNotifierStrategy() {
        return NotifierType.BASIC_NOTIFIER;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public int compareTo(NotifierStrategy other) {
        return Integer.compare(getPriority(),other.getPriority());
    }
}
