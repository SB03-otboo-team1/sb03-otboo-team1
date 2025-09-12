package com.onepiece.otboo.domain.clothes.entity;

import com.onepiece.otboo.global.base.BaseUpdatableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "clothes")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Clothes extends BaseUpdatableEntity {

  @Column(nullable = false, length = 250)
  private String name;

  @Column(nullable = false)
  private ClothesType type;

  @Column(name = "image_url", nullable = true)
  private String imageUrl;

}
