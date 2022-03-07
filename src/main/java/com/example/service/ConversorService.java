package com.example.service;

import com.example.controllers.mappers.PessoaMapper;
import com.example.controllers.request.PessoaRequest;
import com.example.controllers.response.PessoaResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ConversorService {

  @Autowired
  private PessoaMapper pessoaMapper;

  public PessoaResponse converte(final PessoaRequest pessoaRequest) {
    return pessoaMapper.de(pessoaRequest);
  }
}
