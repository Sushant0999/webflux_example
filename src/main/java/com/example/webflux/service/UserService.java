package com.example.webflux.service;

import com.example.webflux.entity.User;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {

    public static ArrayList<User> userData = new ArrayList<>();


    public User addUser(User user){
        boolean added = userData.add(user);
        if(added){
            return user;
        }
        return null;
    }

    public User getUserByEmail(String email) {
        return userData.stream()
                .filter(user -> user.getEmail().equalsIgnoreCase(email))
                .findFirst()
                .orElse(null);
    }

    public List<User> getUserList(){
        User user = new User(null, null, null);
        if(!userData.contains(user)){
            userData.add(user);
        }
        return userData;
    }

    public boolean removeUser(String email){
        User user = getUserByEmail(email);
        return userData.remove(user);
    }

 }
