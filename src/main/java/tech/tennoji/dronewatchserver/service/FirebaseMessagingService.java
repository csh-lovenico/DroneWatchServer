package tech.tennoji.dronewatchserver.service;

import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import com.google.firebase.messaging.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tech.tennoji.dronewatchserver.entity.Subscription;
import tech.tennoji.dronewatchserver.repository.MongoRepository;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FirebaseMessagingService {

    @Autowired
    private FirebaseMessaging firebaseMessaging;

    @Autowired
    private MongoRepository mongoRepository;

    @Autowired
    private Executor executor;

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
            var future = firebaseMessaging.sendMulticastAsync(multicastMessage);
            ApiFutures.addCallback(future, new ApiFutureCallback<>() {
                @Override
                public void onFailure(Throwable t) {
                    log.error("Send fail: " + t.getMessage());
                }

                @Override
                public void onSuccess(BatchResponse result) {
                    log.info(String.format("Message sent, success: %d, fail: %d.", result.getSuccessCount(), result.getFailureCount()));
                }
            }, executor);
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
            var future = firebaseMessaging.sendAsync(message);
            ApiFutures.addCallback(future, new ApiFutureCallback<>() {
                @Override
                public void onFailure(Throwable t) {
                    log.error("Send fail: " + t.getMessage());
                }

                @Override
                public void onSuccess(String result) {
                    log.info(String.format("Message sent, ID: %s", result));
                }
            }, executor);
            log.info("This message should come before the result.");
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

}
