package org.kovalenko.tagstack.service;

import org.kovalenko.tagstack.dto.TagRequestDto;
import org.kovalenko.tagstack.dto.TagResponseDto;

import java.util.List;

public interface TagService {
    public List<TagResponseDto> getAll(Integer userId);
    public List<TagResponseDto> getByName(String name, Integer userId);
    public TagResponseDto getById(Integer id);
    public TagResponseDto create(TagRequestDto dto);
    public TagResponseDto update(TagRequestDto dto, Integer id);
    public void delete(Integer id);
}
