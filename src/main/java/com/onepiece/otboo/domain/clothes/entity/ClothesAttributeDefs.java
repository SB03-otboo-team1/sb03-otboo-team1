package com.onepiece.otboo.domain.clothes.entity;

import com.onepiece.otboo.global.base.BaseUpdatableEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "clothes_attribute_defs")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ClothesAttributeDefs extends BaseUpdatableEntity {

    @Column(nullable = false, length = 100, unique = true)
    private String name;

    @OneToMany(mappedBy = "definition", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ClothesAttributeOptions> options = new ArrayList<>();

    public void update(String newName) {
        if (newName != null && !newName.equals(this.name)) {
            this.name = newName;
        }
    }

    public void updateOptions(List<ClothesAttributeOptions> savedOptions) {
        this.options.addAll(savedOptions);
    }
}
