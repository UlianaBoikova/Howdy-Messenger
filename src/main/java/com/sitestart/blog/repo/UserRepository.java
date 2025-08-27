package com.sitestart.blog.repo;

import com.sitestart.blog.models.User;
import org.springframework.data.repository.CrudRepository;

/**
 This class provides methods (save or update user, find by ID, get all users, delete by ID)
 for working with the database from Users
 */
public interface UserRepository extends CrudRepository<User, Long> {
    /**
     Looks for user with needed username
      @param userName username of the person
      @return user
     */
    User findByUserName(String userName);
}
