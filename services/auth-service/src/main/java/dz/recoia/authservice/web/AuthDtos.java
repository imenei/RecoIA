package dz.recoia.authservice.web;

import dz.recoia.authservice.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

/** Objets echanges avec le frontend. */
public final class AuthDtos {

    private AuthDtos() { }

    public record RegisterRequest(
            @NotBlank @Size(max = 120) String fullName,
            @NotBlank @Email @Size(max = 160) String email,
            @NotBlank @Size(min = 8, max = 100) String password,
            @Size(max = 8) List<@Size(max = 60) String> interests,
            @Size(max = 40) String track,
            @Size(max = 10) String language,
            @Size(max = 10) String duration) { }

    public record LoginRequest(@NotBlank String email, @NotBlank String password) { }

    public record UpdateProfileRequest(
            @Size(max = 120) String fullName,
            @Size(max = 8) List<@Size(max = 60) String> interests,
            @Size(max = 40) String track,
            @Size(max = 10) String language,
            @Size(max = 10) String duration) { }

    public record UserResponse(Long id, String fullName, String email, String role, List<String> interests,
                               String track, String language, String duration) {
        public static UserResponse from(User u) {
            return new UserResponse(u.getId(), u.getFullName(), u.getEmail(), u.getRole().name(),
                    List.copyOf(u.getInterests()), u.getTrack(), u.getPreferredLanguage(), u.getPreferredDuration());
        }
    }

    public record AuthResponse(String token, UserResponse user) { }
}