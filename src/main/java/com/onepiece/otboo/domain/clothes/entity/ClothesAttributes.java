package com.onepiece.otboo.domain.clothes.entity;

import com.onepiece.otboo.global.base.BaseUpdatableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "clothes_attributes")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ClothesAttributes extends BaseUpdatableEntity {

    @Column(nullable = false)
    private UUID clothesId;

    @Column(nullable = false)
    private UUID definitionId;

    @Column(nullable = false)
    private UUID optionId;

}
