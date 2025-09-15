package com.onepiece.otboo.domain.follow.repository;

import com.onepiece.otboo.domain.follow.entity.Follow;
import com.onepiece.otboo.domain.user.entity.User;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FollowRepository extends JpaRepository<Follow, UUID> {

    boolean existsByFollowerAndFollowing(User follower, User following);

    List<Follow> findByFollower(User follower);

    List<Follow> findByFollowing(User following);

    void deleteByFollowerAndFollowing(User follower, User following);

}