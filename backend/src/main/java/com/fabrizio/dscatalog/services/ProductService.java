package com.fabrizio.dscatalog.services;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fabrizio.dscatalog.dto.CategoryDTO;
import com.fabrizio.dscatalog.dto.ProductDTO;
import com.fabrizio.dscatalog.entities.Category;
import com.fabrizio.dscatalog.entities.Product;
import com.fabrizio.dscatalog.repositories.CategoryRepository;
import com.fabrizio.dscatalog.repositories.ProductRepository;
import com.fabrizio.dscatalog.services.exceptions.DatabaseException;
import com.fabrizio.dscatalog.services.exceptions.ResourceNotFoundException;
 
@Service
public class ProductService {
	
	@Autowired
	private ProductRepository repository;
	
	@Autowired
	private CategoryRepository categoryRepository;
	
//	@Transactional(readOnly = true)
//	public Page<ProductDTO> findAllPaged(Pageable pageable) {
//		Page<Product> list = repository.findAll(pageable);
//		return list.map(x -> new ProductDTO(x));
//	}
	
//	COM N+1 consulta
	@Transactional(readOnly = true)
	public Page<ProductDTO> findAllPaged(Long categoryId, String name, Pageable pageable) {
	 	List<Category> categories = (categoryId == 0) ? null :
	 		Arrays.asList(categoryRepository.getOne(categoryId));
	 	Page<Product> page = repository.find(categories, name, pageable);
	 	repository.findProductsWithCategories(page.getContent());
	 	return page.map(x -> new ProductDTO(x, x.getCategories()));
	}
	
//	SEM N+1 consulta
//	@Transactional(readOnly = true)
//	public Page<ProductDTO> findAllPaged(Long categoryId, String name, Pageable pageable) {
//	 	List<Category> categories = (categoryId == 0) ? null :
//	 		Arrays.asList(categoryRepository.getOne(categoryId));
//	 	Page<Product> list = repository.find(categories, name, pageable);
//	 	return list.map(x -> new ProductDTO(x));
//	}
	
//	@Transactional(readOnly = true)
//	public Page<ProductDTO> findAllPaged(Long categoryId, String name, Pageable pageable) {
//	 	List<Category> categories = (categoryId == 0) ? null :
//	 		Arrays.asList(categoryRepository.getOne(categoryId));
//	 	Page<Product> list = repository.find(categories, name, pageable);
//	 	return list.map(x -> new ProductDTO(x));
//	}

	@Transactional(readOnly = true)
	public ProductDTO findById(Long id) {
		Optional<Product> obj = repository.findById(id);
		Product entity = obj.orElseThrow(() -> new ResourceNotFoundException("Entidade não encontrada!"));
		return new ProductDTO(entity, entity.getCategories());
	}

	@Transactional
	public ProductDTO insert(ProductDTO dto) {
		Product entity = new Product();
		copyDtoToEntity(dto, entity);
		entity = repository.save(entity);
		return new ProductDTO(entity);
	}

	@Transactional
	public ProductDTO update(Long id, ProductDTO dto) {
		try {
			Product entity = repository.getOne(id);
			copyDtoToEntity(dto, entity);
			entity = repository.save(entity);
			return new ProductDTO(entity);
		} catch (EntityNotFoundException e) {
			throw new ResourceNotFoundException("ID: " + id + " não encontrado!");
		}
		
	}

	// Não coloca o @Transactional para capturar a exceção do banco de dados, caso ele tenha alguma atribuição
	public void delete(Long id) {
		try {
			repository.deleteById(id);
		} catch (EmptyResultDataAccessException e) {
			throw new ResourceNotFoundException("ID: " + id + " não encontrado!");
		} catch (DataIntegrityViolationException e) {
			throw new DatabaseException("Vioçação de integridade no Banco de Dados, para o ID: " + id);
		}
	}
	

	private void copyDtoToEntity(ProductDTO dto, Product entity) {
		
		entity.setName(dto.getName());
		entity.setDescription(dto.getDescription());
		entity.setPrice(dto.getPrice());
		entity.setImgUrl(dto.getImgUrl());
		entity.setDate(dto.getDate());
		
		entity.getCategories().clear();
		for (CategoryDTO catDto : dto.getCategories()) {
			Category category = categoryRepository.getOne(catDto.getId());
			entity.getCategories().add(category);
		}
		
	}
}
