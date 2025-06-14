package org.kovalenko.tagstack.service;

import org.kovalenko.tagstack.dto.BookmarkRequestDto;
import org.kovalenko.tagstack.dto.BookmarkResponseDto;

import java.util.List;

public interface BookmarkService {
    List<BookmarkResponseDto> getAll(Integer userId);
    List<BookmarkResponseDto> getByTitle(String title, Integer userId);
    BookmarkResponseDto getById(Integer id, Integer userId);
    List<BookmarkResponseDto> getUrlContains(String urlPart, Integer userId);
    List<BookmarkResponseDto> getByTag(Integer tagId, Integer userId);
    BookmarkResponseDto create(BookmarkRequestDto dto, Integer userId);
    BookmarkResponseDto update(BookmarkRequestDto dto, Integer id, Integer userId);
    void deleteById(Integer id, Integer userId);
    void deleteByUrl(String url, Integer userId);
}
