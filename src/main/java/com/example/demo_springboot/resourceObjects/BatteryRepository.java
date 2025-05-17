package com.example.demo_springboot.resourceObjects;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BatteryRepository extends JpaRepository<Battery,Long> {
  List<Battery> findByPostcodeBetweenOrderByNameAsc(String from, String to);
  List<Battery> findByPostcodeGreaterThanEqualOrderByNameAsc(String from);
  List<Battery> findByPostcodeLessThanEqualOrderByNameAsc(String to);
}