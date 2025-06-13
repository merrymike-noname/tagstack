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
    public TagResponseDto getById(Integer id) {
        if (id != null) {
            return repository.findById(id).map(this::convertToResponseDto).orElse(null);
        } else {
            throw new IllegalArgumentException("id should not be null");
        }
    }

    @Override
    @Transactional
    public TagResponseDto create(TagRequestDto dto) {
        if (dto.getParentId() != null && repository.findById(dto.getParentId()).isEmpty()) {
            throw new IllegalArgumentException("parent tag with id " + dto.getParentId() + " not found");
        }
        Tag tag = convertToEntity(dto);
        return convertToResponseDto(repository.save(tag));
    }

    @Override
    public TagResponseDto update(TagRequestDto dto, Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("id should not be null");
        }

        Tag existingTag = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("tag with id " + id + " not found"));

        existingTag.setName(dto.getName());
        if (dto.getParentId() != null) {
            if (dto.getParentId().equals(id)) {
                throw new IllegalArgumentException("tag cannot be its own parent");
            }
            Tag parent = repository.findById(dto.getParentId())
                    .orElseThrow(() -> new IllegalArgumentException("parent tag with id " + dto.getParentId() + " not found"));
            if (!parent.getUserId().equals(existingTag.getUserId())) {
                throw new IllegalArgumentException("parent tag must belong to the same user");
            }

            existingTag.setParent(parent);
        } else {
            existingTag.setParent(null);
        }

        Tag updatedTag = repository.save(existingTag);
        return convertToResponseDto(updatedTag);
    }

    @Override
    public void delete(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("id should not be null");
        }
        repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("tag with id " + id + " not found"));

        repository.deleteById(id);
    }

    private TagResponseDto convertToResponseDto(Tag tag) {
        return TagResponseDto.builder()
                .name(tag.getName())
                .children(tag.getChildren().stream().map(this::convertToResponseDto).toList())
                .build();
    }

    private Tag convertToEntity(TagRequestDto dto) {
        return Tag.builder()
                .userId(dto.getUserId())
                .name(dto.getName().toLowerCase())
                .parent(dto.getParentId() != null ? repository.findById(dto.getParentId()).orElse(null) : null)
                .build();
    }
}
