package org.kovalenko.tagstack.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TagResponseDto {
    private String name;
    private List<TagResponseDto> children;
}
