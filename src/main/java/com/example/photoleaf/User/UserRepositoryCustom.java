// UserRepositoryCustom.java
package com.example.photoleaf.User;

import com.example.photoleaf.Post.Post;

import java.util.List;

public interface UserRepositoryCustom {
    List<Post> findLikedPostsByUserId(Long id);
}