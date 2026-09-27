package com.example.jwtproject.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.jwtproject.entity.Role;

public interface RoleRepository extends JpaRepository<Role, Long>{
	   Optional<Role> findByName(String name);
}
