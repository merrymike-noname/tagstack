package org.kovalenko.tagstack.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.kovalenko.tagstack.dto.BookmarkRequestDto;
import org.kovalenko.tagstack.dto.BookmarkResponseDto;
import org.kovalenko.tagstack.dto.TagResponseDto;
import org.kovalenko.tagstack.entity.Bookmark;
import org.kovalenko.tagstack.entity.Tag;
import org.kovalenko.tagstack.repository.BookmarkRepository;
import org.kovalenko.tagstack.repository.TagRepository;
import org.kovalenko.tagstack.service.BookmarkService;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookmarkServiceImpl implements BookmarkService {
    private final BookmarkRepository bookmarkRepository;
    private final TagRepository tagRepository;

    @Override
    public List<BookmarkResponseDto> getAll(Integer userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId should not be null");
        }

        return bookmarkRepository.findAllByUserId(userId)
                .stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookmarkResponseDto> getByTitle(String title, Integer userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId should not be null");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title should not be null or blank");
        }

        return bookmarkRepository.findAllByTitleContainingIgnoreCaseAndUserId(title, userId)
                .stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public BookmarkResponseDto getById(Integer id, Integer userId) {
        if (id == null || userId == null) {
            throw new IllegalArgumentException("id and userId should not be null");
        }

        return bookmarkRepository.findByIdAndUserId(id, userId)
                .map(this::convertToResponseDto)
                .orElseThrow(() -> new IllegalArgumentException("bookmark not found with id: " + id));
    }

    @Override
    public List<BookmarkResponseDto> getUrlContains(String urlPart, Integer userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId should not be null");
        }
        if (urlPart == null || urlPart.isBlank()) {
            throw new IllegalArgumentException("urlPart should not be null or blank");
        }

        return bookmarkRepository.findAllByUrlContainingIgnoreCaseAndUserId(urlPart, userId)
                .stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookmarkResponseDto> getByTag(Integer tagId, Integer userId) {
        if (tagId == null || userId == null) {
            throw new IllegalArgumentException("tagId and userId should not be null");
        }

        tagRepository.findById(tagId)
                .filter(tag -> tag.getUserId().equals(userId))
                .orElseThrow(() -> new IllegalArgumentException("tag not found with id: " + tagId));

        return bookmarkRepository.findAllByTagIdAndUserId(tagId, userId)
                .stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BookmarkResponseDto create(BookmarkRequestDto dto, Integer userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId should not be null");
        }
        if (dto.getUrl() == null || dto.getUrl().isBlank()) {
            throw new IllegalArgumentException("url should not be null or blank");
        }

        Bookmark bookmark = convertToEntity(dto, userId);

        if (dto.getTagIds() != null && !dto.getTagIds().isEmpty()) {
            Set<Tag> tags = dto.getTagIds().stream()
                    .map(tagId -> tagRepository.findById(tagId)
                            .filter(tag -> tag.getUserId().equals(userId))
                            .orElseThrow(() -> new IllegalArgumentException("tag not found with id: " + tagId)))
                    .collect(Collectors.toSet());
            bookmark.setTags(tags);
        }

        Bookmark savedBookmark = bookmarkRepository.save(bookmark);
        return convertToResponseDto(savedBookmark);
    }

    @Override
    @Transactional
    public BookmarkResponseDto update(BookmarkRequestDto dto, Integer id, Integer userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId should not be null");
        }
        if (dto.getUrl() == null || dto.getUrl().isBlank()) {
            throw new IllegalArgumentException("url should not be null or blank");
        }

        Bookmark existingBookmark = bookmarkRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("bookmark not found with ID: " + dto.getUrl()));

        existingBookmark.setTitle(dto.getTitle());
        existingBookmark.setDescription(dto.getDescription());
        existingBookmark.setUrl(dto.getUrl());
        existingBookmark.setUpdatedAt(java.time.LocalDateTime.now());

        if (dto.getTagIds() != null && !dto.getTagIds().isEmpty()) {
            Set<Tag> tags = dto.getTagIds().stream()
                    .map(tagId -> tagRepository.findById(tagId)
                            .filter(tag -> tag.getUserId().equals(userId))
                            .orElseThrow(() -> new IllegalArgumentException("Tag not found with id: " + tagId)))
                    .collect(Collectors.toSet());
            existingBookmark.setTags(tags);
        }

        Bookmark updatedBookmark = bookmarkRepository.save(existingBookmark);
        return convertToResponseDto(updatedBookmark);
    }

    @Override
    @Transactional
    public void deleteById(Integer id, Integer userId) {
        if (id == null || userId == null) {
            throw new IllegalArgumentException("id and userId should not be null");
        }

        Bookmark bookmark = bookmarkRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("Bookmark not found with id: " + id));

        bookmarkRepository.delete(bookmark);
    }

    @Override
    @Transactional
    public void deleteByUrl(String url, Integer userId) {
        if (url == null || url.isBlank() || userId == null) {
            throw new IllegalArgumentException("url and userId should not be null");
        }

        bookmarkRepository.deleteByUrlAndUserId(url, userId);
    }

    private BookmarkResponseDto convertToResponseDto(Bookmark bookmark) {
        Set<TagResponseDto> tagDtos = bookmark.getTags().stream()
                .map(tag -> TagResponseDto.builder()
                        .name(tag.getName())
                        .build())
                .collect(Collectors.toSet());

        return BookmarkResponseDto.builder()
                .url(bookmark.getUrl())
                .title(bookmark.getTitle())
                .description(bookmark.getDescription())
                .createdAt(bookmark.getCreatedAt())
                .updatedAt(bookmark.getUpdatedAt())
                .tags(tagDtos)
                .build();
    }

    private Bookmark convertToEntity(BookmarkRequestDto dto, Integer userId) {
        return Bookmark.builder()
                .userId(userId)
                .url(dto.getUrl())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .tags(new HashSet<>())
                .build();
    }
}
