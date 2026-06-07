# TASK-1 — Adicionar dependências no pom.xml

**Arquivo alvo:** `pom.xml` (existente)
**Referência SPEC:** Seções 4.1, 8.8
**Depende de:** nenhuma
**Bloqueada por:** nenhuma

---

## Contexto

O `pom.xml` atual possui apenas `spring-boot-starter-web` e `spring-boot-starter-test`. Esta task adiciona o Spring Cloud BOM e as três dependências necessárias para OpenFeign e Logbook. Todas as tasks Java do `core` dependem desta.

## O que fazer

1. Adicionar o bloco `<dependencyManagement>` com o Spring Cloud BOM:

```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>org.springframework.cloud</groupId>
      <artifactId>spring-cloud-dependencies</artifactId>
      <version>2025.0.2</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>
```

2. Adicionar as três dependências em `<dependencies>`:

```xml
<dependency>
  <groupId>org.springframework.cloud</groupId>
  <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
<dependency>
  <groupId>org.zalando</groupId>
  <artifactId>logbook-spring-boot-starter</artifactId>
  <version>4.0.4</version>
</dependency>
<dependency>
  <groupId>org.zalando</groupId>
  <artifactId>logbook-openfeign</artifactId>
  <version>4.0.4</version>
</dependency>
```

## Notas de implementação

- `spring-cloud-starter-openfeign` não precisa de versão explícita — é gerenciada pelo BOM `2025.0.2`.
- `logbook-spring-boot-starter` e `logbook-openfeign` precisam de versão explícita pois o Logbook não está no Spring Cloud BOM.
- Spring Cloud 2025.0.2 é a versão compatível com Spring Boot 3.5.x (release train Northfields).
- Após a alteração, rodar `mvn dependency:tree` para confirmar que não há conflito de versões.

## Critério de aceite

- [ ] `mvn verify` passa sem erros após as alterações
- [ ] `mvn dependency:tree` mostra `spring-cloud-starter-openfeign`, `logbook-spring-boot-starter` e `logbook-openfeign` sem conflitos
- [ ] Nenhuma outra dependência foi removida ou alterada
