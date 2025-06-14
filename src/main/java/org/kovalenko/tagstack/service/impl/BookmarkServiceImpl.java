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

import java.util.*;
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
//        Set<TagResponseDto> tagDtos = bookmark.getTags().stream()
//                .map(tag -> TagResponseDto.builder()
//                        .name(tag.getName())
//                        .build())
//                .collect(Collectors.toSet());

        Set<TagResponseDto> tagDtos = getHierarchicalTags(bookmark.getTags());

        return BookmarkResponseDto.builder()
                .url(bookmark.getUrl())
                .title(bookmark.getTitle())
                .description(bookmark.getDescription())
                .createdAt(bookmark.getCreatedAt())
                .updatedAt(bookmark.getUpdatedAt())
                .tags(tagDtos)
                .build();
    }

    private Set<TagResponseDto> getHierarchicalTags(Set<Tag> tags) {
        Set<TagResponseDto> result = new HashSet<>();
        Map<Integer, Tag> processedRoots = new HashMap<>();

        // For each tag directly associated with the bookmark
        for (Tag tag : tags) {
            // Find the root tag
            Tag rootTag = findRootTag(tag);

            // Skip if we've already processed this root
            if (processedRoots.containsKey(rootTag.getId())) {
                continue;
            }

            // Mark this root as processed
            processedRoots.put(rootTag.getId(), rootTag);

            // Build path from root to the bookmark's tag
            TagResponseDto rootDto = buildPathToTag(rootTag, tag.getId());
            result.add(rootDto);
        }

        return result;
    }

    /**
     * Finds the root tag by traversing up the parent chain
     */
    private Tag findRootTag(Tag tag) {
        Tag current = tag;
        while (current.getParent() != null) {
            current = current.getParent();
        }
        return current;
    }

    /**
     * Builds a path from the root tag down to the target tag
     * Only includes children that are on the path to the target tag
     */
    private TagResponseDto buildPathToTag(Tag currentTag, Integer targetTagId) {
        // Create DTO for current tag
        TagResponseDto dto = TagResponseDto.builder()
                .name(currentTag.getName())
                .children(new ArrayList<>())
                .build();

        // If this is the target tag, return without children
        if (currentTag.getId().equals(targetTagId)) {
            return dto;
        }

        // Find the child that leads to the target tag
        for (Tag child : currentTag.getChildren()) {
            // Check if the target is this child or a descendant of this child
            if (isTagOrDescendant(child, targetTagId)) {
                TagResponseDto childDto = buildPathToTag(child, targetTagId);
                dto.getChildren().add(childDto);
                break; // We only need the path to the target, not all children
            }
        }

        return dto;
    }

    /**
     * Checks if the given tag is the target tag or has the target tag as a descendant
     */
    private boolean isTagOrDescendant(Tag tag, Integer targetTagId) {
        if (tag.getId().equals(targetTagId)) {
            return true;
        }

        for (Tag child : tag.getChildren()) {
            if (isTagOrDescendant(child, targetTagId)) {
                return true;
            }
        }

        return false;
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
