package org.kovalenko.tagstack.repository;

import org.kovalenko.tagstack.entity.Bookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Integer> {
    List<Bookmark> findAllByUserId(Integer userId);
    List<Bookmark> findAllByTitleContainingIgnoreCaseAndUserId(String title, Integer userId);
    Optional<Bookmark> findByIdAndUserId(Integer id, Integer userId);
    List<Bookmark> findAllByUrlContainingIgnoreCaseAndUserId(String urlPart, Integer userId);
    void deleteByUrlAndUserId(String url, Integer userId);

    @Query("SELECT b FROM Bookmark b JOIN b.tags t WHERE t.id = :tagId AND b.userId = :userId")
    List<Bookmark> findAllByTagIdAndUserId(@Param("tagId") Integer tagId, @Param("userId") Integer userId);
}
