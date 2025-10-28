package com.sitestart.blog.controllers;

import com.sitestart.blog.models.MessageFromTo;
import com.sitestart.blog.models.User;
import com.sitestart.blog.repo.UserRepository;
import jakarta.servlet.http.HttpSession;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 A controller that navigates between pages of a site and processes some operations on pages.
 */
@Controller
public class MessengerController {


    @Autowired
    private UserRepository userRepository;

    /**
      Returns sign in page
      @return "enter.html"
     */
    @GetMapping("/enter")
    public String enter() {
        return "enter";
    }

    /**
      Returns register page.
      @return "register.html"
     */
    @GetMapping("/register")
    public String register() {
        return "register";
    }


    /**
     This method allows you to register a user on the page.
     @param userName username
     @param firstName user's first name
     @param secondName user's last name
     @param password user's password
     @param image avatar
     @param model data container
     @return "enter.html" if register was successful
     @throws IOException an exception
     */
    @PostMapping("/register")
    public String register(@RequestParam String userName,
                              @RequestParam String firstName,
                              @RequestParam String secondName,
                              @RequestParam String password,
                              @RequestParam("image") MultipartFile image,
                              Model model) throws IOException {

        // Checking for empty fields
        if (userName.isEmpty() || firstName.isEmpty() || secondName.isEmpty() || password.isEmpty()) {
            model.addAttribute("error", "Please enter all data presented!");
            return "register";
        }

        // Checking an existing user
        if (userRepository.findByUserName(userName) != null) {
            model.addAttribute("error", "We already have a user with this username, make up something else!");
            return "register";
        }

        // Working with images
        Path uploadPath = Paths.get("uploads/");
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        String fileName = UUID.randomUUID() + "_" + image.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);
        Thumbnails.of(image.getInputStream())
                .size(1080, 1080) // уменьшение до 1080px (сохраняя пропорции)
                .outputQuality(0.8) // качество JPEG (0.0 - 1.0)
                .toFile(filePath.toFile());

        // Save the registered user
        User user = new User(userName, firstName, secondName, password);
        user.setImagePath("/uploads/" + fileName);
        userRepository.save(user);

        return "redirect:/enter";
    }

    /**
     Allows an existing user to log into their account
     @param userName username
     @param password user's password
     @param model data container
     @param session current session
     @return "user-profile.html" if sign in was successful or "enter.html" if not
     */
    @PostMapping("/enter")
    public String enter(@RequestParam String userName, @RequestParam String password, Model model,
                        HttpSession session) {
        User user = userRepository.findByUserName(userName);

        // Check if the user exists and if the password matches
        if (user != null && user.getPassword().equals(password)) {
            model.addAttribute("user", user);
            session.setAttribute("currentUserId", user.getId());
            return "user-profile";
        } else {
            model.addAttribute("error", "Not correct name or password!");
            return "enter";
        }
    }

    /**
      Shows user profile paage
      @param model data container
      @param session current session
      @return "user-profile.html"
     */
    @GetMapping("/user-profile")
    public String userProfile(Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("currentUserId");
        User user = userRepository.findById(userId).orElse(null);
        assert user != null;
        model.addAttribute("user", user);
        return "user-profile"; // Страница для отображения информации о пользователе
    }

    /**
     Shows all chats of the current user.
     @param model data container
     @param session current session
     @return "chat.html"
     */
    @GetMapping("/chat")
    public String chat(Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("currentUserId");
        User currentUser = userRepository.findById(userId).orElse(null);
        assert currentUser != null;
        model.addAttribute("user", currentUser);
        model.addAttribute("messages", currentUser.getMessengers());
        // Looking for all the user's interlocutors
        List<String> companions = currentUser.findAllCompanions(currentUser.getUserName());

        // Sorting received companions by the time of the last message with them
        Map<MessageFromTo, User> dialogueMap = new TreeMap<>();
        for (String companionName : companions) {
            User companion = userRepository.findByUserName(companionName);
            dialogueMap.put(currentUser.findLastMessageWith(companionName), companion);
        }

        model.addAttribute("companions", companions);
        model.addAttribute("dialogue", dialogueMap);

        return "chat";
    }

    /**
     This method allows you to work with a fragment of a page so that only
     the data in this fragment is reloaded once every 5 seconds.
     @param model data container
     @param session current session
     @return fragment of the "chat.html"
     */
    @GetMapping("/chat/fragment")
    public String getChatFragment(Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("currentUserId");
        User currentUser = userRepository.findById(userId).orElse(null);
        assert currentUser != null;
        model.addAttribute("user", currentUser);
        model.addAttribute("messages", currentUser.getMessengers());
        List<String> companions = currentUser.findAllCompanions(currentUser.getUserName());

        Map<MessageFromTo, User> dialogueMap = new TreeMap<>();
        for (String companionName : companions) {
            User companion = userRepository.findByUserName(companionName);
            dialogueMap.put(currentUser.findLastMessageWith(companionName), companion);
        }

        model.addAttribute("companions", companions);
        model.addAttribute("dialogue", dialogueMap);


        return "chat :: messagesFragment";
    }

    /**
     This method allows you to send a message from the chat page.
     @param userName username
     @param message text of the message
     @param model data container
     @param session current session
     @return "chat.html"
     */
    @PostMapping("/chat")
    public String chat(@RequestParam String userName, @RequestParam String message, Model model, HttpSession session) {
        User personWhoWeSendFor = userRepository.findByUserName(userName);
        Long userId = (Long) session.getAttribute("currentUserId");
        User currentUser = userRepository.findById(userId).orElse(null);
        // Check if the user we want to send a message to exists
        if (personWhoWeSendFor != null) {
            assert currentUser != null;
            model.addAttribute("user", currentUser);
            MessageFromTo messageFromTo = new MessageFromTo(currentUser.getUserName(), personWhoWeSendFor.getUserName(), message);


            // Set the formatted time of the message
            LocalDateTime currentDateTime = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss");
            String formattedDateTime = currentDateTime.format(formatter);
            DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("HH:mm");
            String formattedTime1 = currentDateTime.format(formatter1);
            messageFromTo.setExactTime(formattedTime1);
            messageFromTo.setTime(formattedDateTime);

            // Saving message information
            currentUser.getMessengers().add(messageFromTo);
            userRepository.save(currentUser);
            personWhoWeSendFor.getMessengers().add(messageFromTo);
            userRepository.save(personWhoWeSendFor);
            model.addAttribute("messages", currentUser.getMessengers());

            // Again, we sort messages by sending time
            List<String> companions = currentUser.findAllCompanions(currentUser.getUserName());
            Map<MessageFromTo, User> dialogueMap = new TreeMap<>();
            for (String companionName : companions) {
                User companion = userRepository.findByUserName(companionName);
                dialogueMap.put(currentUser.findLastMessageWith(companionName), companion);
            }

            model.addAttribute("companions", companions);
            model.addAttribute("dialogue", dialogueMap);


        }
        // If the required user is missing, we display an error
        else {
            model.addAttribute("error", "No user found with this username!");
        }
        return "chat";
    }

    /**
     This method allows you to show a dialog with a specific user.
     @param id user's ID
     @param model data container
     @param session current session
     @return "chat-details"
     */
    @GetMapping("/chat/{id}")
    public String chatDetails(@PathVariable(value = "id") long id, Model model, HttpSession session) {

        Long userId = (Long) session.getAttribute("currentUserId");
        User currentUser = userRepository.findById(userId).orElse(null);
        if (!userRepository.existsById(id)) {
            return "redirect:/chat";
        }
        Optional<User> user = userRepository.findById(id);
        ArrayList<User> res = new ArrayList<>();
        user.ifPresent(res::add);
        Map<User, List<MessageFromTo>> dialogueMap = new HashMap<>();
        assert currentUser != null;
        dialogueMap.put(res.get(0), currentUser.findDialogueWith(res.get(0).getUserName()));
        model.addAttribute("user", res);
        model.addAttribute("dialogue", dialogueMap);

        return "chat-details";
    }

    /**
     This method allows you to work with a fragment of a page so that only
     the data in this fragment is reloaded once every 5 seconds.
     @param id user's ID
     @param model data container
     @param session current session
     @return fragment of chat-details page
     */
    @GetMapping("/chat/{id}/messenger")
    public String getChatMessenger(@PathVariable(value = "id") long id, Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("currentUserId");
        User currentUser = userRepository.findById(userId).orElse(null);

        if (!userRepository.existsById(id)) {
            return "redirect:/chat";
        }
        Optional<User> user = userRepository.findById(id);
        ArrayList<User> res = new ArrayList<>();
        user.ifPresent(res::add);
        Map<User, List<MessageFromTo>> dialogueMap = new HashMap<>();
        dialogueMap.put(res.get(0), currentUser.findDialogueWith(res.get(0).getUserName()));
        // Allows you to work with this data in html page
        model.addAttribute("user", res);
        model.addAttribute("dialogue", dialogueMap);
        return "chat-details :: messenger"; // имя HTML-файла и фрагмента
    }

    /**
     This method allows you to send a message in a dialog with a specific user.
      @param id user's ID
      @param message text of the message
      @param model data container
      @param session current session
      @return "chat-details.html"
     */
    @PostMapping("/chat/{id}")
    public String chatDetails1(@PathVariable(value = "id") long id, @RequestParam String message, Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("currentUserId");
        User currentUser = userRepository.findById(userId).orElse(null);
        if (!userRepository.existsById(id)) {
            return "redirect:/chat-details";
        }
        Optional<User> user = userRepository.findById(id);
        ArrayList<User> res = new ArrayList<>();
        user.ifPresent(res::add);
        Map<User, List<MessageFromTo>> dialogueMap = new HashMap<>();
        dialogueMap.put(res.get(0), currentUser.findDialogueWith(res.get(0).getUserName()));
        MessageFromTo messageFromTo = new MessageFromTo(currentUser.getUserName(), res.get(0).getUserName(), message);

        // Set the formatted time of the message
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss");
        String formattedDateTime = currentDateTime.format(formatter);
        messageFromTo.setTime(formattedDateTime);
        DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("HH:mm");
        String formattedTime1 = currentDateTime.format(formatter2);
        messageFromTo.setExactTime(formattedTime1);

        // Saving message information
        currentUser.getMessengers().add(messageFromTo);
        userRepository.save(currentUser);
        res.get(0).getMessengers().add(messageFromTo);
        userRepository.save(res.get(0));
        model.addAttribute("messages", currentUser.getMessengers());

        model.addAttribute("user", res);
        model.addAttribute("dialogue", dialogueMap);

        return "redirect:/chat/" + id;
    }


}