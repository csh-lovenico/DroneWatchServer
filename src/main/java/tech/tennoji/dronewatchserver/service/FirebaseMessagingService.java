package tech.tennoji.dronewatchserver.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tech.tennoji.dronewatchserver.entity.Subscription;
import tech.tennoji.dronewatchserver.repository.MongoRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FirebaseMessagingService {

    private final FirebaseMessaging firebaseMessaging;

    @Autowired
    private MongoRepository mongoRepository;

    public FirebaseMessagingService(FirebaseMessaging firebaseMessaging) {
        this.firebaseMessaging = firebaseMessaging;
    }

    //todo: use mongodb to manage subscription, not use topic any more.

    public void sendMessageToTopic(String title, String body, Map<String, String> data, String topic) {
        // Find subscribers
        List<String> subscribers = mongoRepository.findSubscribersByTopic(topic).parallelStream().map(Subscription::getToken).collect(Collectors.toList());
        // Build notification and message
        Notification notification = Notification.builder().setTitle(title).setBody(body).build();
        MulticastMessage.Builder multicastMessageBuilder = MulticastMessage.builder().setNotification(notification).addAllTokens(subscribers);
        if (data != null) {
            multicastMessageBuilder.putAllData(data);
        }
        var multicastMessage = multicastMessageBuilder.build();
        try {
            // Send message
            var response = firebaseMessaging.sendMulticast(multicastMessage);
            log.info(String.format("Message sent, success: %d, fail: %d.", response.getSuccessCount(), response.getFailureCount()));
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    public void testSendMessage(String token) {
        // Only for test
        Notification notification = Notification.builder().setTitle("test").setBody("test").build();
        Message.Builder messageBuilder = Message.builder().setNotification(notification).setToken(token);
        var message = messageBuilder.build();
        try {
            var response = firebaseMessaging.send(message);
            log.info(String.format("Message sent, ID: %s", response));
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

}
