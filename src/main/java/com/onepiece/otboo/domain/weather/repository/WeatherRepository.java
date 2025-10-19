package com.onepiece.otboo.domain.weather.repository;

import com.onepiece.otboo.domain.weather.entity.Weather;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WeatherRepository extends JpaRepository<Weather, UUID>, WeatherRepositoryCustom {

}
