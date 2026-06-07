# TASK-7 — Validadores do PipelineAgendamento

**Arquivo alvo:** `src/main/java/com/staroscky/motordecisao/avaliacao/validador/agendamento/` (novo)
**Referência SPEC:** Seção 8.5
**Depende de:** TASK-6 (interface `Validador`), TASK-3 (modelo interno)
**Bloqueada por:** nenhuma

---

## Contexto

Os dois validadores do `PipelineAgendamento` têm comportamento mais sofisticado no `suporta()`: `PermiteAgendamentoValidador` executa somente para PIX em checkins de QR Code; `ProximaDataValidador` executa somente quando a data selecionada já foi marcada como inviável pelo pipeline anterior — lendo o resultado do contexto.

## O que fazer

**`PermiteAgendamentoValidador.java`**
```java
@Component
public class PermiteAgendamentoValidador implements Validador {

    @Override
    public boolean suporta(AvaliacaoContexto contexto, Instrumento instrumento) {
        return instrumento == Instrumento.PIX
            && contexto.getTipoCheckin() == TipoCheckin.QRCODE;
    }

    @Override
    public void validar(AvaliacaoContexto contexto, Instrumento instrumento,
                        ResultadoViabilidade resultado) {
        QrcodeAvaliacaoRequest req = (QrcodeAvaliacaoRequest) contexto.getRequest();
        if (!tipoPermiteAgendamento(req.qrcode().tipo())) {
            resultado.adicionarRestricao(new RestricaoQrcode(
                "QR_CODE_NAO_PERMITE",
                new RestricaoQrcode.ContextoQrcode(req.qrcode().tipo())
            ));
        }
    }

    private boolean tipoPermiteAgendamento(String tipoQrcode) {
        // TODO: definir quais tipos permitem agendamento (ex.: "COBV" sim, "COB" não)
        return false; // placeholder conservador — nenhum tipo permite por enquanto
    }
}
```

**`ProximaDataValidador.java`**
```java
@Component
public class ProximaDataValidador implements Validador {

    @Override
    public boolean suporta(AvaliacaoContexto contexto, Instrumento instrumento) {
        // executa apenas quando o pipeline anterior marcou dataSelecionada como inviável
        return !contexto.getResultado(instrumento).getDataSelecionada().isValido();
    }

    @Override
    public void validar(AvaliacaoContexto contexto, Instrumento instrumento,
                        ResultadoViabilidade resultado) {
        // TODO: buscar próxima data disponível para o instrumento
        // resultado.setProximaDataDisponivel(proxima); — quando o campo for adicionado ao modelo
    }
}
```

## Notas de implementação

- `PermiteAgendamentoValidador.validar()` faz cast direto para `QrcodeAvaliacaoRequest` — esse cast é seguro porque `suporta()` só retorna `true` quando `tipoCheckin == QRCODE`, o que implica que o request foi desserializado como `QrcodeAvaliacaoRequest`
- `ProximaDataValidador.suporta()` lê o estado do pipeline anterior via `contexto.getResultado(instrumento).getDataSelecionada()` — esse é o padrão de comunicação entre pipelines definido na SPEC; não passar parâmetros diretamente entre pipelines
- A lista de tipos de QR Code que permitem agendamento é regra de negócio — deixar como TODO até definição

## Critério de aceite

- [ ] `PermiteAgendamentoValidador.suporta()` retorna `true` somente para `instrumento=PIX` e `tipoCheckin=QRCODE`
- [ ] `PermiteAgendamentoValidador.suporta()` retorna `false` para `instrumento=TED` mesmo com `tipoCheckin=QRCODE`
- [ ] `ProximaDataValidador.suporta()` retorna `true` quando `dataSelecionada.isValido() == false`
- [ ] `ProximaDataValidador.suporta()` retorna `false` quando `dataSelecionada.isValido() == true`
- [ ] `PermiteAgendamentoValidador.validar()` adiciona restrição `QR_CODE_NAO_PERMITE` com `contexto.tipo` correto
- [ ] Build sem erros: `mvn verify`
