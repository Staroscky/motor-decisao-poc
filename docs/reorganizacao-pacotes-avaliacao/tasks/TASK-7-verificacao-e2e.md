# TASK-7 — Verificação e2e da reorganização

**Arquivo alvo:** nenhum (verificação)
**Referência SPEC:** Seção 13, Seção 14 (critérios de aceite)
**Depende de:** TASK-1, TASK-2, TASK-3, TASK-4, TASK-5, TASK-6
**Bloqueada por:** todas as tasks anteriores

---

## Contexto

Verificação final de que a reorganização está completa, correta e sem regressão — conforme os critérios de aceite da SPEC.

## O que fazer

1. Rodar `mvn test` completo e confirmar que todos os testes passam.
2. Verificar estrutura final dos pacotes afetados:

```
# modelo/ deve conter Limite
find src/main -path "*/avaliacao/modelo/Limite.java"

# contexto/ NÃO deve conter Limite
find src/main -path "*/avaliacao/contexto/Limite.java"  # deve retornar vazio

# validador/agendamento/ deve conter ValidadorAgendamento
find src/main -path "*/validador/agendamento/ValidadorAgendamento.java"

# pipeline/agendamento/ NÃO deve conter ValidadorAgendamento
find src/main -path "*/pipeline/agendamento/ValidadorAgendamento.java"  # deve retornar vazio

# validador/agendamento/pix/ deve ter 4 validators
find src/main -path "*/validador/agendamento/pix/*.java"

# pipeline/agendamento/pix/ deve ter apenas PipelineAgendamentoPix.java
find src/main -path "*/pipeline/agendamento/pix/*.java"

# Confirmar zero referências a pipeline.agendamento em validators e testes
grep -r "pipeline\.agendamento\.ValidadorAgendamento\|pipeline\.agendamento\.pix\.\|pipeline\.agendamento\.ted\.\|pipeline\.agendamento\.tef\." src/
```

3. Verificar critérios de aceite da SPEC §14 um por um.

## Critério de aceite

- [ ] `mvn test` passa sem regressão
- [ ] `modelo/Limite.java` existe; `contexto/Limite.java` não existe
- [ ] `validador/agendamento/ValidadorAgendamento.java` existe; `pipeline/agendamento/ValidadorAgendamento.java` não existe
- [ ] `validador/agendamento/pix/` contém 4 validators; `pipeline/agendamento/pix/` contém apenas `PipelineAgendamentoPix.java`
- [ ] `validador/agendamento/ted/` contém 3 validators; `pipeline/agendamento/ted/` contém apenas `PipelineAgendamentoTed.java`
- [ ] `validador/agendamento/tef/` contém 3 validators; `pipeline/agendamento/tef/` contém apenas `PipelineAgendamentoTef.java`
- [ ] `grep -r "pipeline.agendamento" src/` retorna apenas `Pipeline*.java` — zero validators, zero testes
