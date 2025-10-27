package com.onepiece.otboo.infra.security.dto.data;

import org.springframework.http.HttpMethod;

public record Endpoint(HttpMethod method, String pattern) {

}
