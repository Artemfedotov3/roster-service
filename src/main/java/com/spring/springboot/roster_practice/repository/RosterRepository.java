package com.spring.springboot.roster_practice.repository;

import com.spring.springboot.roster_practice.entity.RosterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RosterRepository extends JpaRepository<RosterEntity, Long> {
}
