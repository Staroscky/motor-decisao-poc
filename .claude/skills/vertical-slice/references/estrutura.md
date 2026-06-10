# Estrutura de pastas

## Layout canônico

```
/core
    /ConfigGeral.kt                 # configuração e utilitários transversais
/modulo                             # uma fatia vertical (feature/módulo)
    /controller                     # entrada da aplicação (REST, SDUI, orquestração de I/O)
    /domain                         # modelos e regras de domínio do módulo
    /service                        # lógica de aplicação / casos de uso
    /integration                    # comunicação com o mundo externo
        /ApiExternaClient.kt        # client da API externa (ex.: Feign)
        /data                       # DTOs da API externa (contrato externo, não o domínio)
            /DominioDaApiExterna.kt
    Modulo.kt                       # interface pública: contrato com outros módulos
```

## Papel de cada parte

### `core/`
Tudo que é genuinamente transversal: configuração geral, beans compartilhados, utilitários usados por mais de uma fatia. Não contém regra de negócio de feature. Se algo só serve a um módulo, não pertence aqui.

### Raiz do módulo (ex.: `motorDecisao/`)
A fatia vertical em si. Autocontida: agrupa tudo que a feature precisa. Na raiz fica a **interface pública** (`MotorDecisao.kt`), que é o único ponto que outros módulos enxergam.

### `controller/`
Borda de entrada do módulo: endpoints REST, mapeamento SDUI, validação de entrada e tradução do mundo externo para chamadas de `service`. Não contém regra de negócio.

### `domain/`
Modelos, value objects e regras de domínio do módulo. É o núcleo estável e não conhece detalhes de transporte nem de APIs externas.

### `service/`
Lógica de aplicação e orquestração dos casos de uso. Coordena `domain` e `integration`. É onde tipicamente acontece a conversão entre DTOs externos e modelos de domínio (ou isso fica em `integration`).

### `integration/`
Toda comunicação com sistemas externos. O client (`ApiExternaClient.kt`) e, em `integration/data/`, os DTOs que representam o **contrato da API externa** — separados dos modelos de `domain`. Isso isola mudanças do fornecedor externo do núcleo do módulo.

### Interface pública (`Modulo.kt`)
O contrato que outros módulos usam para falar com este. Expõe só o necessário; esconde controller, service, domain e integration. Trocar a implementação interna não quebra os consumidores.

## Onde colocar cada arquivo — referência rápida

- Endpoint novo / mapeamento de resposta → `controller/`
- Modelo de negócio, regra, value object → `domain/`
- Caso de uso, orquestração → `service/`
- Client de API externa (Feign etc.) → `integration/`
- DTO que espelha o JSON de uma API externa → `integration/data/`
- Config compartilhada entre módulos → `core/`
- Algo que outro módulo precisa chamar → expor pela interface pública na raiz
