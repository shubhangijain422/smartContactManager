package com.myweb.smartContactManager.controller;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.myweb.smartContactManager.dao.UserRepository;
import com.myweb.smartContactManager.entity.Users;
import com.myweb.smartContactManager.helper.Message;
import com.sun.net.httpserver.Authenticator.Result;

@Controller
public class HomeController {
	
	@Autowired
	UserRepository userRepo;
	
	@Autowired
	BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@GetMapping("/")
	//@ResponseBody
	public String home(Model model) {
		model.addAttribute("Title","home");
		return "normal/user_dashboard";
	}
	
	
	
	@GetMapping("/about")
	//@ResponseBody
	public String about(Model model) {
		model.addAttribute("Title","about ");

		return "about";
	}
	
	@GetMapping("/signin")
	public String customLogin(Model model) {
		model.addAttribute("Title","login");

		return "login";
	}
	
	@GetMapping("/signup")
	//@ResponseBody
	public String signup(Model model) {
		model.addAttribute("Title","signup ");
		model.addAttribute("user", new Users());
		return "signup";
	}
	
	@PostMapping("/do_register")
	public String registerUser(@Valid @ModelAttribute("user") Users user,@RequestParam(value="enabled",defaultValue = "false") boolean enabled,Model model,BindingResult result1,HttpSession session) 
	{
		try {
			if(!enabled)
			{
				System.out.println("you have not agreed terms and conditions");
				throw new Exception("you have not agreed terms and conditions");
			}
			if(result1.hasErrors()) {
				System.out.println("ERROR"+result1.toString());
				model.addAttribute("user", user);
				return "signup";
			}
			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
			model.addAttribute("Title","signup ");
			
			System.out.println("User"+user);
			System.out.println("Agreement"+enabled);
			Users result = this.userRepo.save(user);
			model.addAttribute("user", new Users());//it will push the submitted data again to UI
			System.out.println("the result is"+result);
			session.setAttribute("message", new Message("Registered successfully", "alert-success"));
		} catch (Exception e) {
			model.addAttribute("user", user);
			session.setAttribute("message", new Message("Something went wrong!!"+e.getMessage(), "alert-danger"));
		}
		return "signup";
	}
	
	

	
}
