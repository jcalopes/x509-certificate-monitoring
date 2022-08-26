package com.bmw.mapad.cma.notifier.emailService;

import com.bmw.mapad.cma.entity.Cert;
import com.bmw.mapad.cma.notifier.NotifierStrategy;
import com.bmw.mapad.cma.notifier.NotifierType;
import com.bmw.mapad.cma.utils.Utils;
import com.bmw.mapad.cma.utils.exceptions.InvalidEmailFormatException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents an email service that enable sending emails to a registered emails list with customizable structure.
 * Provide a way to alert teams about the state of lifecycle of digital certificates.Useful when some certificate will expire
 * soon and need for replacement to prevent possible service downtime.
 */
@Slf4j
@RequiredArgsConstructor
@Service("MailNotifier")
public class EmailNotifierService implements NotifierStrategy {
    final JavaMailSender mailSender;
    final Utils utils;
    @Value("${cma.notifier.email.from:no@reply.com}")
    String senderEntity;
    @Value("${cma.notifier.email.to:example@mail.com}")
    String[] destinationEmails;
    @Value("${cma.notifier.email.template:Certificates_Overview}")
    String templateMessage;
    @Value("${cma.notifier.email.priority:3}")
    int priority;

    /**
     * Consider use this to build a single email including the sender, the recipient, the subject and the body text.
     *
     * @param certList         Certificates list to build the email properties.
     * @param destinationEmail Target destination for this email.
     * @return A simple mail message ready to be delivery by some email server
     */
    public SimpleMailMessage buildMailStructure(List<Cert> certList, String destinationEmail) throws InvalidEmailFormatException {
        if(utils.isValidEmail(destinationEmail) && utils.isValidEmail(senderEntity)){
            SimpleMailMessage email = new SimpleMailMessage();
            String emailBody = certList.stream()
                    .map(cert->" \n" +
                            "Info:" + "\n Source: " + cert.getProject() +
                            "\n Alias: " + cert.getAlias() +
                            "\n Expire before: " + cert.getFinishBefore())
                    .collect(Collectors.joining());

            email.setTo(destinationEmail);
            email.setFrom(senderEntity);
            email.setSubject("Alert certificate expiring soon. ");
            email.setText(templateMessage.concat(emailBody));
            return email;
        }
        throw new InvalidEmailFormatException("One or more emails have not been sent." +
                " An invalid email was found in the email list supplied.");
    }

    /**
     * Send a single email to all destinations configured in the external file.
     *
     * @param certInfo Target certificate to notify by email.
     */
    void sendEmailBroadcast(List<Cert> certInfo) {
        for (String email : destinationEmails) {
            try {
                SimpleMailMessage emailStructure = buildMailStructure(certInfo, email);
                mailSender.send(emailStructure);
            }catch (InvalidEmailFormatException ex){
                log.error(ex.getMessage());
            }
        }
    }

    /**
     * @param listCerts Set of certificates to be notified regarding the date criteria passed as argument.
     * @param days      Threshold of the days to define the timeframe that will trigger a notification.
     *                  Ex: If days = 10, threshold to be notified will be set as Date.now() + 10
     * @return List of certificates that fits with this timeframe.
     */
    @Override
    public List<Cert> notifyCertsByDate(List<Cert> listCerts, int days) {
        Date limitDate = utils.setLimitDateForNotification(days);
        List<Cert> certsForRenew = listCerts.stream()
                .filter(cert -> cert.getFinishBefore().compareTo(limitDate) <= 0)
                .collect(Collectors.toList());

        sendEmailBroadcast(certsForRenew);
        return certsForRenew;
    }

    /**
     * Identify the type of this notifier.
     * @return Notifier type.
     */
    @Override
    public NotifierType getNotifierStrategy() {
        return NotifierType.EMAIL_NOTIFIER;
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
