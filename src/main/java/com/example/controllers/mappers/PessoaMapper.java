package com.example.controllers.mappers;

import com.example.controllers.request.PessoaRequest;
import com.example.controllers.response.PessoaResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {
    EnderecoMapper.class
})
public interface PessoaMapper {
  PessoaResponse de(final PessoaRequest pessoaRequest);
}
