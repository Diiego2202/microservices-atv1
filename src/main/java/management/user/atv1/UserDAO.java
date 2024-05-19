package management.user.atv1;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserDAO extends CrudRepository<UserBean, Integer> {

    UserBean findByUsername(String username);

    Iterable<UserBean> findByUsernameAndPassword(String username, String senha);

    UserBean[] findByBlockedTrue();

}
