package com.example.photoleaf.Post;

import com.example.photoleaf.User.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private LocalDateTime date;

    private String image; // You can change it to your imageUrl string manipulator


    private String difference;
    // getters
    public Long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public String getImage() {
        return image;
    }


    @Column(name = "nickname")
    private String nickname;


    public int getLikesCount() {
        return likes.size();
    }

    @JsonIgnore
    @ManyToMany
    private List<User> likes = new ArrayList<>();

    @Transient
    private boolean liked;

    public String getDifference() {
        return this.difference;
    }

    @Column(name="user_id", insertable = false, updatable = false)
    private Long userId;

    // setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public void setImage(String image) {
        this.image = image;
    }


    public void setDifference(String difference) {
        this.difference = difference;
    }

    // Required for the JPA entity
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Post )) return false;
        return id != null && id.equals(((Post) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Post{" +
                "id=" + id +
                ", content='" + content + '\'' +
                ", date=" + date +
                ", image='" + image + '\'' +
                ", nickname='" + nickname + '\'' +
                '}';
    }


    public List<User> getLikes() {
        return likes;
    }

    public void setLikes(List<User> likes) {
        this.likes = likes;
    }

    public boolean isLiked() {
        return liked;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getNickname() {
        return nickname;
    }
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    @ManyToOne
    @JoinColumn(name="user_id", nullable=false)
    private User user;
    // Other fields, getters, and setters...
    public User getUser() {
        return this.user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}