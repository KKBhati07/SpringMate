package com.example.SpringMate.Repositoy;
import com.example.SpringMate.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {
    @Query("SELECT u FROM User u WHERE u.uuid = :uuid")
//    @Query(value = "SELECT * FROM users WHERE uuid = :uuid", nativeQuery = true) #to use SQL instead of JPQL
    User findByUuid(String uuid);
    User findByEmail(String email);

    //we can also use Criteria API for more dynamic and custom queries
    // for that need to create a different class instead of this repository interface :: REFER DOCS
    //here is the example to that

//    public List<User> findUsersByRole(String role) {
//        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
//        CriteriaQuery<User> cq = cb.createQuery(User.class);
//        Root<User> user = cq.from(User.class);
//        cq.select(user).where(cb.equal(user.get("role"), role));
//
//        return entityManager.createQuery(cq).getResultList();
//    }
}
