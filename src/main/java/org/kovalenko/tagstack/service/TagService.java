package org.kovalenko.tagstack.service;

import org.kovalenko.tagstack.dto.TagRequestDto;
import org.kovalenko.tagstack.dto.TagResponseDto;

import java.util.List;

public interface TagService {
    List<TagResponseDto> getAll(Integer userId);
    List<TagResponseDto> getByName(String name, Integer userId);
    TagResponseDto getById(Integer id);
    TagResponseDto create(TagRequestDto dto);
    TagResponseDto update(TagRequestDto dto, Integer id);
    void delete(Integer id);
}
