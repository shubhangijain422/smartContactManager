package com.myweb.smartContactManager.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.myweb.smartContactManager.config.CustomUserDetails;
import com.myweb.smartContactManager.dao.UserRepository;
import com.myweb.smartContactManager.entity.Users;

public class UserDetailServiceImpl implements UserDetailsService {

	@Autowired
	UserRepository userRepository;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		// TODO Auto-generated method stub
		Users user = userRepository.getUserByUserName(username);
		if(user == null)
			throw new UsernameNotFoundException("Could not find user");
		CustomUserDetails customUserDetails = new CustomUserDetails(user);
		return customUserDetails;
	}

}
