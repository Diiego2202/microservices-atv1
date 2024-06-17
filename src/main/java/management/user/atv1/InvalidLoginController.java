package management.user.atv1;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/invalid-logins")
public class InvalidLoginController {

    @Autowired
    private InvalidLoginRepository repository;

    @PostMapping
    public ResponseEntity<String> createInvalidLogin(@RequestParam String username) {
        InvalidLogin newInvalidLogin = new InvalidLogin(username, new Date());
        repository.save(newInvalidLogin);
        return new ResponseEntity<>("Login inválido!!", HttpStatus.CREATED);
    }
}