package com.example.demo.repository;

import com.example.demo.entity.RegistroEntity;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegistroRepository extends JpaRepository<RegistroEntity, Long> {

    List<RegistroEntity> findByEstado(String estado);
    long countByEstado(String estado);
}