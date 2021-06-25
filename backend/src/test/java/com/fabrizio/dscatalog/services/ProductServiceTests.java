package com.fabrizio.dscatalog.services;

import static org.mockito.Mockito.times;

import java.util.List;
import java.util.Optional;

import javax.persistence.EntityNotFoundException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.fabrizio.dscatalog.dto.ProductDTO;
import com.fabrizio.dscatalog.entities.Category;
import com.fabrizio.dscatalog.entities.Product;
import com.fabrizio.dscatalog.repositories.CategoryRepository;
import com.fabrizio.dscatalog.repositories.ProductRepository;
import com.fabrizio.dscatalog.services.exceptions.DatabaseException;
import com.fabrizio.dscatalog.services.exceptions.ResourceNotFoundException;
import com.fabrizio.dscatalog.tests.Factory;

@ExtendWith(SpringExtension.class)
public class ProductServiceTests {

	@InjectMocks
	private ProductService service;
	
	// Quando se cria um mock, não pode esquecer de configurar o comportamento simulado dele.
	@Mock
	private ProductRepository repository;
	
	@Mock
	private CategoryRepository categoryRepository;
	
	private long existingId;
	private long nonExistingId;
	private long dependentId;
	private PageImpl<Product> page;		// Representa uma página de dados.
	private Product product;
	private Category category;
	private ProductDTO productDTO;
	
	@BeforeEach
	void setUp() throws Exception {
		existingId = 1L;
		nonExistingId = 2L;
		dependentId = 3L;
		product = Factory.createProduct();
		category = Factory.createCategory();
		page = new PageImpl<>(List.of(product));
		productDTO = Factory.createProductDTO();
		
		// QUANDO O OBJ TEM RETORNO
		// findAll
		Mockito.when(repository.findAll((Pageable)ArgumentMatchers.any())).thenReturn(page);
		
		Mockito.when(repository.save(ArgumentMatchers.any())).thenReturn(product);
		
		Mockito.when(repository.findById(existingId)).thenReturn(Optional.of(product));
		Mockito.when(repository.findById(nonExistingId)).thenReturn(Optional.empty());
		
		Mockito.when(repository.getOne(existingId)).thenReturn(product);
		Mockito.when(repository.getOne(nonExistingId)).thenThrow(EntityNotFoundException.class);
		
		Mockito.when(categoryRepository.getOne(existingId)).thenReturn(category);
		Mockito.when(categoryRepository.getOne(nonExistingId)).thenThrow(EntityNotFoundException.class);
		
		// QUANDO O OBJ RETORNA VOID - Simulando repository usando MOCKITO
		 Mockito.doNothing().when(repository).deleteById(existingId);
		 Mockito.doThrow(EmptyResultDataAccessException.class).when(repository).deleteById(nonExistingId);
		 Mockito.doThrow(DataIntegrityViolationException.class).when(repository).deleteById(dependentId);
	}
	
	@Test
	public void updateDeveriaLancarResourceNotFoundExceptionQUandoNaoExistirId() {
		
		Assertions.assertThrows(ResourceNotFoundException.class, () -> {
			service.update(nonExistingId, productDTO);
		});
		Mockito.verify(repository).getOne(nonExistingId);
	}
	
	@Test
	public void updateDeveriaRetornarUmProdutoDTOQUandoExistirId() {
		
		ProductDTO result = service.update(existingId, productDTO);
		
		Assertions.assertNotNull(result);
		Mockito.verify(categoryRepository).getOne(existingId);
	}
	
	@Test
	public void findByIdDeveriaLancarResourceNotFoundExceptionQuandoNaoExistirId() {
		
		Assertions.assertThrows(ResourceNotFoundException.class, () -> {
			service.findById(nonExistingId);
		});
		Mockito.verify(repository, Mockito.times(1)).findById(nonExistingId);
	}
	
	@Test
	public void findByIdDeveriaRetornarumProductDTOQuandoExistirId() {
				
		ProductDTO result = service.findById(existingId);
		
		Assertions.assertNotNull(result);
		Mockito.verify(repository, times(1)).findById(existingId);
	}
	
	@Test
	public void findAllPagedDeveriaRetornarUmaPagina() {
		
		Pageable pageable = PageRequest.of(0, 10);
		
		Page<ProductDTO> result = service.findAllPaged(pageable);
		
		Assertions.assertNotNull(result);
		Mockito.verify(repository, Mockito.times(1)).findAll(pageable);
	}
	
	@Test
	public void deletDeveriaLancarDatabaseExceptionQuandoNaoExistirId() {
		
		Assertions.assertThrows(DatabaseException.class, () -> {
			service.delete(dependentId);
		});
		Mockito.verify(repository, Mockito.times(1)).deleteById(dependentId);
	}
	
	@Test
	public void deletDeveriaLancarResourceNotFoundExceptionQuandoNaoExistirId() {
		
		Assertions.assertThrows(ResourceNotFoundException.class, () -> {
			service.delete(nonExistingId);
		});
		Mockito.verify(repository, Mockito.times(1)).deleteById(nonExistingId);
	}
	
	@Test
	public void deletDeveriaNaoFazerNadaQuandoExistirId() {
		
		Assertions.assertDoesNotThrow(() -> {
			service.delete(existingId);
		});
		Mockito.verify(repository, Mockito.times(1)).deleteById(existingId);
	}
	
}
