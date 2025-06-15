package org.kovalenko.tagstack.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TagRequestDto {
    private String name;
    private Integer parentId;
}
