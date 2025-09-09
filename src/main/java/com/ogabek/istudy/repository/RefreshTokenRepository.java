package com.ogabek.istudy.repository;

import com.ogabek.istudy.entity.RefreshToken;
import com.ogabek.istudy.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    Optional<RefreshToken> findByUser(User user);
    
    @Modifying
    int deleteByUser(User user);
    
    @Modifying
    int deleteByExpiryDateBefore(LocalDateTime expiryDate);
}