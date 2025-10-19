package com.onepiece.otboo.domain.profile.repository;

import com.onepiece.otboo.domain.profile.entity.Profile;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, UUID> {

    Optional<Profile> findByUserId(UUID userId);

    List<Profile> findAllByLocationId(UUID locationId);
}
