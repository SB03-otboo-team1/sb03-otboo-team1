package com.onepiece.otboo.domain.profile.repository;

import com.onepiece.otboo.domain.profile.entity.Profile;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileRepository extends JpaRepository<Profile, UUID> {

}
