package com.bmw.mapad.cma.notifier;

import com.bmw.mapad.cma.entity.Cert;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Class that aggregates several strategies to notify the certificates.
 */
@RequiredArgsConstructor
@Service
public class NotifierContext {
    final List<NotifierStrategy> notifierStrategies;
    @Value("${cma.notifier.strategies}")
    String[] strategies;

    /**
     * Notify multiple certificates through several platforms passed as argument.
     * In case of more than one strategy to notify, the order is defined by priorities defined in notifiers.
     *
     * @param certs      Certificates to be notified.
     * @param days       Define the timeframe in which the notifier should trigger a notification.
     * @return Certificates which were notified successfully.
     */
    public Map<NotifierType, List<Cert>> notify(List<Cert> certs, int days) {
        Map<NotifierType, List<Cert>> notifiedCerts = new HashMap<>();

        List<NotifierStrategy> notifiers = notifierStrategies.stream()
                .filter(notifier -> Arrays.stream(strategies)
                        .map(NotifierType::stringToNotifier)
                        .anyMatch(strategy -> notifier.getNotifierStrategy().equals(strategy)))
                .sorted()
                .collect(Collectors.toList());;

        notifiers.forEach(notifier -> {
            notifiedCerts.put(notifier.getNotifierStrategy(), notifier.notifyCertsByDate(certs, days));
        });

        return notifiedCerts;
    }
}
