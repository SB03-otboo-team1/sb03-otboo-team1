package com.onepiece.otboo.domain.user.repository;

import com.onepiece.otboo.domain.user.entity.User;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {

}
