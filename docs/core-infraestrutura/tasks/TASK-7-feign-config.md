# TASK-7 — Criar FeignConfig

**Arquivo alvo:** `src/main/java/com/staroscky/motordecisao/core/config/FeignConfig.java` (novo)
**Referência SPEC:** Seções 5 (RF-06, RF-08), 8.6
**Depende de:** TASK-1
**Bloqueada por:** nenhuma

---

## Contexto

Esta classe habilita o escaneamento de interfaces `@FeignClient` em todo o projeto e registra o `FeignLogbookLogger` como o logger global do Feign. Sem isso, chamadas de saída não são logadas pelo Logbook e o `@EnableFeignClients` não está ativo.

## O que fazer

Criar a classe no pacote `com.staroscky.motordecisao.core.config`:

```java
package com.staroscky.motordecisao.core.config;

import feign.Logger;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.openfeign.FeignLogbookLogger;

@Configuration
@EnableFeignClients(basePackages = "com.staroscky.motordecisao")
public class FeignConfig {

    @Bean
    public Logger feignLogger(Logbook logbook) {
        return new FeignLogbookLogger(logbook);
    }

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }
}
```

## Notas de implementação

- `@EnableFeignClients` fica aqui (e não em `MotordecisaoApplication`) para manter o `core` como ponto único de configuração de infraestrutura — alinhado com a arquitetura vertical slice.
- `basePackages = "com.staroscky.motordecisao"` garante que todas as interfaces `@FeignClient` em qualquer slice sejam encontradas.
- O bean `Logbook` é auto-configurado pelo `logbook-spring-boot-starter` — não precisa ser criado manualmente.
- `Logger.Level.FULL` faz o Feign passar headers, body e metadados para o `FeignLogbookLogger`, que delega o logging ao Logbook. Sem `FULL`, o Logbook não recebe os dados completos da requisição/resposta.
- `FeignLogbookLogger` é marcado como `@API(status = EXPERIMENTAL)` no Logbook — monitorar o changelog em upgrades de versão.
- Os beans `feignLogger` e `feignLoggerLevel` são aplicados globalmente a todos os Feign clients. Para sobrescrever por cliente específico, usar `@FeignClient(configuration = MinhaConfigEspecifica.class)`.

## Critério de aceite

- [ ] Classe compila sem erros
- [ ] Aplicação sobe sem erros com `@EnableFeignClients` ativo
- [ ] Bean `FeignLogbookLogger` está presente no contexto Spring
- [ ] Bean `Logger.Level.FULL` está presente no contexto Spring
- [ ] Build e testes relevantes passam sem erros
