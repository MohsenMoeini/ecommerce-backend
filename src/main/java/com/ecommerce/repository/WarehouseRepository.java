package com.ecommerce.repository;

import com.ecommerce.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {
    Optional<Warehouse> findByName(String name);
    List<Warehouse> findByLocation(String location);
    boolean existsByName(String name);
    List<Warehouse> findByActiveTrue();
}
