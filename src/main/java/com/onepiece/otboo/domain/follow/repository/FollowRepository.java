package com.onepiece.otboo.domain.follow.repository;

import com.onepiece.otboo.domain.follow.dto.response.FollowingResponse;
import com.onepiece.otboo.domain.follow.entity.Follow;
import com.onepiece.otboo.domain.user.entity.User;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FollowRepository extends JpaRepository<Follow, UUID> {

    boolean existsByFollowerAndFollowing(User follower, User following);

    List<Follow> findByFollower(User follower);

    List<Follow> findByFollowing(User following);

    void deleteByFollowerAndFollowing(User follower, User following);

    long countByFollowing(User following);

    long countByFollower(User follower);

    @Query("SELECT new com.onepiece.otboo.domain.follow.dto.response.FollowingResponse(" +
        "f.id, u.id, p.nickname, p.profileImageUrl, f.createdAt) " +
        "FROM Follow f " +
        "JOIN f.following u " +
        "JOIN Profile p ON p.user = u " +
        "WHERE f.follower = :user")
    List<FollowingResponse> findFollowingsWithProfile(@Param("user") User user);

}