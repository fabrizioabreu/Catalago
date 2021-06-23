package com.fabrizio.dscatalog.repositories;

import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.EmptyResultDataAccessException;

import com.fabrizio.dscatalog.entities.Product;
import com.fabrizio.dscatalog.tests.Factory;

@DataJpaTest
public class ProductRepositoryTests {
	
	@Autowired
	private ProductRepository repository;
	
	private long idExistente;
	private long idNaoExiste;
	private long contagemTotalDeProdutos;
	
	@BeforeEach
	void setUp() throws Exception {
		idExistente = 1L;
		idNaoExiste = 1000L;
		contagemTotalDeProdutos = 25L;
	}
	
	@Test
	 public void findByIdDeveriaRetornarUmProductQuandoIdExistir() {

		 Optional<Product> result = repository.findById(idExistente);
		 Assertions.assertTrue(result.isPresent());
	 }
	
	@Test
	 public void findByIdDeveriaRetornarVazioQuandoIdNaoExistir() {

		 Optional<Product> result = repository.findById(idNaoExiste);
		 Assertions.assertTrue(result.isEmpty());
	 }
	
	@Test
	public void SaveDeveriaSalvarNovoObjetoComIdAutoincrementQuandoIdEhNull() {
		
		Product product = Factory.createProduct();
		product.setId(null);
		
		product = repository.save(product);
		
		Assertions.assertNotNull(product.getId());	 
		Assertions.assertEquals(contagemTotalDeProdutos + 1, product.getId());
	}

	 @Test
	 public void DeletDeveriaDeletarObjetoQuandoExiteId() {

		 repository.deleteById(idExistente);
		 Optional<Product> result = repository.findById(idExistente);
		 Assertions.assertFalse(result.isPresent());
	 }
	 
	 @Test
	 public void DeletDeveriaLancarErroEmptyResultDataAccessExceptionQuandoNaoExisteId() {
		 
		 Assertions.assertThrows(EmptyResultDataAccessException.class, () ->{			 
			 repository.deleteById(idNaoExiste);
		 });
	 }
}
