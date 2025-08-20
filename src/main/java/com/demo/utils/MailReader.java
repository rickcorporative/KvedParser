package com.demo.utils;

import com.demo.core.logger.DefaultLogger;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMultipart;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MailReader extends DefaultLogger {
    private final String hostName = "imap.gmail.com";
    private String username;
    private String password;
    private int messageCount;
    private int unreadMsgCount;
    private String emailSubject;
    private Message emailMessage;
    private ThreadLocal<String> message = new ThreadLocal<>();
    Properties sysProps = System.getProperties();

    public MailReader() {
        sysProps.setProperty("mail.store.protocol", "imaps");
        sysProps.setProperty("mail.imaps.ssl.trust", "*");
        sysProps.setProperty("mail.imaps.port", "993");
        sysProps.setProperty("mail.imaps.ssl.enable", "true");
        sysProps.setProperty("mail.imaps.auth.mechanisms", "PLAIN");
    }

    public MailReader(String username, String password) {
        this();
        this.username = username;
        this.password = password;
    }

    public String getMessage() {
        return message.get();
    }

    public void readMessage() {

        try {
            Folder emailInbox = openFolder("INBOX");
            emailInbox.open(Folder.READ_WRITE);
            messageCount = emailInbox.getMessageCount();
            //logInfo("Total Message Count: " + messageCount);
            unreadMsgCount = emailInbox.getNewMessageCount();
            //logInfo("Unread Emails count:" + unreadMsgCount);
            emailMessage = emailInbox.getMessage(messageCount);
            emailSubject = emailMessage.getSubject();
            //logInfo("Email subject:" + emailSubject);
            //logInfo("Email content:" + emailMessage.getContent());

            message.set(getTextFromMessage(emailMessage));
            //logInfo("Message text: " + message);
            setFlagSeenMessage(emailMessage);
            emailInbox.close(true);
            closeStore();

        } catch (Exception mex) {
            logError("Failed to read email message", mex);        }
    }

    public String getTextFromEmail(String recipient, String subject, int timeoutSec) {
        Folder inbox = openFolder("INBOX");

        Predicate<Message> filter = msg ->
                isRecipient(recipient, msg);

        String text;
        try {
            text = waitAndExtractText(filter, timeoutSec, inbox);
        } finally {
            closeFolder(inbox);
            closeStore();
        }

        if (text.isEmpty()) {
            logError("No email found with subject: " + subject);
        }

        return text.replaceAll("amp;", "").replace("\"", "");
    }


    public String getTextFromEmail(String recipient,
                                   String subject,
                                   int timeoutSec,
                                   LocalDateTime afterThisTime) {
        Folder inbox = openFolder("INBOX");

        Date afterDate = Date.from(afterThisTime
                .atZone(ZoneId.systemDefault())
                .toInstant());

        Predicate<Message> filter = msg ->
        {
            try {
                return isRecipient(recipient, msg) &&
                        msg.getReceivedDate() != null &&
                        msg.getReceivedDate().after(afterDate);
            } catch (MessagingException e) {
                logError("Error while filtering email: " + e.getMessage());
                return false;
            }
        };

        String text;
        try {
            text = waitAndExtractText(filter, timeoutSec, inbox);
        } finally {
            closeFolder(inbox);
            closeStore();
        }

        if (text.isEmpty()) {
            logError("No email found with subject: " + subject);
        }

        message.set(text);
        return text.replaceAll("amp;", "").replace("\"", "");
    }

    public String getTextFromEmailWithPhone(String recipient,
                                            String subject,
                                            int timeoutSec) {
        Folder inbox = openFolder("INBOX");

        Predicate<Message> filter = msg ->
                isRecipient(recipient, msg) &&
                        subjectMatches(msg, subject, "phone");

        String text;
        try {
            text = waitAndExtractText(filter, timeoutSec, inbox);
        } finally {
            closeFolder(inbox);
            closeStore();
        }

        if (text.isEmpty()) {
            logError("No email found with subject: " + subject);
        }

        return text;
    }

    public String getTextFromEmailWithAddress(String recipient, String subject, int timeoutSec) {
        Folder inbox = openFolder("INBOX");

        Predicate<Message> filter = msg ->
                isRecipient(recipient, msg) &&
                        subjectMatches(msg, subject, "address");

        String text;
        try {
            text = waitAndExtractText(filter, timeoutSec, inbox);
        } finally {
            closeFolder(inbox);
            closeStore();
        }

        if (text.isEmpty()) {
            logError("No email found with subject: " + subject);
        }

        return text;
    }

    public List<String> getTextFromEmails(String recipient, String subject, int timeout) {
        Folder inbox = openFolder("INBOX");

        List<String> allTexts = new ArrayList<>();
        for (int i = 0; i <= timeout; i++) {
            PlaywrightTools.sleep(Constants.NANO_TIMEOUT);

            Message[] messages = fetchLastMessages(inbox);
            boolean isPresent = false;

            for (Message msg : messages) {
                if (isRecipient(recipient, msg)) {

                    String text = getTextFromMessage(msg);
                    allTexts.add(text);
                    setFlagSeenMessage(msg);
                    isPresent = true;
                }
            }
            if (isPresent) break;
        }

        if (!allTexts.isEmpty()) {
            message.set(allTexts.get(allTexts.size() - 1));
        }

        return allTexts;
    }

    public void waitUntilEmailPresent(String recipient, String subject, int timeout) {
        Folder inbox = openFolder("INBOX");

        Predicate<Message> filter = msg -> {
            boolean recipientMatch = isRecipient(recipient, msg);
            if (!recipientMatch) return false;

            try {
                String subj = msg.getSubject();
                return subj != null && (
                        subj.contains(subject) ||
                                ("OTP".equals(subject))
                );
            } catch (MessagingException e) {
                return false;
            }
        };

        try {
            int i = 0;
            while (i <= timeout) {
                PlaywrightTools.sleep(Constants.NANO_TIMEOUT);

                Message[] messages = fetchLastMessages(inbox);
                for (Message msg : messages) {
                    if (filter.test(msg)) {
                        String text = getTextFromMessage(msg);
                        message.set(text);
                        setFlagSeenMessage(msg);
                        logInfo("============== TIME SPENT ON WAITING FOR EMAIL: " + i + "S ==============");
                        logInfo(text);
                        return;
                    }
                }
                i++;
            }
        } catch (Exception e) {
            logError("Failed to wait for email. Recipient: '{}', Subject: '{}', Timeout: {}", e, recipient, subject, timeout);
        } finally {
            closeFolder(inbox);
            closeStore();
        }
    }

    //Functions with work string text of message

    public String getOTP(String message) {
        String OTP = " ";
        Pattern linkPattern = Pattern.compile("[\\d]{6}"); // here you need to define regex as per you need
        logInfo(message);

        Matcher pageMatcher =
                linkPattern.matcher(message);
        while (pageMatcher.find()) {
            OTP = pageMatcher.group(0);
        }
        logInfo(OTP);
        return OTP.substring(OTP.length() - 6);
    }

    public String getOTPFirstLine(String message) {
        String OTP = " ";
        Pattern linkPattern = Pattern.compile("code is:<strong>? [\\d]{6}"); // here you need to define regex as per you need
        Matcher pageMatcher =
                linkPattern.matcher(message);

        while (pageMatcher.find()) {
            OTP = pageMatcher.group(0);
        }
        return OTP.replaceAll("[^0-9]", "");
    }

    public ArrayList<String> getLinksFromMessage(String message) {
        ArrayList<String> links = new ArrayList<>();

        Pattern linkPattern = Pattern.compile
                ("(https?:\\/\\/(?:www\\.|(?!www))[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\\.[^\\s]{2,}|www\\.[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\\.[^\\s]{2,}|https?:\\/\\/(?:www\\.|(?!www))[a-zA-Z0-9]+\\.[^\\s]{2,}|www\\.[a-zA-Z0-9]+\\.[^\\s]{2,})"); // here you need to define regex as per you need
        Matcher pageMatcher = linkPattern.matcher(message);

        while (pageMatcher.find()) {
            links.add(pageMatcher.group(1));
        }
        return links;
    }

    public String getLinkFromMessage(String message, String keyWord) {
        String link = "";
        int linksSize = getLinksFromMessage(message).size();
        for (int i = 0; i < linksSize; i++) {
            link = getLinksFromMessage(message).get(i).replaceAll("\">here</a>.|amp;|\"", "");
            if (link.contains(keyWord))
                break;
        }

        return link;
    }

    //private methods

    private String getTextFromMessage(Message message) {
        String result = "";
        try {
            if (message.isMimeType("text/plain")) {
                result = message.getContent().toString();
            } else if (message.isMimeType("multipart/*")) {
                MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
                result = getTextFromMimeMultipart(mimeMultipart);
            }
        } catch (MessagingException | IOException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    private String getTextFromMimeMultipart(
            MimeMultipart mimeMultipart) throws MessagingException, IOException {
        String result = "";
        int count = mimeMultipart.getCount();
        for (int i = 0; i < count; i++) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
            if (bodyPart.isMimeType("text/plain")) {
                result = result + "\n" + bodyPart.getContent();
                break; // without break same text appears twice in my tests
            } else if (bodyPart.isMimeType("text/html")) {
                String html = (String) bodyPart.getContent();
                //logInfo("PARSE HTML" + org.jsoup.Jsoup.parse(html));
                //logInfo("PARSE HTML TEXT" + org.jsoup.Jsoup.parse(html).text());
                result = result + "\n" + org.jsoup.Jsoup.parse(html);
            } else if (bodyPart.getContent() instanceof MimeMultipart) {
                result = result + getTextFromMimeMultipart((MimeMultipart) bodyPart.getContent());
            }
        }
        return result;
    }

    private String waitAndExtractText(Predicate<Message> matcher, int timeoutSec, Folder folder) {
        int spentTime = 0;
        try {
            while (spentTime <= timeoutSec) {

                for (Message msg : fetchLastMessages(folder)) {
                    if (matcher.test(msg)) {
                        String text = getTextFromMessage(msg);
                        logInfo("Email text - " + text);
                        setFlagSeenMessage(msg);
                        return text;
                    }
                }

                PlaywrightTools.sleep(Constants.NANO_TIMEOUT);
                spentTime++;
            }
            logInfo("============== TIME SPENT ON WAITING FOR EMAIL: " + spentTime + "S ==============");
            return "";
        } catch (Exception e) {
            logError("Failed to extract email text after waiting {} seconds", e, spentTime);
            return "";
        }
    }

    private void setFlagSeenMessage(Message message) {
        try {
            message.setFlag(Flags.Flag.SEEN, true);
        } catch (MessagingException e) {
            logError("Error while marking email as read: " + e.getMessage());
        }
    }

    private Store getStoreFromSession() {
        Session session = Session.getInstance(sysProps, null);
        try {
            return session.getStore();
        } catch (MessagingException e) {
            logError("Error while getting store: " + e.getMessage());
            return null;
        }
    }

    private void connectStore(Store store) {
        try {
            store.connect(hostName, username, password);
        } catch (MessagingException e) {
            logError("Error while connecting store: " + e.getMessage());
        }
    }

    private void closeStore() {
        Store store = getStoreFromSession();
        if (store != null) {
            try {
                store.close();
            } catch (MessagingException e) {
                logError("Error while closing store: " + e.getMessage());
            }
        }
    }

    private Message[] fetchLastMessages(Folder inbox) {
        try {
            int count = inbox.getMessageCount();
            Message[] messages = inbox.getMessages(Math.max(1, count - 5), count);
            /* DEBUGGING
            try {
                for (Message msg : messages) {
                    logInfo("Email subject: " + msg.getSubject());
                    logInfo("Email received date: " + ZonedDateTime.ofInstant(msg.getReceivedDate().toInstant(), ZoneId.systemDefault()));
                    logInfo("Email from: " + msg.getFrom()[0]);
                    logInfo("Email to: " + msg.getAllRecipients()[0]);
                    logInfo("Email content: " + msg.getContent());
                    logInfo("Email content type: " + msg.getContentType());
                    logInfo("Email content class: " + msg.getContent().getClass());
                }
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
             */
            return messages;
        } catch (MessagingException e) {
            logError("Error while fetching messages: " + e.getMessage());
            return null;
        }
    }

    private Folder openFolder(String folderName) {
        try {
            Store store = getStoreFromSession();
            if (store == null) {
                return null;
            }
            connectStore(store);

            Folder inbox = store.getFolder(folderName);
            inbox.open(Folder.READ_WRITE);
            return inbox;
        } catch (MessagingException e) {
            logError("Failed to open inbox: " + e.getMessage());
            return null;
        }
    }


    private void closeFolder(Folder folder) {
        try {
            folder.close(true);
        } catch (MessagingException e) {
            logError("Failed to close email folder", e);
        }
    }

    private boolean isRecipient(String expected, Message msg) {
        try {
            Address[] rcpts = msg.getAllRecipients();
            return rcpts != null && rcpts.length > 0 &&
                    expected.equalsIgnoreCase(((InternetAddress) rcpts[0]).getAddress());
        } catch (MessagingException e) {
            return false;
        }
    }

    private boolean subjectMatches(Message msg, String... needles) {
        try {
            String subj = msg.getSubject();
            if (subj == null) return false;
            for (String n : needles) {
                if (subj.contains(n)) return true;
            }
        } catch (MessagingException ignored) {
        }
        return false;
    }
}
