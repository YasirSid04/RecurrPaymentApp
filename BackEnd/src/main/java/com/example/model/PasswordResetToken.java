package com.example.model;



import lombok.Data;

import javax.persistence.*;
import java.util.Calendar;
import java.util.Date;

@Entity
@Data
public class PasswordResetToken {

    private static final int EXPIRATION = 1000 * 60 * 60 * 24;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(unique=true, length = 60)
    private String token;

    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private User user;
    private Date expiryDate;

    public PasswordResetToken(String token, User user){
        this.token = token;
        this.user = user;
        Date oldDate = new Date();
        Date date = new Date(oldDate.getTime() + EXPIRATION);
        this.expiryDate = date;
    }

    public PasswordResetToken() {

    }
}
