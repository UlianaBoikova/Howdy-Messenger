package com.sitestart.blog.models;

import jakarta.persistence.*;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.List;

/**
 This class stores information about the user: name, last name, login, password, avatar and messages
 The @Entity annotation creates a table in the database for this entity if it does not already exist,
 and corresponding columns for the class fields.
 */
@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;


    private String userName;
    private String firstName, secondName, password;
    private String imagePath;

    // @OneToMany - one user can have many messages
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MessageFromTo> messengers;

    /**
     Constructor for User class.
     * @param userName username of the person
     * @param firstName first name of the person
     * @param secondName last name of the person
     * @param password user's password
     */
    public User(String userName, String firstName, String secondName, String password) {
        this.userName = userName;
        this.firstName = firstName;
        this.secondName = secondName;
        this.password = password;
        messengers = new ArrayList<>();
    }

    /**
     Default User constructor
     */
    public User() {}


    /**
      Gets path to the image
      @return path to the image
     */
    public String getImagePath() {
        return imagePath;
    }

    /**
      Sets path to the image
      @param imagePath path to the image
     */
    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    /**
      Gets user's ID
      @return ID
     */
    public Long getId() {
        return id;
    }

    /**
      Gets username
      @return username
     */
    public String getUserName() {
        return userName;
    }

    /**
      Gets user's last name
      @return secondName
     */
    public String getSecondName() {
        return secondName;
    }

    /**
      Gets user's first name
      @return firstName
     */
    public String getFirstName() {
        return firstName;
    }

    /**
      Sets user's first name
      @param firstName user's first name
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
      Gets user's password
      @return password
     */
    public String getPassword() {
        return password;
    }

    /**
      Gets all user's messages
      @return list of messages
     */
    public List<MessageFromTo> getMessengers() {
        return messengers;
    }

    /**
      Searches for all the people you've already chatted with
      @param currentUser current user
      @return all your companion
     */
    public List<String> findAllCompanions(String currentUser) {
        List<String> companions = new ArrayList<>();
        for (MessageFromTo message: getMessengers()) {
            if (message.getUserFrom().equals(currentUser)) {
                if (!companions.contains(message.getUserTo())) {
                    companions.add(message.getUserTo());
                }
            } else if (message.getUserTo().equals(currentUser)) {
                if (!companions.contains(message.getUserFrom())) {
                    companions.add(message.getUserFrom());
                }
            }
        }
        return companions;
    }


    /**
      Sets username
      @param userName username
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
      Sets last name of the user
      @param secondName last name of the user
     */
    public void setSecondName(String secondName) {
        this.secondName = secondName;
    }

    /**
      Sets user's password
      @param password user's password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
      Searches for dialogue with exact person
      @param userName username of this person
      @return list of messages
     */
    public ArrayList<MessageFromTo> findDialogueWith(String userName) {
        ArrayList<MessageFromTo> usersList = new ArrayList<>();
        for(MessageFromTo user: getMessengers()) {
            if (user.getUserFrom().equals(userName) || user.getUserTo().equals(userName)) usersList.add(user);
        }
        return usersList;
    }

    /**
      Searches for the last message with exact person
      @param userName username of this person
      @return last message you sent or you got from this person
     */
    public MessageFromTo findLastMessageWith(String userName) {
        ArrayList<MessageFromTo> usersList = new ArrayList<>();
        for(MessageFromTo user: getMessengers()) {
            if (user.getUserFrom().equals(userName) || user.getUserTo().equals(userName)) usersList.add(user);
        }
        return usersList.get(usersList.size() - 1);
    }


    /**
      Sets list of messages
      @param messengers list of messages
     */
    public void setMessengers(ArrayList<MessageFromTo> messengers) {
        this.messengers = messengers;
    }
}
