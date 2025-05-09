package com.example.ToDo.ServiceInterface;

import com.example.ToDo.Exception.UserNotFoundException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public interface UserDetailsService {
    UserDetails loadUserByUsername(String username) throws UserNotFoundException;
}
