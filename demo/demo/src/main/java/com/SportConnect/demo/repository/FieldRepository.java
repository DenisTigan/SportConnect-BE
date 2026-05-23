package com.SportConnect.demo.repository;


import com.SportConnect.demo.model.Field;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FieldRepository extends JpaRepository<Field, Long> {

    List<Field> findByOwnerId(Long ownerId);
    List<Field> findByCategoryIgnoreCase(String category);

    @Query("SELECT f FROM Field f WHERE " +
            "(:name IS NULL OR LOWER(f.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:category IS NULL OR LOWER(f.category) = LOWER(:category)) AND " +
            "(:maxPrice IS NULL OR f.pricePerHour <= :maxPrice)")
    List<Field> searchFields(@Param("name") String name,
                             @Param("category") String category,
                             @Param("maxPrice") Double maxPrice);
}
