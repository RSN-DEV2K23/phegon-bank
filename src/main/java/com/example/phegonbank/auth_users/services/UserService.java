package com.example.phegonbank.auth_users.services;

import com.example.phegonbank.auth_users.dtos.UpdatePasswordRequest;
import com.example.phegonbank.auth_users.dtos.UserDTO;
import com.example.phegonbank.auth_users.entity.User;
import com.example.phegonbank.res.Response;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {

    User getCurrentLoggedInUser();

    Response<UserDTO> getMyProfile();

    Response<Page<UserDTO>> getAllUsers(int page, int size);

    Response<?> updatePassword(UpdatePasswordRequest updatePasswordRequest);

    Response<?> uploadProfilePictureToS3(MultipartFile file);
}
