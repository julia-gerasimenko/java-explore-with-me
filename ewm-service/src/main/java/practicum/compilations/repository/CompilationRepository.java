package practicum.compilations.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import practicum.compilations.model.Compilation;
import practicum.util.Pagination;

public interface CompilationRepository extends JpaRepository<Compilation, Long> {
    Page<Compilation> findAllByPinned(Boolean pinned, Pagination paginationSetup);
}