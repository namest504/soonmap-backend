package soonmap.entity;

import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Member implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long id;

    @Column
    private String userId;

    @Column
    private String userEmail;

    @Column
    private String userName;

    @Column
    private String userPassword;

    @Column
    @Enumerated(EnumType.STRING)
    private AccountType accountType;

    @Column
    private boolean isBan;

    @Column
    private boolean isAdmin;

    @Column
    private boolean isManager;

    @Column
    private boolean isStaff;

    @Column
    private String snsId;

    @Column
    private LocalDateTime userCreateAt;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        if (this.isAdmin) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }
        if (this.isManager) {
            authorities.add(new SimpleGrantedAuthority("ROLE_MANAGER"));
        }
        if (this.isStaff) {
            authorities.add(new SimpleGrantedAuthority("ROLE_STAFF"));
        }
        return authorities;
    }

    @Override
    public String getPassword() {
        return this.userPassword;
    }

    @Override
    public String getUsername() {
        return this.userName;
    }

    @Override
    public boolean isAccountNonExpired() {
        return this.isBan;
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.isBan;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return this.isBan;
    }

    public String getUserEmail() {
        return this.userEmail;
    }

    public boolean isAdmin() {
        return this.isAdmin;
    }

    @Override
    public boolean isEnabled() {
        return this.isBan;
    }

    public void updateBan() {
        this.isBan = !this.isBan;
    }

    public void updateManager() {
        this.isManager = !this.isManager;
    }

    public void updateAdmin() {
        this.isAdmin = !this.isAdmin;
    }
}
