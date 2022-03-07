package com.example.controllers.mappers;

import com.example.controllers.request.EnderecoRequest;
import com.example.controllers.response.EnderecoResponse;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EnderecoMapper {

  @Mapping(target = "logradouro", source = "rua")
  EnderecoResponse de(final EnderecoRequest enderecoRequest);

  @InheritInverseConfiguration
  EnderecoRequest de(final EnderecoResponse enderecoResponse);

  List<EnderecoResponse> deRequest(final List<EnderecoRequest> enderecoRequests);

  @InheritConfiguration
  List<EnderecoRequest> deResponse(final List<EnderecoResponse> enderecoResponses);
}
