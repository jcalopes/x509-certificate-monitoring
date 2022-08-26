package com.bmw.mapad.cma.notifier;

import com.bmw.mapad.cma.entity.Cert;

import java.util.List;
/**
 * This interface represents a notifier tool abstraction. The user of this interface is able to notify someone through a platform chosen
 * by the concretion class.
 */
public interface NotifierStrategy extends Comparable<NotifierStrategy> {

    /**
     * Notify the target platform about all certificates that expire within the timeframe established the number of days passed as argument.
     * @param listCerts Set of certificates to be notified regarding the date criteria aforementioned.
     * @param days Threshold of the days to define the timeframe that will trigger a notification.
     * @return List of certificates which has been actually successfully notified.
     * Ex: If days = 10, threshold to be notified will be set as Date.now() + 10
     */
    List<Cert> notifyCertsByDate(List<Cert> listCerts, int days);

    /**
     * Get the notification strategy name.
     * @return Notifier strategy name.
     */
    NotifierType getNotifierStrategy();

    /**
     * Get the priority over other notifiers. For instance, notifier implementations with priority = 1 will trigger
     * before other notifiers. Notifiers with the same priority will be triggered randomly so make sure to coordinate
     * along with other priority notifiers to ensure the expected flow.
     * @return The priority which the app should trigger this notifier among others.
     */
    int getPriority();
}
