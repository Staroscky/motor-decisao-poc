# TASK-6 — Interface Validador e validadores do PipelineDataSelecionada

**Arquivo alvo:** `src/main/java/com/staroscky/motordecisao/avaliacao/validador/` (novo)
**Referência SPEC:** Seções 8.5, 5
**Depende de:** TASK-1, TASK-3
**Bloqueada por:** nenhuma

---

## Contexto

A interface `Validador` é o contrato de todos os validadores do slice. Os cinco validadores desta task compõem o `PipelineDataSelecionada` — cada um declara autonomamente via `suporta()` em quais condições deve executar. A lógica de negócio interna (horários reais, limites, chamadas upstream) fica como placeholder: o foco aqui é a estrutura correta de `suporta()` e a assinatura de `validar()`.

## O que fazer

**`Validador.java`** em `com.staroscky.motordecisao.avaliacao.validador`:
```java
public interface Validador {
    boolean suporta(AvaliacaoContexto contexto, Instrumento instrumento);
    void validar(AvaliacaoContexto contexto, Instrumento instrumento, ResultadoViabilidade resultado);
}
```

**`validador/instituicao/StatusInstituicaoValidador.java`**
```java
@Component
public class StatusInstituicaoValidador implements Validador {

    @Override
    public boolean suporta(AvaliacaoContexto contexto, Instrumento instrumento) {
        return true; // executa para todos os instrumentos
    }

    @Override
    public void validar(AvaliacaoContexto contexto, Instrumento instrumento,
                        ResultadoViabilidade resultado) {
        // TODO: consultar status operacional da instituição destino
        // se indisponível: resultado.adicionarRestricao(new RestricaoGenerica("INSTITUICAO_INDISPONIVEL"));
    }
}
```

**`validador/instituicao/LimiteInstituicaoValidador.java`**
```java
@Component
public class LimiteInstituicaoValidador implements Validador {

    @Override
    public boolean suporta(AvaliacaoContexto contexto, Instrumento instrumento) {
        return true;
    }

    @Override
    public void validar(AvaliacaoContexto contexto, Instrumento instrumento,
                        ResultadoViabilidade resultado) {
        // TODO: verificar limite da instituição para o instrumento
    }
}
```

**`validador/data/HorarioPermitidoValidador.java`**
```java
@Component
public class HorarioPermitidoValidador implements Validador {

    @Override
    public boolean suporta(AvaliacaoContexto contexto, Instrumento instrumento) {
        return contexto.isOnline(); // só executa para data atual (não futura)
    }

    @Override
    public void validar(AvaliacaoContexto contexto, Instrumento instrumento,
                        ResultadoViabilidade resultado) {
        // TODO: verificar janela de horário permitido para o instrumento
        // se fora da janela: resultado.adicionarRestricao(new RestricaoGenerica("FORA_HORARIO_PERMITIDO"));
    }
}
```

**`validador/data/LimiteDiarioValidador.java`**
```java
@Component
public class LimiteDiarioValidador implements Validador {

    @Override
    public boolean suporta(AvaliacaoContexto contexto, Instrumento instrumento) {
        return true;
    }

    @Override
    public void validar(AvaliacaoContexto contexto, Instrumento instrumento,
                        ResultadoViabilidade resultado) {
        // TODO: verificar limite diário do instrumento
    }
}
```

**`validador/data/DataValidaValidador.java`**
```java
@Component
public class DataValidaValidador implements Validador {

    @Override
    public boolean suporta(AvaliacaoContexto contexto, Instrumento instrumento) {
        return true;
    }

    @Override
    public void validar(AvaliacaoContexto contexto, Instrumento instrumento,
                        ResultadoViabilidade resultado) {
        // TODO: verificar se a data é válida para o instrumento (dia útil, etc.)
    }
}
```

## Notas de implementação

- Todos os validadores são `@Component` — o pipeline os injeta por construtor na ordem desejada (não por `@Autowired List<Validador>`, que não garante ordem)
- A lógica interna (TODOs) é preenchida em sprints posteriores, conforme regras de negócio forem definidas
- `suporta()` nunca deve lançar exceção — deve sempre retornar `true` ou `false`

## Critério de aceite

- [ ] `HorarioPermitidoValidador.suporta()` retorna `false` para data futura e `true` para data atual
- [ ] Os demais quatro validadores retornam `true` em `suporta()` para qualquer contexto
- [ ] Nenhum `validar()` lança exceção nos placeholders
- [ ] Build sem erros: `mvn verify`
