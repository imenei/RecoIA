package dz.recoia.catalogueservice.repo;

import dz.recoia.catalogueservice.domain.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CourseRepository extends JpaRepository<Course, Long> {

    /** Les parametres ne sont jamais null (chaine vide = pas de filtre) : evite les erreurs de typage PostgreSQL. */
    @Query("""
        select c from Course c
        where (:q = '' or lower(c.title) like lower(concat('%', :q, '%'))
               or lower(c.university) like lower(concat('%', :q, '%'))
               or lower(c.skills) like lower(concat('%', :q, '%')))
          and (:level = '' or c.level = :level)
          and (:category = '' or c.category = :category)
          and c.rating >= :minRating
        """)
    Page<Course> search(@Param("q") String q, @Param("level") String level,
                        @Param("category") String category, @Param("minRating") double minRating,
                        Pageable pageable);
}
