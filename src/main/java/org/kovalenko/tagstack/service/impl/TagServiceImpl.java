package org.kovalenko.tagstack.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.kovalenko.tagstack.dto.TagRequestDto;
import org.kovalenko.tagstack.dto.TagResponseDto;
import org.kovalenko.tagstack.entity.Tag;
import org.kovalenko.tagstack.repository.TagRepository;
import org.kovalenko.tagstack.service.TagService;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class TagServiceImpl implements TagService {

    private final TagRepository repository;

    @Override
    public List<TagResponseDto> getAll(Integer userId) {
        if (userId != null) {
            List<Tag> allByUserId = repository.findAllByUserId(userId);
            return allByUserId.stream().map(this::convertToResponseDto).toList();
        } else {
            // todo custom exception
            throw new IllegalArgumentException("userId should not be null");
        }
    }

    @Override
    public List<TagResponseDto> getByName(String name, Integer userId) {
        if (userId != null && name != null && !name.isBlank()) {
            return repository.findAllByNameAndUserId(name, userId)
                    .stream().map(this::convertToResponseDto).toList();
        } else {
            throw new IllegalArgumentException("userId or name should not be null");
        }
    }

    @Override
    public TagResponseDto getById(Integer id, Integer userId) {
        if (id != null && userId != null) {
            return repository.findByIdAndUserId(id, userId)
                    .map(this::convertToResponseDto)
                    .orElseThrow(() -> new IllegalArgumentException("tag with id " + id + " not found for user " + userId));
        } else {
            throw new IllegalArgumentException("id and userId should not be null");
        }
    }

    @Override
    @Transactional
    public TagResponseDto create(TagRequestDto dto, Integer userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId should not be null");
        }

        if (dto.getParentId() != null) {
            repository.findByIdAndUserId(dto.getParentId(), userId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "parent tag with id " + dto.getParentId() + " not found for user " + userId));
        }

        Tag tag = convertToEntity(dto, userId);
        return convertToResponseDto(repository.save(tag));
    }


    @Override
    @Transactional
    public TagResponseDto update(TagRequestDto dto, Integer id, Integer userId) {
        if (id == null || userId == null) {
            throw new IllegalArgumentException("id and userId should not be null");
        }

        Tag existingTag = repository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("tag with id " + id + " not found for user " + userId));

        existingTag.setName(dto.getName().toLowerCase());

        if (dto.getParentId() != null) {
            if (dto.getParentId().equals(id)) {
                throw new IllegalArgumentException("tag cannot be its own parent");
            }

            Tag parent = repository.findByIdAndUserId(dto.getParentId(), userId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "parent tag with id " + dto.getParentId() + " not found for user " + userId));

            existingTag.setParent(parent);
        } else {
            existingTag.setParent(null);
        }

        Tag updatedTag = repository.save(existingTag);
        return convertToResponseDto(updatedTag);
    }


    @Override
    @Transactional
    public void delete(Integer id, Integer userId) {
        if (id == null || userId == null) {
            throw new IllegalArgumentException("id and userId should not be null");
        }

        Tag tag = repository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("tag with id " + id + " not found for user " + userId));

        repository.delete(tag);
    }

    private TagResponseDto convertToResponseDto(Tag tag) {
        return TagResponseDto.builder()
                .name(tag.getName())
                .children(tag.getChildren().stream().map(this::convertToResponseDto).toList())
                .build();
    }

    private Tag convertToEntity(TagRequestDto dto, Integer userId) {
        return Tag.builder()
                .userId(userId)
                .name(dto.getName().toLowerCase())
                .parent(dto.getParentId() != null ? repository.findById(dto.getParentId()).orElse(null) : null)
                .build();
    }
}
