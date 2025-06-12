package org.kovalenko.tagstack.dto;

import lombok.*;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookmarkRequestDto {
    private String url;
    private String title;
    private String description;

   private Set<Integer> tagIds;
}
