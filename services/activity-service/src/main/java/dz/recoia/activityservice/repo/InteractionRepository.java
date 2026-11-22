package dz.recoia.activityservice.repo;

import dz.recoia.activityservice.domain.Interaction;
import dz.recoia.activityservice.domain.InteractionType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InteractionRepository extends JpaRepository<Interaction, Long> {

    Optional<Interaction> findFirstByStudentIdAndCourseIdAndType(long studentId, long courseId, InteractionType type);

    List<Interaction> findByStudentId(long studentId);

    long countByType(InteractionType type);

    long countByTypeAndScore(InteractionType type, Integer score);

    /** Une formation et le nombre de lignes correspondantes. */
    interface CourseCount {
        Long getCourseId();
        long getTotal();
    }

    @Query("select i.courseId as courseId, count(i) as total from Interaction i "
            + "where i.type = :type group by i.courseId order by count(i) desc, i.courseId asc")
    List<CourseCount> top(@Param("type") InteractionType type, Pageable pageable);

    @Query("select i.courseId as courseId, count(i) as total from Interaction i "
            + "where i.type = :type and i.score = :score group by i.courseId order by count(i) desc, i.courseId asc")
    List<CourseCount> topByScore(@Param("type") InteractionType type, @Param("score") Integer score, Pageable pageable);
}
