package management.user.atv1;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.Optional;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.kafka.core.KafkaTemplate;

@RestController
@RequestMapping("/user")
public class User {

    @Autowired
    private UserDAO dao;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @PostMapping
    public ResponseEntity<String> createUser(@RequestBody UserBean user) throws DupedIdException {

        if (dao.count() == 0) {
            System.out.println("ERRO!! Não ha usuários cadastrados!");
            dao.save(user);
            return new ResponseEntity<String>(user.getUsername(), HttpStatus.CREATED);
        } else if (dao.findByUsername(user.getUsername()) != null) {
            System.out.println("ERRO!! Usuário já existe no sistema!");
            return new ResponseEntity<String>("Usuário já existe no sistema", HttpStatus.NOT_ACCEPTABLE);
        }

        System.out.println("Usuário não existe no sistema!");

        user.setTotalFails(0);
        user.setTotalLogins(0);

        System.out.println(user);

        dao.save(user);

        return new ResponseEntity<String>(user.getUsername(), HttpStatus.CREATED);
    }

    @GetMapping()
    public ResponseEntity<Iterable<UserBean>> allUsers() {

        System.out.println(dao.count());

        if (dao.count() > 0) {
            return new ResponseEntity<Iterable<UserBean>>(dao.findAll(), HttpStatus.OK);
        } else {
            System.out.println("ERRO!! Não ha usuários cadastrados!");
            return new ResponseEntity<Iterable<UserBean>>(HttpStatus.NO_CONTENT);
        }
    }

    @PutMapping()
    public ResponseEntity<String> updateUser(@RequestBody UserBean user) {

        if (user.getUsername() == null || user.getPassword() == null) {
            return new ResponseEntity<String>("ERRO!! Usuário e senha são campos obrigatórios!",
                    HttpStatus.BAD_REQUEST);
        }

        UserBean userExists = dao.findByUsername(user.getUsername());
        if (userExists == null) {
            System.out.println("ERRO!! Nenhum usuário foi encontrado!");
            return new ResponseEntity<String>("Nenhum usuário foi encontrado", HttpStatus.UNAUTHORIZED);
        }

        if (userExists.isBlocked()) {
            System.out.println("Usuário bloqueado!");
            return new ResponseEntity<String>("Usuário bloqueado", HttpStatus.UNAUTHORIZED);
        }

        try {
            if (userExists.getTotalLogins() > 10) {
                System.out.println("ERRO!! Você atingiu o limite de tentativas, troque sua senha!");
                return new ResponseEntity<String>("Você atingiu o limite de tentativas, troque sua senha",
                        HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
        }

        if (!userExists.getPassword().equals(user.getPassword())) {
            System.out.println("ERRO!! Senha incorreta!");

            try {
                userExists.setTotalFails(userExists.getTotalFails() + 1);
            } catch (Exception e) {
                userExists.setTotalFails(1);
            }

            if (userExists.getTotalFails() > 5) {
                userExists.setBlocked(true);
                System.out.println("Usuário bloqueado");
            }

            kafkaTemplate.send("invalid-logins", user.getUsername());

            dao.save(userExists);

            return new ResponseEntity<String>("Senha incorreta", HttpStatus.UNAUTHORIZED);
        }

        try {
            userExists.setTotalLogins(userExists.getTotalLogins() + 1);
        } catch (Exception e) {
            userExists.setTotalLogins(1);
        }

        dao.save(userExists);

        return new ResponseEntity<String>("Login efetuado com sucesso :)", HttpStatus.OK);
    }

    @GetMapping("/blocked")
    public ResponseEntity<UserBean[]> usuariosBloqueados() {

        UserBean[] bloqueados = dao.findByBlockedTrue();

        System.out.println(bloqueados.length);

        if (bloqueados.length > 0) {
            return new ResponseEntity<UserBean[]>(bloqueados, HttpStatus.OK);
        } else {
            System.out.println("Não há usuários bloqueados");
            return new ResponseEntity<UserBean[]>(HttpStatus.NO_CONTENT);
        }
    }

    @PutMapping("/changepassword")
    public ResponseEntity<String> trocaSenha(@RequestBody ChangePasswordBean user) {
        UserBean userExists = dao.findByUsername(user.getUsername());

        if (userExists == null) {
            System.out.println("ERRO!! Usuário não existe!");
            return new ResponseEntity<String>("Usuário não existe", HttpStatus.NOT_FOUND);
        }

        if (userExists.isBlocked()) {
            System.out.println("ERRO!! Usuário bloqueado!");
            return new ResponseEntity<String>("Usuário bloqueado", HttpStatus.UNAUTHORIZED);
        }

        if (!userExists.getPassword().equals(user.getCurrentPassword())) {
            System.out.println("ERRO!! Senha atual incorreta");
            return new ResponseEntity<String>("Senha atual incorreta", HttpStatus.UNAUTHORIZED);
        }

        if (userExists.getPassword().equals(user.getNewPassword())) {
            System.out.println("ERRO!! Senha atual e a senha nova são iguais!");
            return new ResponseEntity<String>("Senha atual e a senha nova são iguais", HttpStatus.BAD_REQUEST);
        }

        userExists.setPassword(user.getNewPassword());
        userExists.setTotalLogins(0);

        dao.save(userExists);

        return new ResponseEntity<String>("Senha alterada com sucesso", HttpStatus.OK);
    }

    @PutMapping("/desbloquear/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> unblockUser(@PathVariable String username) {
        return username == null ? ResponseEntity.badRequest().body("ERRO!! Username é obrigatório")
                : Optional.ofNullable(dao.findByUsername(username))
                        .map(user -> !user.isBlocked()
                                ? ResponseEntity.badRequest().body("ERRO!! Usuário não está bloqueado")
                                : unlockUser(user))
                        .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário inexistente"));
    }

    private ResponseEntity<String> unlockUser(UserBean user) {
        user.setTotalFails(0);
        user.setBlocked(false);
        dao.save(user);
        return ResponseEntity.ok("Usuário desbloqueado!!");
    }
}
