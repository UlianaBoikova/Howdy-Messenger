package com.sitestart.blog.models;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;


@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String userName, firstName, secondName, password;
    private String imagePath;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<MessageFromTo> messengers = new ArrayList<>();

    public User(String userName, String firstName, String secondName, String password) {
        this.userName = userName;
        this.firstName = firstName;
        this.secondName = secondName;
        this.password = password;
        messengers = new ArrayList<>();
    }

    public User() {}



    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }



    public Long getId() {
        return id;
    }

    public String getUserName() {
        return userName;
    }

    public String getSecondName() {
        return secondName;
    }
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getPassword() {
        return password;
    }

    public List<MessageFromTo> getMessengers() {
        return messengers;
    }

    public List<String> findAllCompanions(String currentUser) {
        List<String> companions = new ArrayList<>();
        for (MessageFromTo message : getMessengers()) {
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



    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setSecondName(String secondName) {
        this.secondName = secondName;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public ArrayList<MessageFromTo> findDialogueWith(String userName) {
        ArrayList<MessageFromTo> usersList = new ArrayList<>();
        for(MessageFromTo user: getMessengers()) {
            if (user.getUserFrom().equals(userName) || user.getUserTo().equals(userName)) usersList.add(user);
        }
        return usersList;
    }

    public MessageFromTo findLastMessageWith(String userName) {
        ArrayList<MessageFromTo> usersList = new ArrayList<>();
        for(MessageFromTo user: getMessengers()) {
            if (user.getUserFrom().equals(userName) || user.getUserTo().equals(userName)) usersList.add(user);
        }
        return usersList.get(usersList.size() - 1);
    }



    public void setMessengers(ArrayList<MessageFromTo> messengers) {
        this.messengers = messengers;
    }
}
