package com.deolhoneles.dto;

import java.util.List;

public record FeedRequest(
        List<Long> deputyIds
) {
}
