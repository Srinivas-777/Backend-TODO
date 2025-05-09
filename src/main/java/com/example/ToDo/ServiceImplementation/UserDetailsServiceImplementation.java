package com.example.ToDo.ServiceImplementation;

import com.example.ToDo.Exception.UserNotFoundException;
import com.example.ToDo.Model.User;
import com.example.ToDo.Repository.UserRepository;
import com.example.ToDo.ServiceInterface.UserDetailsService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserDetailsServiceImplementation implements UserDetailsService {
    private final UserRepository userRepository;

    public UserDetailsServiceImplementation(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UserNotFoundException {

        if(!userRepository.existsByEmail(username))  throw new UsernameNotFoundException("user not found with email "+username);

        User user = userRepository.findByEmail(username).get();

        List<GrantedAuthority> authorities = new ArrayList<>();

        return new org.springframework.security.core.userdetails.User(user.getEmail(),user.getPassword(),authorities);
    }
}
