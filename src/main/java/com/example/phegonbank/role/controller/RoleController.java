package com.example.phegonbank.role.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.phegonbank.res.Response;
import com.example.phegonbank.role.services.RoleService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@PreAuthorize("hasAuthrority('ADMIN'))")

public class RoleController {

        private final RoleService roleService;

        @PostMapping
        public ResponseEntity<Response<Role>>createRole(@RequestBody Role roleRequest ){
            return ResponseEntity.ok(roleService.createRole(roleRequest));
        }

        @PutMapping
        public ResponseEntity<Response<Role>>updateRole(@RequestBody Role roleRequest ){
            return ResponseEntity.ok(roleService.updateRole(roleRequest));
        }

        @GetMapping
        public ResponseEntity<Response<List<Role>>> getAllRoles(){
            return ResponseEntity.ok(roleService.getAllRoles());
        }

        @DeleteMapping
        public ResponseEntity<Response<Role>> deleteRol(@PathVariable Long id){
            return ResponseEntity.ok(roleService.deleteRole(id));
        }
}
