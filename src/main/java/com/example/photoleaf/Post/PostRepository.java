package com.example.photoleaf.Post;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByNickname(String nickname);
    @Query("SELECT p FROM Post p WHERE p.userId = :user_id")
    List<Post> findByUser_Id(@Param("user_id") Long user_id);
    List<Post> findByUser_IdOrderByIdDesc(Long user_id);
    @Query("SELECT p FROM Post p ORDER BY p.id DESC")
    List<Post> findPostsByPage(Pageable pageable);
    @EntityGraph(attributePaths = {"user"})
    List<Post> findAll();
    @Query("SELECT p FROM Post p ORDER BY SIZE(p.likes) DESC")
    List<Post> findPostsOrderByLikesCountDesc();
}