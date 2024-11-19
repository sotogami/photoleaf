package com.example.photoleaf.Post;


import com.example.photoleaf.User.User;
import com.example.photoleaf.User.UserRepository;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Files;
import java.io.IOException;
import java.nio.file.Paths;
import java.security.Principal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Controller
public class PostController {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public PostController(PostRepository postRepository, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/profile/{username}")
    public ModelAndView getProfile(@PathVariable String username) {
        ModelAndView mav = new ModelAndView("profile");

        // Get current authenticated User
        String loggedInUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        // Get posts by username
        Optional<User> userOpt = userRepository.findByUsername(username);
        List<Post> posts = new ArrayList<>();
        List<Post> likedPosts = new ArrayList<>();
        if(userOpt.isPresent()) {
            User user = userOpt.get();
            posts = postRepository.findByUser_Id(user.getId());  // Retrieve posts by user id

            for(Post post : posts) {
                Optional<User> postAuthor = userRepository.findById(post.getUserId());
                if (postAuthor.isPresent()) {
                    String authorNickname = postAuthor.get().getNickname();
                    post.setNickname(authorNickname);
                }
            }
            likedPosts = userRepository.findLikedPostsByUserId(user.getId());
            for(Post likedPost : likedPosts) {
                Optional<User> postAuthor = userRepository.findById(likedPost.getUserId());
                if (postAuthor.isPresent()) {
                    String authorNickname = postAuthor.get().getNickname();
                    likedPost.setNickname(authorNickname);
                }
            }
            // Add the nickname to model
            mav.addObject("nickname", user.getNickname());
            int followerCount = user.getFollowers().size();
            int followingCount = user.getFollowing().size();
            mav.addObject("followerCount", followerCount);
            mav.addObject("followingCount", followingCount);
        }

        Collections.sort(posts, Comparator.comparing(Post::getId).reversed());
        Collections.sort(likedPosts, Comparator.comparing(Post::getId).reversed());

        for(Post post : posts) {
            setPostLikedAndDifference(post, loggedInUsername);
        }
        for(Post liked_post : likedPosts) {
            setPostLikedAndDifference(liked_post, loggedInUsername);
        }

        // add new Post object for form
        Post postForm = new Post();
        mav.addObject("loggedInUsername", loggedInUsername);
        mav.addObject("postForm", postForm);

        mav.addObject("userRepository", userRepository);
        mav.addObject("posts", posts);
        mav.addObject("likedPosts", likedPosts);   // Add 'likedPosts' to model

        return mav;
    }

    @PostMapping("/profile/upload-profile-pic")
    public ResponseEntity<?> uploadProfilePic(@RequestParam("file") MultipartFile file, Principal principal) throws IOException {
    // Get the current logged in user
    String username = principal.getName();
    Optional<User> userOpt = userRepository.findByUsername(username);

    if(userOpt.isPresent()) {
        User user = userOpt.get();
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());

        // Ensure the filename is valid
        if (originalFilename.contains("..")) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        // The directory where the file will be stored
        String directory = "src/main/resources/static/images/profiles";

        // Ensure the directory exists
        new File(directory).mkdirs();

        // Save file in file storage
        String filePath = directory + "/" + originalFilename;
        byte[] bytes = file.getBytes();
        Path path = Paths.get(filePath);
        Files.write(path, bytes);

        // Save the path in the DB
        user.setProfilePic(filePath);
        userRepository.save(user);

        return new ResponseEntity<>(HttpStatus.OK);
    } else {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
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
    @PostMapping("/post")
    public ResponseEntity<?> createPost(@RequestParam("content") String content,
                                        @RequestParam("image") MultipartFile file,
                                        Principal principal) {

        Optional<User> userOptional = userRepository.findByUsername(principal.getName());

        if (!userOptional.isPresent()) {
            return ResponseEntity.badRequest().body("User does not exist.");
        }

        User user = userOptional.get();

        // Check if the post content is empty
        if (content.isEmpty()) {
            return ResponseEntity.badRequest().body("Post content cannot be empty.");
        }

        // Process the file
        if (file != null && !file.isEmpty()) {
            String directory = "src/main/resources/static/images/posts";
            new File(directory).mkdirs();
            String filename = StringUtils.cleanPath(file.getOriginalFilename());
            String filePath = directory + "/" + filename;
            try {
                byte[] bytes = file.getBytes();
                Path path = Paths.get(filePath);
                Files.write(path, bytes);

                // Construct the Post
                Post post = new Post();
                post.setContent(content);
                post.setDate(LocalDateTime.now());
                post.setUser(user);  // Set the User instead of userId
                // Save only filename, not the full path
                post.setImage(filename);
                post.setNickname(user.getNickname());
                postRepository.save(post);

                return ResponseEntity.ok(post);
            } catch(IOException e) {
                return ResponseEntity.badRequest().body("Error occurred while saving uploaded image. Please try again.");
            }
        } else {
            return ResponseEntity.badRequest().body("No image provided. Please try again.");
        }
    }

    @PostMapping("/post/delete/{id}")
    public String deletePost(@PathVariable Long id) {
        // Get the post from the repository
        Optional<Post> postOpt = postRepository.findById(id);

        // Ensure the post exists
        if(postOpt.isPresent()) {
            Post post = postOpt.get();

            // Get the userId from the Post
            Long userId = post.getUserId();

            // Get the user using the userId
            Optional<User> userOpt = userRepository.findById(userId);

            // Ensure the User exists
            if(userOpt.isPresent()) {
                // Get the username from the User
                String username = userOpt.get().getUsername();

                // Delete the post
                postRepository.deleteById(id);

                return "redirect:/profile/" + username;
            } else {
                return "redirect:/defaultPage";
            }
        } else {
            return "redirect:/defaultPage";
        }
    }

    @ResponseBody
    @PostMapping("/post/toggle-like/{id}")
    public ResponseEntity<Map<String, Object>> toggleLike(@PathVariable Long id) {
        Optional<Post> postOptional = postRepository.findById(id);
        Map<String, Object> response = new HashMap<>();

        if (postOptional.isPresent()) {
            Post post = postOptional.get();
            String loggedInUsername = SecurityContextHolder.getContext().getAuthentication().getName();
            Optional<User> loggedInUser = userRepository.findByUsername(loggedInUsername);

            if (loggedInUser.isPresent()) {
                User user = loggedInUser.get();
                List<User> likes = post.getLikes();

                if (likes.contains(user)) {
                    likes.remove(user);
                    response.put("liked", false);
                } else {
                    likes.add(user);
                    response.put("liked", true);
                }

                postRepository.save(post);

                // fetch the updated like count from the post repository
                postOptional = postRepository.findById(id);
                post = postOptional.get();
                response.put("likesCount", post.getLikes().size());
            }
        }

        return ResponseEntity.ok(response);
    }

    @ResponseBody
    @GetMapping("/profile/liked-posts/{username}")
    public List<Post> getLikedPosts(@PathVariable String username) {
        // Get liked posts by username
        Optional<User> user = userRepository.findByUsername(username);
        List<Post> likedPosts = new ArrayList<>();
        if (user.isPresent()) {
            likedPosts = userRepository.findLikedPostsByUserId(user.get().getId());
        }

        String loggedInUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        for (Post likedPost : likedPosts) {
            setPostLikedAndDifference(likedPost, loggedInUsername);
        }

        return likedPosts;  // return as JSON
    }

    @PostMapping("/user/nickname/update")
    public ResponseEntity<?> updateNickname(@RequestBody Map<String, String> newNickname) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> user = userRepository.findByUsername(username);

        if(user.isPresent()) {
            User userToUpdate = user.get();
            userToUpdate.setNickname(newNickname.get("nickname"));
            userRepository.save(userToUpdate);
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/user/{username}/image")
    public ResponseEntity<Resource> getProfileImage(@PathVariable String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isPresent()) {
            try {
                Path filePath = Paths.get(userOpt.get().getProfilePic());
                Resource resource = new UrlResource(filePath.toUri());
                if(resource.exists()) {
                    String fileName = resource.getFilename();
                    String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                            .path("/user/")
                            .path(username)
                            .path("/image/")
                            .path(fileName)
                            .toUriString();

                    return ResponseEntity.ok()
                            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                            .body(resource);
                }else{
                    throw new FileNotFoundException("File doesn't exist");
                }
            } catch (Exception e) {
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/user/follow-status/{username}")
    public ResponseEntity<Boolean> getFollowStatus(@PathVariable String username) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        Optional<User> currentUser = userRepository.findByUsername(currentUsername);
        if (!currentUser.isPresent())
            return ResponseEntity.notFound().build();

        Boolean following = currentUser.get().getFollowing().stream().anyMatch(u -> u.getUsername().equals(username));
        return ResponseEntity.ok(following);
    }

    @PostMapping("/user/follow-toggle/{username}")
    public ResponseEntity<?> toggleFollow(@PathVariable String username) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        Optional<User> currentUser = userRepository.findByUsername(currentUsername);
        Optional<User> userToFollowUnfollow = userRepository.findByUsername(username);
        if (!currentUser.isPresent() || !userToFollowUnfollow.isPresent())
            return ResponseEntity.notFound().build();

        User current = currentUser.get();
        User toFollowUnfollow = userToFollowUnfollow.get();

        if (current.getFollowing().contains(toFollowUnfollow)) {
            // Unfollow
            current.getFollowing().remove(toFollowUnfollow);
            toFollowUnfollow.getFollowers().remove(current);
        } else {
            // Follow
            current.getFollowing().add(toFollowUnfollow);
            toFollowUnfollow.getFollowers().add(current);
        }

        userRepository.save(current);
        userRepository.save(toFollowUnfollow);

        return ResponseEntity.ok().build();
    }
    @GetMapping("/load-more-posts/{page}")
    public ResponseEntity<List<Post>> loadMorePosts(@PathVariable int page) {
        Pageable pageable = PageRequest.of(page, 5, Sort.by("date").descending());
        List<Post> posts = postRepository.findPostsByPage(pageable);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/images/posts/{filename}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        String directory = "src/main/resources/static/images/posts/";
        Resource file = new FileSystemResource(directory + filename);
        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(file);
    }

}