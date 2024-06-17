package management.user.atv1;

import jakarta.persistence.*;
import java.util.Date;

@Entity
public class InvalidLogin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Date dateTime;
    private String username;

    public InvalidLogin(String username, Date dateTime) {
        this.username = username;
        this.dateTime = dateTime;
    }

    public String getUsername() {
        return username;
    }

    public Date getDateTime() {
        return dateTime;
    }

}
