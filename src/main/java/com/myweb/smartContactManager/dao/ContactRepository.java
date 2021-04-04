package com.myweb.smartContactManager.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.myweb.smartContactManager.entity.Contact;


public interface ContactRepository extends JpaRepository<Contact, Integer> {
	//this method will retrieve all the contact details
	@Query("from Contact as c where c.user.id =:userId")
	public List<Contact> findContactsByUser(@Param("userId") int userId);
	
	@Query("from Contact as c where c.user.id =:userId")
	//Page is the sublist of a list.it has been used for pagination purpose
	public Page<Contact> findContactsByUser(@Param("userId") int userId,Pageable pageable);
}
