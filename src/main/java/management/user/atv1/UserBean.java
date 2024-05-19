package management.user.atv1;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity(name = "UserTable")
public class UserBean {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "userId")

    private int id;
    private String username;
    private String password;
    private Integer totalLogins;
    private Integer totalFails;
    private boolean blocked;

    // id
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    // username
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    // password
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // Total logins
    public Integer getTotalLogins() {
        return totalLogins;
    }

    public void setTotalLogins(Integer total_logins) {
        this.totalLogins = total_logins;
    }

    // Total fails
    public Integer getTotalFails() {
        return totalFails;
    }

    public void setTotalFails(Integer total_falhas) {
        this.totalFails = total_falhas;
    }

    // Blocked
    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

}