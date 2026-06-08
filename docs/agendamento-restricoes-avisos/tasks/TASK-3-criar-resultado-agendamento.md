# TASK-3 — Criar ResultadoAgendamento

**Arquivo alvo:** `avaliacao/contexto/ResultadoAgendamento.java` (criar)
**Referência SPEC:** Seções 8.2, RF-01, RF-02
**Depende de:** TASK-1
**Bloqueada por:** nenhuma

---

## Contexto

O bloco `agendamento` usa hoje `ResultadoViabilidade`, que tem `boolean valido` como campo mutado por efeito colateral. `ResultadoAgendamento` substitui esse modelo para o bloco de agendamento, com `isPodeAgendar()` sempre derivado da lista de restrições — eliminando a possibilidade de estado inconsistente. Também suporta `Aviso`, que não existe em `ResultadoViabilidade`.

## O que fazer

Criar `avaliacao/contexto/ResultadoAgendamento.java`:

```java
package com.staroscky.motordecisao.avaliacao.contexto;

import com.staroscky.motordecisao.avaliacao.modelo.Aviso;
import com.staroscky.motordecisao.avaliacao.modelo.restricao.Restricao;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ResultadoAgendamento {

    private final List<Restricao> restricoes = new ArrayList<>();
    private final List<Aviso> avisos         = new ArrayList<>();
    private LocalDate proximaDataDisponivel;

    public void adicionarRestricao(Restricao restricao) {
        restricoes.add(restricao);
    }

    public void adicionarAviso(Aviso aviso) {
        avisos.add(aviso);
    }

    public boolean isPodeAgendar() {
        return restricoes.isEmpty();
    }

    public boolean temProximaData() {
        return proximaDataDisponivel != null;
    }

    public List<Restricao> getRestricoes() {
        return restricoes;
    }

    public List<Aviso> getAvisos() {
        return avisos;
    }

    public LocalDate getProximaDataDisponivel() {
        return proximaDataDisponivel;
    }

    public void setProximaDataDisponivel(LocalDate proximaDataDisponivel) {
        this.proximaDataDisponivel = proximaDataDisponivel;
    }
}
```

## Notas de implementação

- `isPodeAgendar()` **nunca** deve se tornar um campo. O único meio de tornar o agendamento impossível é via `adicionarRestricao()`.
- `setProximaDataDisponivel()` é necessário para `ProximaData*Validador` — é o único setter da classe.
- Não há setter para `avisos` nem para `restricoes` — apenas os métodos `adicionar*()`.
- `LocalDate` requer `import java.time.LocalDate`.

## Critério de aceite

- [ ] `isPodeAgendar()` retorna `true` com lista vazia e `false` após `adicionarRestricao()`
- [ ] `temProximaData()` retorna `false` quando `proximaDataDisponivel` é `null`
- [ ] `adicionarAviso()` não afeta `isPodeAgendar()`
- [ ] Build compila sem erros
