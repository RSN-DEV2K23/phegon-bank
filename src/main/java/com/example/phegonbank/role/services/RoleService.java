package com.example.phegonbank.role.services;

import java.util.List;

import com.example.phegonbank.res.Response;
import com.example.phegonbank.role.entity.Role;

public interface RoleService {

        Response<Role> createRole(Role roleRequest);
        Response<Role> updateRole(Role roleRequest);
        Response<List<Role>> getAllRolesResponse();
        Response<Role> deleteRole(Long id);
}
