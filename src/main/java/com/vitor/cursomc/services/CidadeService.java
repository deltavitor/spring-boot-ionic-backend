package com.vitor.cursomc.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vitor.cursomc.domain.Cidade;
import com.vitor.cursomc.repositories.CidadeRepository;

@Service
public class CidadeService {
	
	@Autowired
	private CidadeRepository repo;
	
	public List<Cidade> findCidades(Integer estadoId) {
		return repo.findAllByEstadoId(estadoId);
	}
	
}
