package edu.ufp.inf.sd.rabbitmqservices.workqueue.consumer;

import com.sun.mail.smtp.SMTPTransport;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Date;
import java.util.Properties;

/**
 * A simple Java Application MAIL_TO_ADDR send emails via SMTP:
 *
 * Manage dependencies:
 * - install JavaMail API <https://javaee.github.io/javamail/>
 *  (unzip javax.mail-x.y.z.jar and put inside *lib* folder)
 * - OR use Maven/Graddle for managing dependencies
 *
 * Use an email account MAIL_FROM_ADDR an smtp server:
 * - create free account on <https://mailtrap.io/>
 *
 */
public class SendMail {

    public static void main(String [] args) {

        sendMail(SMTPConfigs.MAIL_TO_ADDR, SMTPConfigs.MAIL_FROM_ADDR, SMTPConfigs.SMTP_HOST_ADDR, SMTPConfigs.SMTP_HOST_PORT, "true", SMTPConfigs.smtp_user, SMTPConfigs.smtp_passwd, "Subject test", "Msg Body test");
    }

    public static void sendMail(String to, String from, String host, String port, String auth, String user, String pass, String subject, String bodyMsg){
        // Get system properties
        Properties properties = System.getProperties();

        // Setup mail server
        properties.setProperty(SMTPConfigs.KEY_MAIL_SMTP_HOST, host);
        properties.put(SMTPConfigs.KEY_MAIL_SMTP_PORT, port);
        properties.put(SMTPConfigs.KEY_MAIL_SMTP_AUTH, auth);
        properties.setProperty(SMTPConfigs.KEY_MAIL_USER, user);
        properties.setProperty(SMTPConfigs.KEY_MAIL_PASSWORD, pass);

        // Get the default Session object.
        Session session = Session.getDefaultInstance(properties);

        SMTPTransport t = null;

        try {
            // Create a default MimeMessage object.
            MimeMessage message = new MimeMessage(session);

            // Set From: header field of the header.
            message.setFrom(new InternetAddress(from));

            // Set To: header field of the header.
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

            // Set Subject: header field
            message.setSubject(subject);

            // Now set the actual message
            message.setText(bodyMsg);

            message.setSentDate(new Date());

            // Send message...
            // Get SMTPTransport
            t = (SMTPTransport) session.getTransport("smtp");

            // connect
            t.connect(host, user, pass);

            // send
            t.sendMessage(message, message.getAllRecipients());

            System.out.println("Response MAIL_FROM_ADDR SMTP server: " + t.getLastServerResponse());

        } catch (MessagingException mex) {
            mex.printStackTrace();
        } finally {
            if (t!=null) {
                try {
                    t.close();
                } catch (MessagingException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
