Visão Geral

A FairPay API é uma API RESTful desenvolvida em Java com Spring Boot, projetada para gerenciar despesas compartilhadas em grupos. Ela oferece funcionalidades robustas para autenticação de usuários, criação e gerenciamento de grupos, registro de despesas e divisão equitativa entre os membros, além de permitir a liquidação de débitos. A aplicação segue as melhores práticas de desenvolvimento, utilizando PostgreSQL como banco de dados, Flyway para migrações de schema, autenticação JWT para segurança e Swagger UI para documentação interativa da API.

Funcionalidades Principais

•
Autenticação e Autorização Segura: Implementa autenticação baseada em JSON Web Tokens (JWT) para proteger os endpoints da API, garantindo que apenas usuários autorizados possam acessar e manipular os recursos.

•
Gerenciamento de Usuários: Permite o registro e login de usuários, com perfis individuais para acompanhamento de despesas.

•
Gerenciamento de Grupos: Usuários podem criar grupos para organizar suas despesas compartilhadas, adicionar e remover membros.

•
Registro de Despesas: Facilita o registro de novas despesas dentro de um grupo, com detalhes como descrição, valor total e data.

•
Divisão de Despesas: Suporta a divisão equitativa de despesas entre os membros do grupo, com lógica para lidar com centavos residuais de forma justa.

•
Liquidação de Débitos: Funcionalidade para marcar despesas como liquidadas, auxiliando no controle financeiro e na resolução de saldos entre os membros.

•
Documentação Interativa da API: Integrada com Swagger UI (SpringDoc OpenAPI), fornecendo uma interface amigável para explorar os endpoints da API, testar requisições e entender os modelos de dados.

•
Persistência de Dados: Utiliza PostgreSQL para armazenamento de dados, garantindo confiabilidade e escalabilidade.

•
Migrações de Banco de Dados: Gerencia o versionamento e as migrações do schema do banco de dados com Flyway, facilitando o desenvolvimento e a implantação.

Tecnologias Utilizadas

•
Java 17: Linguagem de programação.

•
Spring Boot 3.x: Framework para construção de aplicações Java robustas e escaláveis.

•
spring-boot-starter-web: Para construção de APIs RESTful.

•
spring-boot-starter-data-jpa: Para persistência de dados com JPA e Hibernate.

•
spring-boot-starter-security: Para segurança da aplicação, incluindo autenticação e autorização.

•
spring-boot-starter-validation: Para validação de dados de entrada.



•
PostgreSQL: Banco de dados relacional.

•
Flyway: Ferramenta de migração de banco de dados.

•
JJWT (Java JWT): Biblioteca para implementação de JSON Web Tokens.

•
SpringDoc OpenAPI (Swagger UI): Para geração automática e interativa da documentação da API.

•
Lombok: Biblioteca para reduzir código boilerplate em Java.

•
Maven: Ferramenta de automação de build e gerenciamento de dependências.

Estrutura do Projeto

O projeto segue uma estrutura de pacotes organizada, comum em aplicações Spring Boot:

•
com.fairpay.FairPayApplication: Classe principal da aplicação.

•
com.fairpay.config: Classes de configuração para segurança (JWT), OpenAPI e tratamento de exceções.

•
com.fairpay.controller: Controladores REST para Auth, Expense, Group e User.

•
com.fairpay.exception: Classes de exceção personalizadas e handler global.

•
com.fairpay.model: Contém DTOs (Data Transfer Objects) para requisições e respostas, e entidades JPA para o mapeamento do banco de dados.

•
com.fairpay.model.mapper: Mappers para converter entre entidades e DTOs.

•
com.fairpay.repository: Interfaces de repositório para acesso a dados, estendendo JpaRepository.

•
com.fairpay.service: Camada de serviço contendo a lógica de negócio para Auth, Expense, Group e User.

Como Executar o Projeto

Pré-requisitos

•
Java 17 ou superior

•
Maven

•
Docker e Docker Compose (recomendado para PostgreSQL)

Configuração do Banco de Dados (com Docker Compose)

1.
Navegue até a raiz do projeto.

2.
Execute o seguinte comando para iniciar o serviço PostgreSQL:

Bash


docker-compose up -d postgres



Isso iniciará um contêiner PostgreSQL e criará o banco de dados fairpay com o usuário fairpay e senha fairpay, conforme configurado em src/main/resources/application.yml.



Variáveis de Ambiente

Crie um arquivo .env na raiz do projeto ou defina as seguintes variáveis de ambiente:

•
SPRING_PROFILES_ACTIVE: dev (ou test para ambiente de teste)

•
DB_URL: jdbc:postgresql://localhost:5432/fairpay

•
DB_USERNAME: fairpay

•
DB_PASSWORD: fairpay

•
JWT_SECRET: Uma string secreta forte de pelo menos 32 caracteres (ex: your-super-secret-jwt-key-with-at-least-32-chars)

•
JWT_EXPIRATION: Tempo de expiração do token em milissegundos (ex: 86400000 para 24 horas)

•
SERVER_PORT: Porta em que a API será executada (ex: 8080)

Executando a Aplicação

1.
Compile o projeto:

Bash


mvn clean install





2.
Execute a aplicação:

Bash


mvn spring-boot:run





A API estará disponível em http://localhost:8080 (ou na porta configurada ).

Documentação da API (Swagger UI)

Após iniciar a aplicação, acesse a documentação interativa da API em:

http://localhost:8080/swagger-ui.html

Testes

O projeto inclui testes unitários e de integração. Para executá-los:

Bash


mvn test



Contribuição

Contribuições são bem-vindas! Sinta-se à vontade para abrir issues e pull requests.

