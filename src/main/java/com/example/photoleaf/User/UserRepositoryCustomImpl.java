// UserRepositoryCustomImpl.java
package com.example.photoleaf.User;

import com.example.photoleaf.Post.Post;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;


import java.util.List;

public class UserRepositoryCustomImpl implements UserRepositoryCustom {

    private final EntityManager entityManager;

    public UserRepositoryCustomImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<Post> findLikedPostsByUserId(Long id) {
        String queryStr = "SELECT p FROM Post p WHERE :user MEMBER OF p.likes ORDER BY p.id DESC";
        User user = entityManager.find(User.class, id);
        TypedQuery<Post> query = entityManager.createQuery(queryStr, Post.class);
        query.setParameter("user", user);
        return query.getResultList();
    }
}