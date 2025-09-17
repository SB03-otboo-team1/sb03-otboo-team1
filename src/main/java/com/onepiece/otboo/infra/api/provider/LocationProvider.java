package com.onepiece.otboo.infra.api.provider;

import com.onepiece.otboo.infra.api.dto.KakaoLocationItem;

public interface LocationProvider {

    KakaoLocationItem getLocation(double longitude, double latitude);
}
