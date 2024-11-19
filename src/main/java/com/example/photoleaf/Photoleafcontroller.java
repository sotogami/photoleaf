package com.example.photoleaf;

import com.example.photoleaf.Post.Post;
import com.example.photoleaf.Post.PostRepository;
import com.example.photoleaf.User.User;
import com.example.photoleaf.User.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.data.domain.Sort;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Logger;


@Controller
public class Photoleafcontroller {
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public Photoleafcontroller(PostRepository postRepository, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/home")
    public ModelAndView home() {
        Logger logger = Logger.getLogger(getClass().getName());
        ModelAndView mav = new ModelAndView("photoleaf_main");

        // Get username of logged in user
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> currentUserOpt = userRepository.findByUsername(username);

        logger.info("Username: " + username);

        if (currentUserOpt.isPresent()) {
            User currentUser = currentUserOpt.get();
            List<Post> followingUsersPosts = new ArrayList<>();

            logger.info("User ID: " + currentUser.getId());
            // Add the current user ID to the mav
            mav.addObject("userId", currentUser.getId());

            // Get posts from all the users that the current user is following
            for (User followingUser : currentUser.getFollowing()) {
                List<Post> posts = postRepository.findByUser_IdOrderByIdDesc(followingUser.getId());

                for (Post post : posts) {
                    post.setUserId(post.getUserId());
                    User user = userRepository.findById(post.getUserId()).orElseThrow(() -> new UsernameNotFoundException("User not found"));
                    post.setNickname(user.getNickname());
                }

                followingUsersPosts.addAll(posts);
            }

            // Sort posts by date, most recent first
            Collections.sort(followingUsersPosts, Comparator.comparing(Post::getDate).reversed());

            mav.addObject("followingUsersPosts", followingUsersPosts);
        }
        mav.addObject("userRepository", userRepository);
        return mav;
    }
    private void setPostLikedAndDifference(Post post, String loggedInUsername) {
        List<User> likes = post.getLikes();
        boolean liked = likes.stream().anyMatch(user -> user.getUsername().equals(loggedInUsername));
        post.setLiked(liked);

        // Calculate difference between post date and current time
        long difference = Duration.between(post.getDate(), LocalDateTime.now()).toMinutes();

        // Set difference string accordingly
        if (difference < 60) {
            post.setDifference(difference + "분 전");
        } else {
            post.setDifference(difference / 60 + "시간 전");
        }
    }
    @GetMapping("/top-liked-posts")
    @ResponseBody
    public List<PostDetailedDto> getTopLikedPosts() {
        List<Post> posts = postRepository.findPostsOrderByLikesCountDesc();
        List<PostDetailedDto> postDetailedDtos = new ArrayList<>();

        for (Post post : posts) {
            PostDetailedDto postDetailedDto = new PostDetailedDto();
            User user = userRepository.findById(post.getUserId()).orElseThrow();

            postDetailedDto.id = post.getId();
            postDetailedDto.username = user.getUsername();
            postDetailedDto.nickname = post.getNickname();
            postDetailedDto.content = post.getContent();
            postDetailedDto.imageUrl = "/images/posts/" + post.getImage();
            postDetailedDto.liked = post.isLiked();
            postDetailedDto.likesCount = post.getLikesCount();

            if (post.getDate() != null) {
                postDetailedDto.date = post.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            }
            postDetailedDtos.add(postDetailedDto);
        }

        return postDetailedDtos;
    }

    public static class PostDetailedDto {
        public Long id;
        public String username;
        public String nickname;
        public String content;
        public String imageUrl;
        public boolean liked;
        public int likesCount;
        public String date;
    }
    @GetMapping("/Photoleaf_chatbox")
    public ModelAndView chatBox() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> currentUserOpt = userRepository.findByUsername(username);

        if (currentUserOpt.isPresent()) {
            ModelAndView mav = new ModelAndView("Photoleaf_chatbox");
            User currentUser = currentUserOpt.get();
            mav.addObject("userId", currentUser.getId());  // Add `userId` to the mav
            return mav;
        } else {
            // 유저가 존재하지 않는 경우 에러 페이지를 반환하거나 에러 상태 코드를 전송
            return new ModelAndView("error");
        }
    }

    @GetMapping("/register")
    public String register() {
        return "Register";
    }

    @GetMapping("/login")
    public String login() {
        return "login_page";
    }

}