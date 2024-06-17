package management.user.atv1;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import java.util.Date;

@Service
public class KafkaInvalidLoginConsumer {
    @Autowired
    private InvalidLoginRepository repository;

    @KafkaListener(topics = "invalid-logins", groupId = "invalid-login-group")
    public void consume(String username) {
        InvalidLogin newInvalidLogin = new InvalidLogin(username, new Date());
        repository.save(newInvalidLogin);
    }
}
