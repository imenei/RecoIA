package dz.recoia.authservice.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** Un compte : etudiant ou administrateur. */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 160)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Column(name = "full_name", nullable = false, length = 120)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role = Role.STUDENT;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_interests", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "interest", length = 60)
    private List<String> interests = new ArrayList<>();

    /** Parcours : Licence, Master, Doctorat, Formation courte, Certifications (facultatif). */
    @Column(name = "track", length = 40)
    private String track;

    /** Preference de langue : FR, EN ou ANY (facultatif). */
    @Column(name = "pref_language", length = 10)
    private String preferredLanguage;

    /** Preference de duree : SHORT, MEDIUM, LONG ou ANY (facultatif). */
    @Column(name = "pref_duration", length = 10)
    private String preferredDuration;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public List<String> getInterests() { return interests; }
    public void setInterests(List<String> interests) { this.interests = new ArrayList<>(interests); }
    public String getTrack() { return track; }
    public void setTrack(String track) { this.track = track; }
    public String getPreferredLanguage() { return preferredLanguage; }
    public void setPreferredLanguage(String preferredLanguage) { this.preferredLanguage = preferredLanguage; }
    public String getPreferredDuration() { return preferredDuration; }
    public void setPreferredDuration(String preferredDuration) { this.preferredDuration = preferredDuration; }
    public Instant getCreatedAt() { return createdAt; }
}