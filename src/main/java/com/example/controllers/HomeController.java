package com.example.controllers;

import com.example.controllers.request.PessoaRequest;
import com.example.controllers.response.PessoaResponse;
import com.example.service.ConversorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/home")
public class HomeController {

  @Autowired
  private ConversorService conversorService;

  /*

POST http://localhost:8080/home

  {
    "nome": "Teste",
    "idade": 48,
    "enderecos": [{
      "rua": "Rua 3",
      "numero": 27,
      "cidade": "Londrina"
    }]
  }

  -----------------------------------------------------------------------------------

Resturn

  {
      "nome": "Teste",
      "idade": 48,
      "enderecos": [
          {
              "logradouro": "Rua 3",
              "numero": "27",
              "cidade": "Londrina"
          }
      ]
  }

   */
  @PostMapping
  public ResponseEntity<PessoaResponse> create(@RequestBody final PessoaRequest pessoaRequest) {
    final PessoaResponse pessoaResponse = conversorService.converte(pessoaRequest);
    return ResponseEntity.ok(pessoaResponse);
  }
}
