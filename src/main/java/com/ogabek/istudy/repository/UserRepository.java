package com.ogabek.istudy.repository;

import com.ogabek.istudy.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    List<User> findByBranchId(Long branchId);
    boolean existsByUsername(String username);
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.branch WHERE u.username = :username")
    Optional<User> findByUsernameWithBranch(@Param("username") String username);
}