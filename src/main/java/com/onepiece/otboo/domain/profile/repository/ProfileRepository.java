package com.onepiece.otboo.domain.profile.repository;

import com.onepiece.otboo.domain.profile.entity.Profile;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, UUID> {

}
