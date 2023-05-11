package example.repos;

import example.models.ApplicationUser;
import example.models.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApplicationUserRepository extends JpaRepository<ApplicationUser, Long> {
      ApplicationUser findByUsername(String username);
     List<ApplicationUser> findAllByOrderByUsernameAsc();

}
