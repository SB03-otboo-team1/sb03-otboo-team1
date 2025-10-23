package com.onepiece.otboo.domain.follow.repository;

import com.onepiece.otboo.domain.follow.entity.Follow;
import com.onepiece.otboo.domain.user.entity.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FollowRepository extends JpaRepository<Follow, UUID>, FollowRepositoryCustom {

    boolean existsByFollowerAndFollowing(User follower, User following);

    @EntityGraph(attributePaths = {"follower.profile", "following.profile"})
    List<Follow> findByFollower(User follower);

    @EntityGraph(attributePaths = {"follower.profile", "following.profile"})
    List<Follow> findByFollowing(User following);

    void deleteByFollowerAndFollowing(User follower, User following);

    long countByFollowing(User following);

    long countByFollower(User follower);

    @EntityGraph(attributePaths = {"follower.profile", "following.profile"})
    Optional<Follow> findByFollowerAndFollowing(User follower, User following);

    @EntityGraph(attributePaths = {"follower.profile", "following.profile"})
    Optional<Follow> findById(UUID id);

    @EntityGraph(attributePaths = {"follower.profile", "following.profile"})
    List<Follow> findAllByFollowingId(UUID followingId);
}