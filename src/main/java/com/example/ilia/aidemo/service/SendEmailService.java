package com.example.ilia.aidemo.service;

import lombok.AllArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;


@Service
@AllArgsConstructor
public class SendEmailService {
    private JavaMailSender mailSender;


    public void sendResetPassword(String recipient, String resetCode){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("ilialimits222@gmail.com");
        message.setTo(recipient);
        message.setText("Your code to reset the password is: " + resetCode);
        message.setSubject("Code for resetting your password");

        mailSender.send(message);

        System.out.println("Sent successfully");

    }

    public void sendEmailToContactUs(String recipient, String messageToSend){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("ilialimits222@gmail.com");
        message.setTo("il96@njit.edu");
        message.setText("The message was from: " + recipient + "\n\nHis message was: \n" + messageToSend);
        message.setSubject("Message from customer");

        mailSender.send(message);

        System.out.println("Sent successfully");

    }



}
