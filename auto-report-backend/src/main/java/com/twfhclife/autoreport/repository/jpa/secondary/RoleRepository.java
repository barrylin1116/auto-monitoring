package com.twfhclife.autoreport.repository.jpa.secondary;

import com.twfhclife.autoreport.entity.secondary.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface RoleRepository extends JpaRepository<Role, String> {

    @Query("SELECT r FROM Role r " +
            "JOIN UserRole ur ON r.roleId = ur.roleId " +
            "WHERE ur.userId = (SELECT u.id FROM UserEntity u WHERE u.username = :username and u.realmId = :realmId)")
    List<Role> findRolesByUsername(@Param("username") String username, @Param("realmId") String realmId);
}
