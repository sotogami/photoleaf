package com.example.photoleaf.Message;

import com.example.photoleaf.User.User;
import com.example.photoleaf.User.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
public class MessageController {

    private UserRepository userRepository;

    private MessageRepository messageRepository;

    public MessageController(UserRepository userRepository, MessageRepository messageRepository) {
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
    }

    @PostMapping("/send-message")
    public ResponseEntity<?> sendMessage(@RequestBody MessageDTO messageDTO) {
        if (messageDTO.getSenderId() == null || messageDTO.getReceiverId() == null) {
            return new ResponseEntity<>("Sender ID and Receiver ID must not be null.", HttpStatus.BAD_REQUEST);
        }

        Optional<User> optSender = userRepository.findById(messageDTO.getSenderId());
        Optional<User> optReceiver = userRepository.findById(messageDTO.getReceiverId());

        if (optSender.isPresent() && optReceiver.isPresent()) {
            User sender = optSender.get();
            User receiver = optReceiver.get();

            Message message = new Message();
            message.setSender(sender);
            message.setReceiver(receiver);
            message.setContent(messageDTO.getContent());
            message.setSendTime(LocalDateTime.now());
            message.setRead(false);

            Message savedMessage = messageRepository.save(message);
            return new ResponseEntity<>(savedMessage, HttpStatus.OK);
        } else {
            // handle error
            return new ResponseEntity<>("Sender or Receiver not found.", HttpStatus.BAD_REQUEST);
        }
    }
    @GetMapping("/messages")
    public ResponseEntity<?> getMessages(@RequestParam Long userId, @RequestParam Long receiverId) {
        Optional<User> optUser = userRepository.findById(userId);
        Optional<User> optReceiver = userRepository.findById(receiverId);

        if (optUser.isPresent() && optReceiver.isPresent()) {
            User user = optUser.get();
            User receiver = optReceiver.get();

            // Retrieve all messages exchanged between the two users
            List<Message> messages = messageRepository.findBySenderAndReceiverOrReceiverAndSender(user, receiver);
            messages.sort(Comparator.comparing(Message::getSendTime));

            List<MessageDTO> messageDTOs = MessageDTO.fromEntityList(messages);
            return new ResponseEntity<>(messageDTOs, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
    @GetMapping("/search-users")
    public ResponseEntity<?> searchUsers(@RequestParam String username) {
        Optional<User> user = userRepository.findByUsername(username);
        return user.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}
