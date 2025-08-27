package com.sitestart.blog.repo;


import com.sitestart.blog.models.MessageFromTo;
import org.springframework.data.repository.CrudRepository;

/**
 This class provides methods (save or update user, find by ID, get all users, delete by ID)
 for working with the database from MessageFromTo
 */
public interface MessageFromToRepository extends CrudRepository<MessageFromTo, Long> {
}
