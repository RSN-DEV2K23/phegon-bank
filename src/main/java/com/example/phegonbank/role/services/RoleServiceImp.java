package com.example.phegonbank.role.services;

import java.util.List;

import javax.naming.NameNotFoundException;

import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.example.phegonbank.exceptions.NotFoundException;
import com.example.phegonbank.res.Response;
import com.example.phegonbank.role.entity.Role;
import com.example.phegonbank.role.repo.RoleRepo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoleServiceImp implements RoleService {
    
    private final RoleRepo roleRepo;
    
    @Override
    public Response<Role> createRole(Role roleRequest) {
        if(roleRepo.findByName(roleRequest.getName().isPresent()){
            // TODO Auto-generated method stub
        throw new BadRequestException("Role already exist");
        }
        Role saveRole = roleRepo.save(roleRequest);

        return Response.<Role>builder()
        .statusCode(HttpStatus.OK.value())
        .message("Role Save Successfully")
        .data(saveRole)
        .build();
    }

    @Override
    public Response<Role> updateRole(Role roleRequest) {
        Role role = roleRepo.findById(roleRequest.getId())
                    .orElseThrow(()-> new NameNotFoundException("Role not found"));

        role.setName(roleRequest.getName());
        Role updatedRole = roleRepo.save(role);
        return Response.<Role>builder()
                    .statusCode(HttpStatus.OK.value())
                    .message("Role updated successfully")
                    .data(updatedRole)
                    .build();
    }

    @Override
    public Response<List<Role>> getAllRolesResponse() {
        List<Role> roles = roleRepo.findAll();
        return Response.<List<Role>>builder()
                    .statusCode(HttpStatus.OK.value())
                    .message("Role updated successfully")
                    .data(roles)
                    .build();
    }

    @Override
    public Response<Role> deleteRole(Long id) {
        if(!roleRepo.existById(id)){
            throw new NotFoundException("Role Not Found")
        }
        roleRepo.deleteById(id);

        return Response.builder()
                    .statusCode(HttpStatus.OK.value())
                    .message("Role deleted successfuly")
                    .build();
    }



}
