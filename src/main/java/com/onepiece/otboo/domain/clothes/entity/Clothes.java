package com.onepiece.otboo.domain.clothes.entity;

import com.onepiece.otboo.global.base.BaseUpdatableEntity;
import jakarta.persistence.*;
import java.util.UUID;
import lombok.*;

/**
 * 의상 엔티티 클래스
 * 사용자가 등록한 의상 정보를 저장합니다.
 */
@Entity
@Table(name = "clothes")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Clothes extends BaseUpdatableEntity {

  @Column(nullable = false)
  private UUID ownerId;

  @Column(nullable = false, length = 250)
  private String name;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private ClothesType type;

  @Column(name = "image_url", nullable = true)
  private String imageUrl;

}
