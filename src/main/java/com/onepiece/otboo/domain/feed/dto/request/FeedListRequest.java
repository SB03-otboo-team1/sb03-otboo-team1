//package com.onepiece.otboo.domain.feed.dto.request;
//
//
//
//import com.onepiece.otboo.domain.feed.enums.FeedSortBy;
//import com.onepiece.otboo.global.enums.SortDirection;
//
//import java.util.UUID;
//
//public record FeedListRequest(
//    String cursor,
//    UUID idAfter,
//    int limit,
//    FeedSortBy sortBy,
//    SortDirection sortDirection,
//    String keywordLike,
//    String skyStatusEqual,
//    String precipitationTypeEqual,
//    UUID authorIdEqual
//) { }