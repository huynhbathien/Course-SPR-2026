package com.mycompany.repository;

import com.mycompany.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByUsername(String username);

    Optional<UserEntity> findByEmail(String email);

    Optional<UserEntity> findByGoogleId(String googleId);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM UserEntity u WHERE u.active = true AND u.username = :username")
    Optional<UserEntity> findActiveUserByUsername(@Param("username") String username);

    @Query("SELECT u FROM UserEntity u WHERE u.active = true AND u.email = :email")
    Optional<UserEntity> findActiveUserByEmail(@Param("email") String email);
}

