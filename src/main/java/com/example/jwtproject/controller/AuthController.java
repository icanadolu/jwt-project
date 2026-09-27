package com.example.jwtproject.controller;

import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.jwtproject.dto.request.LoginRequest;
import com.example.jwtproject.dto.request.RegisterRequest;
import com.example.jwtproject.dto.response.AuthResponse;
import com.example.jwtproject.entity.Role;
import com.example.jwtproject.entity.User;
import com.example.jwtproject.repository.RoleRepository;
import com.example.jwtproject.repository.UserRepository;
import com.example.jwtproject.service.JwtService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
	private final AuthenticationManager authenticationManager;
	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	
	
	@PostMapping("/register")
	public ResponseEntity<AuthResponse>register(@RequestBody  RegisterRequest request){
		
		if(userRepository.existsByUsername(request.getUsername())) {
			return ResponseEntity.badRequest().build();
		}
		
		Role userRole = roleRepository.findByName("ROLE_USER")
				.orElseGet(() -> roleRepository.save(new Role("ROLE_USER")));
		
		User user = User.builder().username(request.getUsername())
				.password(passwordEncoder.encode(request.getPassword()))
				.roles(Set.of(userRole) )
				.build();
		
		userRepository.save(user);
		
		String token = jwtService.generateToken(user);
		
		return ResponseEntity.ok(new  AuthResponse(token));
	}
	
	@PostMapping("/login")
	public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request){
		authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(),request.getPassword()));
		
		User user = userRepository.findByUsername(request.getUsername()).orElseThrow();
		
		String token =  jwtService.generateToken(user);
		
		return ResponseEntity.ok(new AuthResponse(token));
	}
	

}
