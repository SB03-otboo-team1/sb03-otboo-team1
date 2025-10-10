package com.onepiece.otboo.infra.api.provider;

import com.onepiece.otboo.infra.api.dto.KmaItem;
import java.util.List;

public interface WeatherProvider {

    List<KmaItem> fetchLatestItems(double latitude, double longitude);
}
