package com.myweb.smartContactManager.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.myweb.smartContactManager.dao.ContactRepository;
import com.myweb.smartContactManager.dao.UserRepository;
import com.myweb.smartContactManager.entity.Contact;
import com.myweb.smartContactManager.entity.Users;
import com.myweb.smartContactManager.helper.Message;

@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ContactRepository contactRepository;
	
	//this is a generalised method which will gather the user attributes for response
	@ModelAttribute
	public void addCommonData(Model model,Principal principal) {
		String userName = principal.getName();
		System.out.println("username is "+userName);
		Users user = userRepository.getUserByUserName(userName);
		System.out.println(user);
		model.addAttribute("user",user);

		
	}
	
	@RequestMapping("/index")
	public String userDashboard(Model model, Principal principal) {
		
		String userName = principal.getName();
		System.out.println("username is "+userName);
		Users user = userRepository.getUserByUserName(userName);
		System.out.println(user);
		model.addAttribute("title","user dashboard");
		model.addAttribute("user",user);
		return "normal/user_dashboard";
	}

	//Add contact form handler
	@GetMapping("/add-contact")
	public String addContactForm(Model model, Principal principal) {
		model.addAttribute("title","addContact");
		model.addAttribute("contact", new Contact());
	return "normal/add_contact";
	}
	
	//processing add contact form "process-contact"
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute("contact") Contact contact,@RequestParam("profileImage") MultipartFile file,Principal principal,HttpSession session) {
		try{
		String name = principal.getName();
		
		Users user = this.userRepository.getUserByUserName(name);
		
		if(file.isEmpty()) {
			System.out.println("the file is empty");
			contact.setImage("contact.png");
		}
		else
		{
			contact.setImage(file.getOriginalFilename());
			File saveFile =  new ClassPathResource("static/img").getFile();
			Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
			Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			System.out.println("file uploaded successfully");
		}
		
		contact.setUser(user);//adding user to contact list
		user.getContacts().add(contact);//adding contact list to user
		this.userRepository.save(user);
		System.out.println("contact object data"+contact);
		session.setAttribute("message", new Message("Your file has been uploaded successfully", "success"));
		}catch (Exception e) {
			System.out.println("Error"+e.getMessage());
			session.setAttribute("message", new Message("Your file failed to upload", "danger"));
		}
		return "normal/add_contact";
	}
	
	//show contact handler with pagination
	@GetMapping("/view-contact/{page}")
	public String showContact(@PathVariable("page") int page,Model model,Principal principal) {
		Users user = this.userRepository.getUserByUserName(principal.getName());
		/*List<Contact> contactList = contactRepository.findContactsByUser(user.getId());// it will display the complete data
		model.addAttribute("contacts", contactList);
		*/
		Pageable pageable  = PageRequest.of(page, 1);
		Page<Contact> contacts = contactRepository.findContactsByUser(user.getId(), pageable);
		model.addAttribute("currentPage", page); 
		model.addAttribute("contacts", contacts);
		model.addAttribute("totalPages", contacts.getTotalPages());
		model.addAttribute("title", "view-contact");
		return "normal/view_contact";
	}
	
	
	//show contact detail on clicking on email
		@GetMapping("/{cId}/contact")
		public String showParticularContactDetail(@PathVariable("cId") int cId,Model model,Principal principal) {
			Optional<Contact> optionalContact=contactRepository.findById(cId);
			Contact contact= optionalContact.get();
			
			Users user = this.userRepository.getUserByUserName(principal.getName());
			if(contact.getUser().getId() == user.getId()) {
				model.addAttribute("contact", contact);
			}
			
			model.addAttribute("title", contact.getName()+"view-detail");
			return "normal/view_detail";
		}
		
		//delete contact detail on clicking on email
				@GetMapping("/delete/{cId}")
				public String deleteContactByCid(@PathVariable("cId") int cId,Model model,HttpSession session,Principal principal) {
					Contact contact=contactRepository.findById(cId).get();
										
					Users user = this.userRepository.getUserByUserName(principal.getName());
					user.getContacts().remove(contact);
					this.userRepository.save(user);
					if(contact.getUser().getId() == user.getId())
					{
						contact.setUser(null);
						if(cId > -1) {
						contactRepository.delete(contact);
						session.setAttribute("message", new Message("Contact deleted successfully","success"));
						
						}
						model.addAttribute("contact", contact);
						
					}
					
					model.addAttribute("title", contact.getName()+"view-detail");
					return "redirect:/user/view-contact/0";
				}
				
		//open update form controller
				@PostMapping("/update-contact/{cId}")
				public String updateForm(@PathVariable("cId") int cId,Model model,Principal principal) {
					Contact contact=contactRepository.findById(cId).get();
					
					Users user = this.userRepository.getUserByUserName(principal.getName());
					model.addAttribute("contact", contact);
					model.addAttribute("title", "Update-contact");
					return "normal/updateContact_form";
				}
				
				//process-update contact controller
				@PostMapping("/process-update")
				public String updateContact(@ModelAttribute("contact") Contact contact,@RequestParam("profileImage") MultipartFile file,Model model,Principal principal,HttpSession session) {
					
					Users user = this.userRepository.getUserByUserName(principal.getName());
					contact.setUser(user);
					Contact oldContact = this.contactRepository.getOne(contact.getcId());
					try {
						if(!file.isEmpty()) {
							//deleting old picture
							File oldFile =  new ClassPathResource("static/img").getFile();
							File file1 = new File(oldFile,oldContact.getImage());
							file1.delete();
							//updating new picture
							File saveFile =  new ClassPathResource("static/img").getFile();
							Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
							Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
							contact.setImage(file.getOriginalFilename());
						}
						else{
							contact.setImage(oldContact.getImage());
						}
						
						this.contactRepository.save(contact);
						session.setAttribute("message", new Message("Your contact has been updated successfully", "success"));
					} catch (Exception e) {
						
					}
					
					model.addAttribute("contact", contact);
					model.addAttribute("title", "Update-contact");
					return "redirect:/user/"+contact.getcId()+"/contact";
				}
				
				//user profile
				@GetMapping("/profile")
				public String yourProfile(Model model,Principal principal) {
					Users user = this.userRepository.getUserByUserName(principal.getName());
					model.addAttribute("user", user);
					model.addAttribute("title", "Profile page");
					return "normal/profile";
				}

}
