package org.kovalenko.tagstack.controller.rest;

import lombok.RequiredArgsConstructor;
import org.kovalenko.tagstack.dto.BookmarkRequestDto;
import org.kovalenko.tagstack.dto.BookmarkResponseDto;
import org.kovalenko.tagstack.service.BookmarkService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v0/bookmarks")
@RequiredArgsConstructor
public class BookmarkController {
    private final BookmarkService bookmarkService;

    @GetMapping
    public ResponseEntity<List<BookmarkResponseDto>> getAllBookmarks(@RequestParam Integer userId) {
        try {
            return ResponseEntity.ok(bookmarkService.getAll(userId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/search/title")
    public ResponseEntity<List<BookmarkResponseDto>> getBookmarksByTitle(
            @RequestParam String title,
            @RequestParam Integer userId) {
        try {
            return ResponseEntity.ok(bookmarkService.getByTitle(title, userId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookmarkResponseDto> getBookmarkById(
            @PathVariable Integer id,
            @RequestParam Integer userId) {
        try {
            return ResponseEntity.ok(bookmarkService.getById(id, userId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/search/url")
    public ResponseEntity<List<BookmarkResponseDto>> getBookmarksByUrlPart(
            @RequestParam String urlPart,
            @RequestParam Integer userId) {
        try {
            return ResponseEntity.ok(bookmarkService.getUrlContains(urlPart, userId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/search/tag/{tagId}")
    public ResponseEntity<List<BookmarkResponseDto>> getBookmarksByTag(
            @PathVariable Integer tagId,
            @RequestParam Integer userId) {
        try {
            return ResponseEntity.ok(bookmarkService.getByTag(tagId, userId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping
    public ResponseEntity<BookmarkResponseDto> createBookmark(
            @RequestBody BookmarkRequestDto bookmarkRequest,
            @RequestParam Integer userId) {
        try {
            BookmarkResponseDto createdBookmark = bookmarkService.create(bookmarkRequest, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdBookmark);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookmarkResponseDto> updateBookmark(
            @PathVariable Integer id,
            @RequestBody BookmarkRequestDto bookmarkRequest,
            @RequestParam Integer userId) {
        try {
            BookmarkResponseDto updatedBookmark = bookmarkService.update(bookmarkRequest, id, userId);
            return ResponseEntity.ok(updatedBookmark);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBookmarkById(
            @PathVariable Integer id,
            @RequestParam Integer userId) {
        try {
            bookmarkService.deleteById(id, userId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteBookmarkByUrl(
            @RequestParam String url,
            @RequestParam Integer userId) {
        try {
            bookmarkService.deleteByUrl(url, userId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}