# TASK-2 — Configurar application.yaml

**Arquivo alvo:** `src/main/resources/application.yaml` (existente)
**Referência SPEC:** Seções 4.1, 8.7
**Depende de:** TASK-1
**Bloqueada por:** nenhuma

---

## Contexto

O `application.yaml` atual tem apenas `spring.application.name`. Esta task ativa virtual threads no servidor Tomcat e configura o Logbook com formato JSON e obfuscação de headers sensíveis.

## O que fazer

Substituir o conteúdo atual por:

```yaml
spring:
  application:
    name: motordecisao
  threads:
    virtual:
      enabled: true

logbook:
  filter:
    enabled: true
  format:
    style: json
  obfuscate:
    headers:
      - Authorization
      - X-Auth-Token
```

## Notas de implementação

- `spring.threads.virtual.enabled: true` faz o Spring Boot configurar o Tomcat para usar virtual threads por requisição e o `SimpleAsyncTaskExecutorBuilder` com virtual threads habilitadas — ambos sem código adicional.
- `logbook.format.style: json` produz logs estruturados; usar `http` se preferir formato legível por humanos durante o desenvolvimento.
- Adicionar outros headers em `logbook.obfuscate.headers` conforme os slices forem evoluindo (ex: `X-Api-Key`, tokens de sessão).
- A confirmação de virtual threads no startup aparece nos logs: procurar por `TomcatVirtualThreadsWebServerFactoryCustomizer`.

## Critério de aceite

- [ ] Aplicação sobe sem erros com a nova configuração
- [ ] Log de startup contém referência ao `TomcatVirtualThreadsWebServerFactoryCustomizer` (confirma virtual threads no Tomcat)
- [ ] `logbook.filter.enabled: true` está presente (Logbook ativo por padrão)
- [ ] Build e testes relevantes passam sem erros
