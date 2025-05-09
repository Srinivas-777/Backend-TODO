package com.example.ToDo.ServiceImplementation;

import com.example.ToDo.Exception.UserNotFoundException;
import com.example.ToDo.JWT_PACKAGE.JwtTokenProvider;
import com.example.ToDo.Model.User;
import com.example.ToDo.Repository.UserRepository;
import com.example.ToDo.ServiceInterface.UserService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImplementation implements UserService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public UserServiceImplementation(UserRepository userRepository,JwtTokenProvider jwtTokenProvider) {
        this.userRepository=userRepository;
        this.jwtTokenProvider=jwtTokenProvider;
    }

    @Override
    public User findUserById(Long userId) throws UserNotFoundException {
        Optional<User> user=userRepository.findById(userId);
        if(user.isEmpty()) throw new UserNotFoundException("user not found with id "+userId);
        return user.get();
    }

    @Override
    public User findUserProfileByJwt(String jwt) throws UserNotFoundException {
        String email=jwtTokenProvider.getEmailFromJwtToken(jwt);
        if(!userRepository.existsByEmail(email))  throw new UserNotFoundException("user not found with email "+email);
        return userRepository.findByEmail(email).get();
    }

    @Override
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

}

