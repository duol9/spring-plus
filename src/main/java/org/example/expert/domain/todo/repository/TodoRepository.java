package org.example.expert.domain.todo.repository;

import org.example.expert.domain.todo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface TodoRepository extends JpaRepository<Todo, Long>, TodoRepositoryCustom {

    @Query("SELECT t " +
        "FROM Todo t " +
        "LEFT JOIN FETCH t.user u " +
        "WHERE (:weather IS NULL OR t.weather = :weather) " +
        "AND (:startDay IS NULL  OR t.modifiedAt >= :startDay) " +
        "AND (:endDay IS NULL  OR t.modifiedAt <= :endDay) " +
        "ORDER BY t.modifiedAt DESC")
    Page<Todo> findAllByConditions(
        @Param("weather") String weather,
        @Param("startDay") LocalDateTime startDay,
        @Param("endDay") LocalDateTime endDay,
        Pageable pageable);
}
