package org.kovalenko.tagstack.controller.rest;

import lombok.RequiredArgsConstructor;
import org.kovalenko.tagstack.dto.TagRequestDto;
import org.kovalenko.tagstack.dto.TagResponseDto;
import org.kovalenko.tagstack.service.TagService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v0/tags")
@RequiredArgsConstructor
public class TagController {
    private final TagService tagService;

    @GetMapping
    public ResponseEntity<List<TagResponseDto>> getAllTags(@RequestParam Integer userId) {
        return ResponseEntity.ok(tagService.getAll(userId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<TagResponseDto>> getTagsByName(
            @RequestParam String name,
            @RequestParam Integer userId) {
        return ResponseEntity.ok(tagService.getByName(name, userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TagResponseDto> getTagById(
            @PathVariable Integer id,
            @RequestParam Integer userId) {
        TagResponseDto tag = tagService.getById(id, userId);
        if (tag == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(tag);
    }

    @PostMapping
    public ResponseEntity<TagResponseDto> createTag(
            @RequestBody TagRequestDto tagRequest,
            @RequestParam Integer userId) {
        TagResponseDto createdTag = tagService.create(tagRequest, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTag);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TagResponseDto> updateTag(
            @PathVariable Integer id,
            @RequestBody TagRequestDto tagRequest,
            @RequestParam Integer userId) {
        try {
            TagResponseDto updatedTag = tagService.update(tagRequest, id, userId);
            return ResponseEntity.ok(updatedTag);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTag(
            @PathVariable Integer id,
            @RequestParam Integer userId) {
        try {
            tagService.delete(id, userId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
