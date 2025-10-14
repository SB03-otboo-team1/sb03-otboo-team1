package com.onepiece.otboo.domain.clothes.entity;

import com.onepiece.otboo.global.base.BaseUpdatableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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

    @ManyToOne
    @JoinColumn(name = "clothes_id", columnDefinition = "uuid", nullable = false)
    private Clothes clothes;

    @ManyToOne
    @JoinColumn(name = "definition_id", columnDefinition = "uuid", nullable = false)
    private ClothesAttributeDefs definition;

    @Column(name = "option_value", nullable = false, length = 50)
    private String optionValue;

}
