package com.example.photoleaf.Message;

import com.example.photoleaf.User.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findBySenderOrReceiver(User sender, User receiver);
    List<Message> findBySenderAndReceiver(User sender, User receiver);
    @Query("select m from Message m where (m.sender = :sender and m.receiver = :receiver) or (m.sender = :receiver and m.receiver = :sender) order by m.sendTime")
    List<Message> findBySenderAndReceiverOrReceiverAndSender(@Param("sender") User sender, @Param("receiver") User receiver);
}