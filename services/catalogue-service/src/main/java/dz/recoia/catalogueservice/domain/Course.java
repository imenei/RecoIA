package dz.recoia.catalogueservice.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "courses")
public class Course {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank @Column(nullable = false, length = 300)
    private String title;
    @Column(length = 200)
    private String university;
    @Column(length = 50)
    private String level;
    @Column(length = 100)
    private String category;
    @Column(length = 4000)
    private String description;
    @Column(length = 1000)
    private String skills;
    @DecimalMin("0") @DecimalMax("5")
    private double rating;
    private int reviews;
    private int hours;
    @Column(length = 300)
    private String imageUrl;
    @Column(length = 100)
    private String badge;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getUniversity() { return university; }
    public void setUniversity(String university) { this.university = university; }
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getSkills() { return skills; }
    public void setSkills(String skills) { this.skills = skills; }
    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }
    public int getReviews() { return reviews; }
    public void setReviews(int reviews) { this.reviews = reviews; }
    public int getHours() { return hours; }
    public void setHours(int hours) { this.hours = hours; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getBadge() { return badge; }
    public void setBadge(String badge) { this.badge = badge; }
}
