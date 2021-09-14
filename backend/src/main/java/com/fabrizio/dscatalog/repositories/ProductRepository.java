package com.fabrizio.dscatalog.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.fabrizio.dscatalog.entities.Category;
import com.fabrizio.dscatalog.entities.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

//	N+1 Consulta ARRUMADO
	@Query(   "SELECT DISTINCT obj "
			+ "  FROM Product obj "
			+ " INNER JOIN obj.categories cats "
			+ " WHERE ( COALESCE( :categories ) IS NULL OR cats IN :categories ) "
			+ "   AND ( LOWER( obj.name ) LIKE LOWER( CONCAT( '%', :name, '%' ) ) ) " )
	Page<Product> find(List<Category> categories, String name, Pageable pageable);
	
//	JOIN FETCH - não funciona com página, somente com list
	@Query(" SELECT obj "
			+ "FROM Product obj "
			+ "JOIN FETCH obj.categories "
			+ "WHERE obj IN :products ")
	List<Product> findProductsWithCategories(List<Product>products); 
	
//	Sem N+1 Consulta
//	@Query(   "SELECT DISTINCT obj "
//			+ "  FROM Product obj "
//			+ " INNER JOIN obj.categories cats "
//			+ " WHERE ( COALESCE( :categories ) IS NULL OR cats IN :categories ) "
//			+ "   AND ( LOWER( obj.name ) LIKE LOWER( CONCAT( '%', :name, '%' ) ) ) " )
//	Page<Product> find(List<Category> categories, String name, Pageable pageable);
	
	// COALESCE( ) Faz uma adaptação para valor nulo, nos BD
	
//	@Query(   "SELECT obj "
//			+ "  FROM Product obj "
//			+ " WHERE :categories IN obj.categories")
//	Page<Product> find(List<Category> categories, String name, Pageable pageable);

}
