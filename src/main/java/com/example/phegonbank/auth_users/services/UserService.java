package com.example.phegonbank.auth_users.services;

import org.springframework.web.multipart.MultipartFile;

import com.example.phegonbank.auth_users.dtos.UpdatePasswordRequest;
import com.example.phegonbank.auth_users.dtos.UserDTO;
import com.example.phegonbank.auth_users.entity.User;
import com.example.phegonbank.res.Response;

public interface UserService {
    
    User getCurrentlLoggedInUser();

    Response<UserDTO> getMyProfile();
    
    Response<Page<UserDTO>> getAllUsers(int page, int size);
    
    Response<?> updatePassword(UpdatePasswordRequest updatePasswordRequest);
    
    Response<?> uploadProfilePicture(MultipartFile file);
}
