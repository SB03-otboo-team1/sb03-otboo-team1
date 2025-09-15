package com.onepiece.otboo.domain.feed.repository;

import com.onepiece.otboo.domain.feed.entity.Feed;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FeedRepository extends JpaRepository<Feed, UUID> { }