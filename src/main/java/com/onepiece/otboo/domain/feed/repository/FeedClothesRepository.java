package com.onepiece.otboo.domain.feed.repository;

import com.onepiece.otboo.domain.feed.entity.FeedClothes;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FeedClothesRepository extends JpaRepository<FeedClothes, UUID> { }