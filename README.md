# MapStruct — Mapeando seus DTOs para Model

A utilização de DTOs é bastante conhecida na comunidade de desenvolvimento, mas afinal, o que são? Onde vivem? Do que se alimentam?

Segundo a Wikipedia [1]:

Objeto de Transferência de Dados (do inglês, Data transfer object, ou simplesmente DTO), é um padrão de projeto de software usado para transferir dados entre subsistemas de um software. DTOs são frequentemente usados em conjunção com objetos de acesso a dados para obter dados de um banco de dados.
A diferença entre objetos de transferência de dados e objetos de negócio ou objetos de acesso a dados é que um DTO não possui comportamento algum, exceto o de armazenamento e obtenção de seus próprios dados. DTOs são objetos simples que não contêm qualquer lógica de negócio que requeira testes…

Vale ressaltar também, que hoje vivemos em um universo integrado por APIs REST, e uma boa prática é não expor suas entidades como payload afim de evitar ataques como Mass Assignment, conforme orientado nesse artigo do OWASP [2].

Existem diversos frameworks com o propósito de fazer o mapeamento de DTOs para as classes de negócio, porém em sua maioria, fazem uso de reflection, que possui aquela fama de performance já conhecida por todos.

Hoje quero apresentar um desses frameworks, o MapStruct [3], que não faz uso de reflection para mapear nossos DTOs, ao invés disso ele vai gerar uma implementação baseada em uma definição de interface que fizermos.

```
<dependency>
   <groupId>org.mapstruct</groupId>
   <artifactId>mapstruct</artifactId>
   <version>1.1.0.Final</version>
   <scope>compile</scope>
</dependency>
```

Vamos imaginar o seguinte relacionamento de Report e Fields conforme descrito abaixo.

```
@Entity
@Table(name = "repo_report")
@UuidGenerator(name = "report_uuid_gen")
@Multitenant
@TenantDiscriminatorColumn(name = "company_id")
public class Report {
    @Id
    @GeneratedValue(generator = "report_uuid_gen")
    @Column(name="report_uuid")
    private String uuid;
    private String name;
    @OneToMany
    private List fields;
    //Getters and Setters
    
}
@Entity
@Table(name = "repo_field")
@UuidGenerator(name = "FIELD_UUID_GEN")
@Multitenant
@TenantDiscriminatorColumn(name = "company_id")
public class Field {
    @Id
    @GeneratedValue(generator = "FIELD_UUID_GEN")
    @Column(name="field_uuid")
    private String uuid;
    private String name;
    @Enumerated(EnumType.STRING)
    private DataTypeEnum dataType;
    //Getters and Setters
}
```

…e seus respectivos DTOs…

```
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReportDTO {
    private String uuid;
    @NotNull
    private String name;
    
    private String userUuid;
    
    @NotNull
    private List fields;
   //Getters and Setters
}
@JsonIgnoreProperties(ignoreUnknown = true)
public class FieldDTO {
    private String uuid;
    @NotNull
    private String name;
    @NotNull
    private String type;
   //Getters and Setters
}
```

Podemos escrever nosso próprio mapeamento, usando construtores, getters e setters. Mas podemos delegar essa implementação ao MapStruct definindo uma interface.

```
public interface ReportMapper {
    Report toModel(ReportDTO dto);
    @Mapping(target = "anoutherField", ignore = true) // ignorar map
    ReportDTO toDto(Report model);
    @Mapping(source = "type", target = "dataType") // map
    Field fieldDtoToField(FieldDTO dto); //para o relacionamento List fields
    
    @InheritInverseConfiguration
    FieldDTO fieldToFieldDto(Field model); //para o relacionamento List fields
    
}
```

Para nosso exemplo, não implementei o FieldMapper, mas o conceito é o mesmo ;)
Perceba que descrevemos 4 métodos: toModel, toDto, fieldDtoToField e fieldToFieldDto. A anotação **@Mapping** permite definirmos qual atributo da classe sera mapeado para outro com nome diferente. Se os nomes forem iguais, não é necessário descreve-lo.
Para fazer o mapeamento contrário conforme o exemplo acima, usamos **@InheritInverseConfiguration**. Dessa forma não é necessário descrever o **@Mapping** inverso.

## 🚀 Utilização

A Documentação recomenda criar uma factory na interface.

```
CarMapper INSTANCE = Mappers.getMapper(CarMapper.class);
CarDto carDto = CarMapper.INSTANCE.carToCarDto(car);
```

Porém esta pratica não é recomendada e o Sonar vai ficar no seu pé. Para evitar isso, vamos criar nosso próprio factory genérico.

```
public class Mapper {
private Mapper(){
    }
public static  T factory(final Class clazz){
        return Mappers.getMapper(clazz);
    }
}
```

E para utilizar nosso mapper.

```
Report report = Mapper.factory(ReportMapper.class).toModel(reportDto);
```

### 📋 Injeção de Dependência

“Ahh! Mas eu uso Spring…” “Não gosto de ficar escrevendo Factories…”
Não seja por isso, podemos falar pro MapStruct gerar a implementação como um **@Component** do Spring usando **@Mapper(componentModel=”spring”)**

```
@Mapper(componentModel="spring")
public interface ReportMapper {
    Report toModel(ReportDTO dto);
    @Mapping(target = "anoutherField", ignore = true) // ignorar map
    ReportDTO toDto(Report model);
    @Mapping(source = "type", target = "dataType") // map
    Field fieldDtoToField(FieldDTO dto);
    
    @InheritInverseConfiguration
    FieldDTO fieldToFieldDto(Field model);
    
}
@Service
public class ReportServiceImpl implements ReportService {
    @Autowired
    private ReportRepository repository;
    @Autowired
    private ReportMapper mapper;
    //...
    @Override
    public Report save(ReportDTO dto) {
        return dao.save(mapper.toModel(dto));
    }
}
```

Para que o MapStruct gere as implementações, é necessário adicionar um Processador (mapstruct-processor) na etapa de compilação do projeto. O link de instalação [4] possui os exemplos de Maven, Gradle e Ant.

```
<plugin>
  <artifactId>maven-compiler-plugin</artifactId>
  <version>3.5.1</version>
  <configuration>
    <encoding>UTF-8</encoding>
    <source>${java.version}</source>
    <target>${java.version}</target>
    <annotationProcessorPaths>
      <path>
        <groupId>org.mapstruct</groupId>
        <artifactId>mapstruct-processor</artifactId>
        <version>${org.mapstruct.version}</version>
      </path>
    </annotationProcessorPaths>
  </configuration>
</plugin>
```

Sucesso….

Postman Request  

![console summary](img/postman.png)

## 🛠️ IntelliJ Idea mapstruct java: Internal error in the mapping processor: java.lang.NullPointerException


A solução é atualizar o MapStruct para a versão 1.4.1.Final ou posterior, veja este problema para mais detalhes.

Você também pode adicionar -Djps.track.ap.dependencies=false at File | Settings (Preferences on macOS) | Build, Execution, Deployment | Compiler | Build process VM options como solução alternativa.

![console summary](img/config.png)

## ✒️ Autores

*DEV-CAVE* - [dev-cave](https://medium.com/dev-cave/mapstruct-mapeando-seus-dtos-para-model-8bc362b628fe)

[1] https://pt.wikipedia.org/wiki/Objeto_de_Transfer%C3%AAncia_de_Dados

[2] https://www.owasp.org/index.php/Mass_Assignment_Cheat_Sheet

[3] http://mapstruct.org/

[4] http://mapstruct.org/documentation/installation/
