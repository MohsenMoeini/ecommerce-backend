package com.ecommerce.repository;

import com.ecommerce.entity.Address;
import com.ecommerce.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {
    Optional<Warehouse> findByName(String name);
    List<Warehouse> findByAddress(Address address);
    boolean existsByName(String name);
    List<Warehouse> findByActiveTrue();
}
