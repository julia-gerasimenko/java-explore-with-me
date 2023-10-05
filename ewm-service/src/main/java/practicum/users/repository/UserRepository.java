package practicum.users.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import practicum.users.model.User;
import practicum.util.Pagination;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    Page<User> findAllByIdIn(List<Long> ids, Pagination pagination);

}