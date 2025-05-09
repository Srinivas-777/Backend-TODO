package com.example.ToDo.ServiceInterface;

import com.example.ToDo.Exception.UserNotFoundException;
import com.example.ToDo.Model.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface UserService {
    public User findUserById(Long userId) throws UserNotFoundException;
    public User findUserProfileByJwt(String jwt) throws UserNotFoundException;
    public List<User> findAllUsers();
}
