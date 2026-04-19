package com.aisleon.social.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "A single page of social feed posts, cursor-paginated")
public class FeedPageDto {

    private List<PostDto> posts;

    @Schema(
            nullable = true,
            description = "ISO-8601 timestamp. Pass this back as ?cursor= to get the next page. Null when hasMore is false.")
    private String nextCursor;

    private boolean hasMore;
}
