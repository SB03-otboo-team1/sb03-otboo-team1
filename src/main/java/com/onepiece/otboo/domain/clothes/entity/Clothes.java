package com.onepiece.otboo.domain.clothes.entity;

import com.onepiece.otboo.domain.user.entity.User;
import com.onepiece.otboo.global.base.BaseUpdatableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 의상 엔티티 클래스
 * 사용자가 등록한 의상 정보를 저장합니다.
 */
@Entity
@Table(name = "clothes")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Clothes extends BaseUpdatableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", columnDefinition = "uuid", nullable = false)
    private User owner;

    @Column(nullable = false, length = 250)
    private String name;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ClothesType type;

    @Column(name = "image_url", nullable = true)
    private String imageUrl;

}
