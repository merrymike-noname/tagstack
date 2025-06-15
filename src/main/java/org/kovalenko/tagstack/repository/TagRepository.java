package org.kovalenko.tagstack.repository;

import org.kovalenko.tagstack.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Integer> {
    List<Tag> findAllByUserId(Integer userId);
    List<Tag> findAllByNameAndUserId(String name, Integer userId);
    Optional<Tag> findByIdAndUserId(Integer id, Integer userId);
}
