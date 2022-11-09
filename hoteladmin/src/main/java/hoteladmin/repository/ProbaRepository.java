package hoteladmin.repository;

import hoteladmin.domain.Proba;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Proba entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ProbaRepository extends JpaRepository<Proba, Long> {}
