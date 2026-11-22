package dz.recoia.activityservice.domain;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "interactions")
public class Interaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private long studentId;

    @Column(nullable = false)
    private long courseId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InteractionType type;

    private Integer score;

    @Column(nullable = false)
    private Instant createdAt;

    protected Interaction() { }

    public Interaction(long studentId, long courseId, InteractionType type, Integer score) {
        this.studentId = studentId;
        this.courseId = courseId;
        this.type = type;
        this.score = score;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public long getStudentId() { return studentId; }
    public long getCourseId() { return courseId; }
    public InteractionType getType() { return type; }
    public Integer getScore() { return score; }
    public Instant getCreatedAt() { return createdAt; }

    public void setScore(Integer score) {
        this.score = score;
        this.createdAt = Instant.now();
    }
}
