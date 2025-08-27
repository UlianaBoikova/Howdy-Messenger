package com.sitestart.blog.models;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.*;

import java.io.Serializable;


/**
 This class stores and provides information about each existing message.
 */
@Entity
public class MessageFromTo implements Serializable, Comparable<MessageFromTo> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String userFrom;
    private String userTo;

    private String message;

    private String time;
    private String exactTime;

    /**
     This is a default constructor for MessageFromTo class.
     */
    public MessageFromTo() {}

    /**
      Main constructor for message class.
      @param userFrom message sender
      @param userTo the person to whom the message is intended
      @param message current message
     */
    public MessageFromTo(String userFrom, String userTo, String message) {
        this.userFrom = userFrom;
        this.userTo = userTo;
        this.message = message;
        time = "yyyy.MM.dd HH:mm:ss";
        exactTime = "HH:mm";
    }

    /**
      Gets time and date of message sending
      @return time and date of message creation
     */
    public String getTime() {
        return time;
    }

    /**
      Gets exact time of message creation (hh:mm)
      @return time
     */
    public String getExactTime() {
        return exactTime;
    }

    /**
      Sets exact time of message creation (hh:mm)
      @param exactTime time (hh:mm)
     */
    public void setExactTime(String exactTime) {
        this.exactTime = exactTime;
    }

    /**
      Sets date and time of the current message.
      @param time date and time
     */
    public void setTime(String time) {
        this.time = time;
    }

    /**
      Gets an ID of the message
      @return message ID
     */
    public Long getId() {
        return id;
    }

    /**
      ets a message ID
      @param id an ID of the message
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
      Gets sender of the message
      @return user that sent this message
     */
    public String getUserFrom() {
        return userFrom;
    }

    /**
      Sets sender of the message
      @param userFrom user that sent this message
     */
    public void setUserFrom(String userFrom) {
        this.userFrom = userFrom;
    }

    /**
      Gets user that should get this message.
      @return user that should get this message
     */
    public String getUserTo() {
        return userTo;
    }

    /**
      Sets user that gets this message.
      @param userTo user that gets this message
     */
    public void setUserTo(String userTo) {
        this.userTo = userTo;
    }

    /**
      Gets text of the message.
      @return text of the message
     */
    public String getMessage() {
        return message;
    }

    /**
      Sets text of this message.
      @param message text of this message
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
      Returns full information about this message.
      @return full information about this message
     */
    @Override
    public String toString() {
        return "From: " + userFrom + " \nTo: " + userTo + "Message: " + getMessage();
    }

    /**
      Compares message sending times
      @param o the object to be compared.
      @return message that was sent earlier
     */
    @Override
    public int compareTo(MessageFromTo o) {
        return o.getTime().compareTo(this.getTime());
    }
}
