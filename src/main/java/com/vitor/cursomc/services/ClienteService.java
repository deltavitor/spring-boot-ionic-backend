package com.vitor.cursomc.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vitor.cursomc.domain.Cidade;
import com.vitor.cursomc.domain.Cliente;
import com.vitor.cursomc.domain.Endereco;
import com.vitor.cursomc.domain.enums.Perfil;
import com.vitor.cursomc.domain.enums.TipoCliente;
import com.vitor.cursomc.dto.ClienteDTO;
import com.vitor.cursomc.dto.ClienteNewDTO;
import com.vitor.cursomc.repositories.ClienteRepository;
import com.vitor.cursomc.repositories.EnderecoRepository;
import com.vitor.cursomc.security.UserSS;
import com.vitor.cursomc.services.exceptions.AuthorizationException;
import com.vitor.cursomc.services.exceptions.DataIntegrityException;
import com.vitor.cursomc.services.exceptions.ObjectNotFoundException;

@Service
public class ClienteService {
	
	@Autowired
	private ClienteRepository repo;
	@Autowired
	private EnderecoRepository enderecoRepository;
	@Autowired
	private BCryptPasswordEncoder pe;
	
	public Cliente find(Integer id) {
		
		UserSS user = UserService.authenticated();
		if ((user == null) || (!user.hasRole(Perfil.ADMIN)) && (!id.equals(user.getId()))) {
			throw new AuthorizationException("Acesso negado");
		}
		
		Optional<Cliente> obj = repo.findById(id);
		return obj.orElseThrow(() -> new ObjectNotFoundException(
				 "Objeto não encontrado! Id: " + id + ", Tipo: " + Cliente.class.getName())); 
	}
	
	@Transactional
	public Cliente insert(Cliente obj) {
		obj.setId(null);
		obj = repo.save(obj);
		enderecoRepository.saveAll(obj.getEnderecos());
		return obj;
	}
	
	public Cliente update(Cliente obj) {
		Cliente newObj = find(obj.getId());
		updateData(newObj, obj);
		return repo.save(newObj);
	}
	
	public void delete(Integer id) {
		find(id);
		try {
			repo.deleteById(id);
		}
		catch (DataIntegrityViolationException e) {
			throw new DataIntegrityException("Não é possível excluir porque há entidades relacionadas");
		}
	}
	
	public List<Cliente> findAll() {
		return repo.findAll();
	}
	
	public Cliente findByEmail(String email) {
		
		UserSS user = UserService.authenticated();
		if ((user == null) || (!user.hasRole(Perfil.ADMIN) && (!email.equals(user.getUsername())))) {
			throw new AuthorizationException("Acesso negado");
		}
		
		Cliente obj = repo.findByEmail(email);
		if (obj == null) {
			throw new ObjectNotFoundException("Objeto não encontrado! Id: " + user.getId() + ", Tipo: " + Cliente.class.getName());
		}
		return obj;
	}
	
	
	public Page<Cliente> findPage(Integer page, Integer linesPerPage, String orderBy, String direction) {
		PageRequest pageRequest = PageRequest.of(page, linesPerPage, Direction.valueOf(direction), orderBy);
		return repo.findAll(pageRequest);
	}
	
	public Cliente fromDTO(ClienteDTO objDto) {
		return new Cliente(objDto.getId(), objDto.getNome(), objDto.getEmail(), null, null, null);
	}
	
	public Cliente fromDTO(ClienteNewDTO objDto) {
		Cliente cli = new Cliente(null, objDto.getNome(), objDto.getEmail(), objDto.getCpfOuCnpj(), TipoCliente.toEnum(objDto.getTipo()), pe.encode(objDto.getSenha()));
		Cidade cid = new Cidade(objDto.getCidadeId(), null, null);
		Endereco end = new Endereco(null, objDto.getLogradouro(), objDto.getNumero(), objDto.getComplemento(), objDto.getBairro(), objDto.getCep(), cli, cid);
		cli.getEnderecos().add(end);
		cli.getTelefones().add(objDto.getTelefone1());
		if (objDto.getTelefone2() != null) {
			cli.getTelefones().add(objDto.getTelefone2());
		}
		if (objDto.getTelefone3() != null) {
			cli.getTelefones().add(objDto.getTelefone3());
		}
		return cli;
	}
	
	private void updateData(Cliente newObj, Cliente obj) {
		newObj.setNome(obj.getNome());
		newObj.setEmail(obj.getEmail());
	}
	
}
