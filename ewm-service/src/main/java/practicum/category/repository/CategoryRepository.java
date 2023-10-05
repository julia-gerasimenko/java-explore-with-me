package practicum.category.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import practicum.category.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}