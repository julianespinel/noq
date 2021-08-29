package com.jespinel.noq.queues;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

class PageForTests<T> extends PageImpl<T> {

//    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
//    public PageForTests(@JsonProperty("content") List<T> content, @JsonProperty("number") int number, @JsonProperty("size") int size,
//                        @JsonProperty("totalElements") Long totalElements, @JsonProperty("pageable") JsonNode pageable, @JsonProperty("last") boolean last,
//                        @JsonProperty("totalPages") int totalPages, @JsonProperty("sort") JsonNode sort, @JsonProperty("first") boolean first,
//                        @JsonProperty("numberOfElements") int numberOfElements) {
//        super(content, PageRequest.of(number, size), totalElements);
//    }

    @JsonCreator
    public PageForTests(List<T> content, int number, int size,
                        Long totalElements, JsonNode pageable, boolean last,
                        int totalPages, JsonNode sort, boolean first,
                        int numberOfElements) {
        super(content, PageRequest.of(number, size), totalElements);
    }
}
