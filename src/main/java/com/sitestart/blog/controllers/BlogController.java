package com.sitestart.blog.controllers;

import com.sitestart.blog.models.MessageFromTo;
import com.sitestart.blog.models.User;
import com.sitestart.blog.repo.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
public class BlogController {


    @Autowired
    private UserRepository userRepository;

    public BlogController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    @GetMapping("/enter")
    public String blogEnter(Model model) {
        return "enter";
    }

    @GetMapping("/register")
    public String blogRegister(Model model) {
        return "register";
    }

    @PostMapping("/register")
    public String blogPostAdd(@RequestParam String userName,
                              @RequestParam String firstName,
                              @RequestParam String secondName,
                              @RequestParam String password,
                              @RequestParam("image") MultipartFile image,
                              Model model) throws IOException  {
        String fileName = UUID.randomUUID() + "_" + image.getOriginalFilename();
        Path uploadPath = Paths.get("uploads/");
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        Files.copy(image.getInputStream(), uploadPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);

        User user = new User(userName, firstName, secondName, password);
        user.setImagePath("/uploads/" + fileName);
        userRepository.save(user);
        return "redirect:/enter";
    }

    @PostMapping("/enter")
    public String enter(@RequestParam String userName, @RequestParam String password, Model model,
                        HttpSession session) {
        User user = userRepository.findByUserName(userName);

        // Проверяем, существует ли пользователь и совпадает ли пароль
        if (user != null && user.getPassword().equals(password)) {
            model.addAttribute("user", user);
            session.setAttribute("currentUserId", user.getId());
            return "user-profile"; // Перенаправление на страницу с данными пользователя
        } else {
            model.addAttribute("error", "Неверное имя или пароль!");
            return "enter"; // Если ошибка, возвращаем на страницу входа
        }
    }

    @GetMapping("/user-profile")
    public String userProfile(Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("currentUserId");
        User user = userRepository.findById(userId).orElse(null);
        model.addAttribute("user", user);
        return "user-profile"; // Страница для отображения информации о пользователе
    }

    @GetMapping("/chat")
    public String chat(Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("currentUserId");
        User currentUser = userRepository.findById(userId).orElse(null);
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

        return "chat"; // Страница для отображения информации о пользователе
    }

    @GetMapping("/chat/fragment")
    public String getChatFragment(Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("currentUserId");
        User currentUser = userRepository.findById(userId).orElse(null);
        User user = currentUser;
        model.addAttribute("user", user);
        model.addAttribute("messages", user.getMessengers());
        List<String> companions = currentUser.findAllCompanions(currentUser.getUserName());

        Map<MessageFromTo, User> dialogueMap = new TreeMap<>();
        for (String companionName : companions) {
            User companion = userRepository.findByUserName(companionName);
            dialogueMap.put(currentUser.findLastMessageWith(companionName), companion);
        }

        model.addAttribute("companions", companions);
        model.addAttribute("dialogue", dialogueMap);


        return "chat :: messagesFragment"; // имя HTML-файла и фрагмента
    }

    @PostMapping("/chat")
    public String chat(@RequestParam String userName, @RequestParam String message, Model model, HttpSession session) {
        User personWhoWeSendFor = userRepository.findByUserName(userName);
        Long userId = (Long) session.getAttribute("currentUserId");
        User currentUser = userRepository.findById(userId).orElse(null);
        User sender = currentUser;
        // Проверяем, существует ли пользователь и совпадает ли пароль
        if (personWhoWeSendFor != null) {
            model.addAttribute("user", sender);
            MessageFromTo messageFromTo = new MessageFromTo(sender.getUserName(), personWhoWeSendFor.getUserName(), message);


            LocalDateTime currentDateTime = LocalDateTime.now();

            // Формат: год.месяц.день часы:минуты:секунды
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss");

            String formattedDateTime = currentDateTime.format(formatter);
            DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("HH:mm");
            String formattedTime1 = currentDateTime.format(formatter1);
            messageFromTo.setExactTime(formattedTime1);
            messageFromTo.setTime(formattedDateTime);


            sender.getMessengers().add(messageFromTo);
            userRepository.save(sender);
            personWhoWeSendFor.getMessengers().add(messageFromTo);
            userRepository.save(personWhoWeSendFor);
            model.addAttribute("messages", sender.getMessengers());

            List<String> companions = currentUser.findAllCompanions(currentUser.getUserName());
            Map<MessageFromTo, User> dialogueMap = new TreeMap<>();
            for (String companionName : companions) {
                User companion = userRepository.findByUserName(companionName);
                dialogueMap.put(currentUser.findLastMessageWith(companionName), companion);
            }

            model.addAttribute("companions", companions);
            model.addAttribute("dialogue", dialogueMap);


            return "chat"; // Перенаправление на страницу с данными пользователя
        } else {
            model.addAttribute("error", "Неверное имя или пароль!");
            return "chat"; // Если ошибка, возвращаем на страницу входа
        }
    }

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
        dialogueMap.put(res.get(0), currentUser.findDialogueWith(res.get(0).getUserName()));
        model.addAttribute("user", res);
        model.addAttribute("dialogue", dialogueMap);

        return "chat-details";
    }

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
        model.addAttribute("user", res);
        model.addAttribute("dialogue", dialogueMap);
        return "chat-details :: messenger"; // имя HTML-файла и фрагмента
    }

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

        LocalDateTime currentDateTime = LocalDateTime.now();

        // Формат: год.месяц.день часы:минуты:секунды
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss");

        DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

        String formattedDateTime1 = currentDateTime.format(formatter);

        String formattedDateTime = currentDateTime.format(formatter);
        messageFromTo.setTime(formattedDateTime);
        DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("HH:mm");
        String formattedTime1 = currentDateTime.format(formatter2);
        messageFromTo.setExactTime(formattedTime1);

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