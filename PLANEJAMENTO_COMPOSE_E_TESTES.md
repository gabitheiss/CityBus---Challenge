# Planejamento — Migração para Jetpack Compose + Cobertura de Testes 100%

> Documento de planejamento. **Nenhum código foi alterado.** Serve como roteiro para a refatoração do app **CityBus** (visualização de ônibus de SP via API Olho Vivo / SPTrans).

---

## 1. Estado atual

### Arquitetura
- **Padrão:** MVVM + Repository, com DI via **Koin** (3.3.3).
- **UI:** 100% **XML + View Binding** (não há Compose ativo).
- **Async:** Coroutines (`viewModelScope`), estado exposto com **LiveData**.
- **Rede:** Retrofit + Gson + OkHttp logging.
- **Mapa:** Google Maps (`play-services-maps`) com clustering, encapsulado em `CityBusCustomMap`.
- **SDK:** min 26, target/compile 34, Kotlin 1.9.0, JVM target 1.8.

### Telas (View system)
| Tela | Tipo | Layout XML |
|------|------|-----------|
| `SplashActivity` | Activity | `activity_splash.xml` (Lottie + permissões) |
| `BusesPositionMapActivity` | Activity | `activity_maps.xml` (mapa + botões) |
| `SearchActivity` | Activity (container) | `activity_search.xml` |
| `SearchLinesFragment` | Fragment | `fragment_search.xml` (busca + RecyclerView) |
| `BusesLineFragment` | Fragment | `fragment_buses_line.xml` (mapa por linha) |

### Camada de dados
- **ViewModels (3):** `SearchLinesViewModel`, `BusesPositionViewModel`, `BusesLineViewModel`.
- **Repositories (2):** `SearchLinesRepository(Impl)`, `BusesPositionRepository(Impl)` — wrappers finos sobre o `BusesService`.
- **Service (1):** `BusesService` (Retrofit) — 4 endpoints (Autenticar, Posicao, Linha/Buscar, Posicao/Linha).
- **Models (3):** `BusesLines`, `BusesPosition`, `BusesPositionToLine`.

### Testes existentes
- 3 testes unitários **apenas de inicialização** (`assertNotNull(viewModel)`) — sem cobertura de comportamento.
- 1 teste instrumentado boilerplate (`ExampleInstrumentedTest`).
- **Cobertura real de lógica: ~0%.**

### Dívidas técnicas que bloqueiam testes/Compose
Identificadas nos 3 ViewModels (código idêntico duplicado):
1. **TOKEN hardcoded** e **COOKIE como `companion object` estático mutável** → estado global compartilhado entre instâncias, impossível isolar em teste e não thread-safe.
2. **Lógica de autenticação duplicada** nos 3 ViewModels.
3. **`println` no lugar de log** — ruído e nada testável.
4. **Sem tratamento de erro** (só 200/401; rede caindo, timeout, etc. são ignorados).
5. **Estado em `LiveData`** sem representação de loading/erro/empty.
6. **Cookie manual** em vez de `Interceptor`/`CookieJar` do OkHttp.

> **Observação importante:** o `gradle/libs.versions.toml` **já declara** as dependências do Compose (`composeBom 2023.08.00`, `activity-compose`, `material3`, `ui-test-junit4`, `ui-test-manifest`, `ui-tooling`) — elas só **não estão referenciadas** no `app/build.gradle.kts`. A migração não exige adicionar libs novas, apenas wire-up.

---

## 2. Objetivos

1. Migrar toda a camada de UI de **XML/View Binding → Jetpack Compose**.
2. Modernizar estado: **LiveData → `StateFlow` + UiState selado** (loading/success/error/empty), pré-requisito para testar e para Compose.
3. Eliminar a dívida técnica que impede testabilidade (estado estático, auth duplicada).
4. Atingir **100% de cobertura de testes unitários** na lógica testável (ViewModels, Repositories, mappers, lógica de auth, UiState), validada por **JaCoCo**.
5. Cobrir a UI Compose com testes de instrumentação (`composeTestRule`) — para as regras de UI testáveis (visibilidade, cliques, estados de tela).

> **Nota sobre "100%":** mapa do Google (`CityBusCustomMap`, render de cluster), `Activity`/`Application` puramente de bootstrap e código gerado serão **excluídos da métrica via filtros do JaCoCo** — são naturalmente não-testáveis em unit test. O "100%" se aplica à lógica de negócio/apresentação. Os filtros de exclusão estão definidos na Fase 5.

---

## 3. Estratégia geral

Refatorar **antes** de migrar a UI, e testar **junto** com cada refatoração. Ordem deliberada:

```
Fase 0  Infra de testes + JaCoCo  (mede o baseline)
Fase 1  Refatorar data/auth/estado (destrava testabilidade)
Fase 2  Testes unitários 100% da camada não-UI
Fase 3  Habilitar Compose no build
Fase 4  Migrar telas XML → Compose (uma a uma)
Fase 5  Testes de UI Compose + fechar a meta de cobertura
Fase 6  Limpeza (remover XML, ViewBinding, libs mortas)
```

Cada fase é mergeável de forma independente — o app continua funcionando ao fim de cada uma.

---

## 4. Fase 0 — Infraestrutura de testes e medição

**Objetivo:** conseguir medir cobertura antes de começar, para evitar "achismo".

- Adicionar dependências de teste ao `app/build.gradle.kts` (já no catálogo ou a adicionar):
  - `kotlinx-coroutines-test` (controlar `viewModelScope` / `TestDispatcher`).
  - `androidx.arch.core:core-testing` (`InstantTaskExecutorRule`) — enquanto ainda houver LiveData.
  - `app.cash.turbine:turbine` — para testar `StateFlow`.
  - `mockk` (já presente) como mock principal; padronizar (hoje há mistura Mockito + MockK).
- Aplicar o plugin **JaCoCo** e criar task `jacocoTestReport` com filtros de exclusão (ver Fase 5).
- Criar regra utilitária `MainDispatcherRule` (JUnit) para trocar o `Dispatchers.Main` por um `TestDispatcher`.
- Rodar baseline: `./gradlew testDebugUnitTest jacocoTestReport`.

**Entregável:** relatório JaCoCo gerado + regra de dispatcher reusável.

---

## 5. Fase 1 — Refatoração da camada de dados / estado (pré-requisito)

> Sem isto, não há como atingir 100% de forma confiável.

1. **Extrair TOKEN/COOKIE do estado estático:**
   - Criar `SessionManager` (ou `AuthRepository`) injetável que guarda o cookie/sessão.
   - Token vai para `BuildConfig`/secrets, não hardcoded no VM.
2. **Centralizar autenticação:**
   - Mover o fluxo `toAuthenticate` + retry para um único ponto (interceptor OkHttp **ou** `AuthRepository`), removendo a tripla duplicação.
   - Ideal: `Authenticator`/`Interceptor` do OkHttp cuida do 401 + reenvio do cookie automaticamente → ViewModels deixam de conhecer auth.
3. **Introduzir `UiState` selado** por tela:
   ```
   sealed interface SearchUiState {
       data object Idle : SearchUiState
       data object Loading : SearchUiState
       data class Success(val lines: List<BusesLines>) : SearchUiState
       data object Empty : SearchUiState
       data class Error(val message: String) : SearchUiState
   }
   ```
4. **Migrar `LiveData` → `StateFlow`** nos 3 ViewModels.
5. **Tratamento de erro real:** `try/catch` + ramo `else`/exception → `UiState.Error`.
6. **Remover `println`** (Timber opcional, ou nada).
7. (Opcional, recomendado) **Camada UseCase** fina para encapsular regra (ex.: "busca só com 4+ chars" hoje vive no Fragment).

**Entregável:** ViewModels puros, sem estado global, com estado observável testável.

---

## 6. Fase 2 — Testes unitários 100% (camada não-UI)

Cobrir, com `kotlinx-coroutines-test` + Turbine + MockK:

### ViewModels (cada um)
- Sucesso (200) → emite `Loading` depois `Success` com os dados.
- Lista vazia → emite `Empty`.
- 401 → dispara autenticação → re-tenta a chamada original → `Success`.
- Auth retorna `false`/sem cookie → emite `Error`, não faz retry.
- Exceção de rede → `Error`.
- Regra de input (ex.: busca < 4 chars não chama repo) — se movida para o VM/UseCase.

### Repositories
- Cada método delega ao `BusesService` com os parâmetros corretos e repassa a `Response`.
- Caminhos de erro (`Response` de erro, exceção propagada).

### SessionManager / AuthRepository
- Armazena/retorna cookie; atualização e estado inicial.

### Models / Mappers
- Desserialização (campos `@SerializedName`), `DirectionLine` enum, qualquer mapeamento.

**Meta:** 100% de linhas/branches nas classes incluídas no filtro JaCoCo.

**Entregável:** suíte unitária verde + relatório JaCoCo na meta para a camada não-UI.

---

## 7. Fase 3 — Habilitar Compose no build

- No `app/build.gradle.kts`:
  - `buildFeatures { compose = true }`.
  - `composeOptions { kotlinCompilerExtensionVersion = ... }` compatível com Kotlin **1.9.0** (ex.: `1.5.x`).
  - Referenciar as libs do Compose **já presentes no catálogo** (`platform(libs.androidx.compose.bom)`, `androidx.ui`, `androidx.material3`, `androidx.activity.compose`, `ui-tooling`, `ui-test-junit4`, `ui-test-manifest`).
- Compose e View system **coexistem** durante a migração (migração incremental, sem big-bang).

**Atenção à compatibilidade:** o `composeBom 2023.08.00` está alinhado ao compiler 1.5.x / Kotlin 1.9.0. Validar a matriz antes; se necessário, subir BOM e compiler juntos. Avaliar também subir o JVM target de 1.8 → 11/17 (recomendado para Compose).

**Entregável:** projeto compila com Compose habilitado, telas antigas intactas.

---

## 8. Fase 4 — Migração das telas XML → Compose

Ordem da mais simples para a mais complexa (cada uma é um PR):

| Ordem | Tela | Complexidade | Notas de migração |
|-------|------|-------------|-------------------|
| 1 | **SplashActivity** | Baixa | Lottie → `lottie-compose` (`LottieAnimation`); permissões via `rememberLauncherForActivityResult`. |
| 2 | **SearchLinesFragment** | Média | `TextInputLayout` → `OutlinedTextField`; RecyclerView+Adapter → `LazyColumn` + item composable; consome `UiState`. **Maior ganho de teste.** |
| 3 | **SearchActivity** | Baixa | Vira host Compose (`setContent`) / navegação. |
| 4 | **BusesLineFragment** | Alta | Mapa. Usar `maps-compose` (`GoogleMap`) **ou** manter `CityBusCustomMap` via `AndroidView`. |
| 5 | **BusesPositionMapActivity** | Alta | Idem mapa + botões de ação como composables. |

**Decisão a tomar (mapa):**
- **Opção A — `maps-compose`:** mais idiomático, mais testável, mas reescreve o clustering.
- **Opção B — `AndroidView` envolvendo `CityBusCustomMap`:** reaproveita o que já funciona, menos risco. **Recomendada** para reduzir escopo; o mapa fica fora da métrica de cobertura de qualquer forma.

**Navegação:** avaliar trocar Activities/Fragments por **Navigation-Compose** (single-Activity). Opcional — pode ficar para depois sem bloquear a meta.

**Entregável:** todas as telas renderizadas em Compose; XML legado ainda presente mas não usado.

---

## 9. Fase 5 — Testes de UI Compose + fechamento da meta

### Testes de UI (`androidTest`, `createComposeRule`)
- **Search:** digitar < 4 chars → não busca / mostra hint; ≥ 4 → mostra loading → lista; estado vazio; estado de erro com retry.
- **Item da lista:** exibe code/labels corretos; clique dispara callback.
- **Splash:** anima e navega após o delay/permissão.
- Estados de cada tela dirigidos pelo `UiState` (preview-driven testing).

### Filtros JaCoCo (exclusões da métrica "100%")
Excluir do cálculo (não-testáveis em unit):
- `*Application*`, `*Activity*` (bootstrap), `**/di/**` (módulos Koin),
- `**/customview/**` (mapa/cluster), classes geradas (`*_Impl`, `*Binding`, `R`, `BuildConfig`),
- Composables puramente visuais sem lógica (cobertos por UI test, não unit).

### Gate de cobertura
- Configurar `jacocoTestCoverageVerification` com `minimum = 1.0` (100%) sobre o conjunto **incluído**.
- Adicionar ao CI (se houver) para impedir regressão.

**Entregável:** unit 100% (camada incluída) + UI tests verdes + gate de cobertura no build.

---

## 10. Fase 6 — Limpeza

- Remover layouts XML migrados (`activity_*.xml`, `fragment_*.xml`, `item_bus_line.xml`).
- Desabilitar `viewBinding` no `build.gradle.kts`.
- Remover Adapters/ViewHolders, extensões de View não usadas (`ViewExt`, `AppCompatExt` de fragment), `ExampleInstrumentedTest`.
- Remover libs mortas do catálogo (`hilt-android`, `osmdroid` — não usadas).
- Atualizar README.

---

## 11. Riscos e mitigações

| Risco | Mitigação |
|-------|-----------|
| Incompatibilidade Compose compiler ↔ Kotlin 1.9.0 | Travar matriz BOM/compiler/Kotlin na Fase 3 antes de migrar telas. |
| Migração do mapa é cara e arriscada | Opção B (`AndroidView` reusando `CityBusCustomMap`) reduz escopo. |
| "100%" inflacionado/irreal | Filtros JaCoCo explícitos + gate; o número se refere à lógica testável. |
| Estado estático (COOKIE) quebra testes em paralelo | Resolver na Fase 1 **antes** de escrever a suíte. |
| Mistura Mockito + MockK | Padronizar em MockK na Fase 0. |
| JVM target 1.8 limita libs Compose modernas | Avaliar subir para 11/17 na Fase 3. |

---

## 12. Resumo de dependências a adicionar/referenciar

| Item | Onde | Situação |
|------|------|----------|
| Compose BOM, ui, material3, activity-compose, ui-tooling | catálogo | **já existem**, só referenciar |
| ui-test-junit4, ui-test-manifest | catálogo | **já existem**, referenciar em `androidTestImplementation` |
| `kotlinx-coroutines-test` | catálogo + app | adicionar |
| `turbine` | catálogo + app | adicionar |
| `androidx.arch.core:core-testing` | catálogo + app | adicionar (temporário, enquanto houver LiveData) |
| `lottie-compose` | catálogo + app | adicionar (migração Splash) |
| `maps-compose` | catálogo + app | adicionar **se** escolher Opção A do mapa |
| JaCoCo plugin | `app/build.gradle.kts` | adicionar |

---

## 13. Sequência sugerida de PRs

1. `chore: setup jacoco + infra de testes (MainDispatcherRule)`
2. `refactor: SessionManager + auth centralizada (remove estado estático)`
3. `refactor: StateFlow + UiState selado nos ViewModels`
4. `test: cobertura 100% ViewModels/Repositories/models`
5. `build: habilitar Jetpack Compose`
6. `feat: migrar Splash para Compose`
7. `feat: migrar Search (tela + lista) para Compose`
8. `feat: migrar telas de mapa para Compose`
9. `test: UI tests Compose + gate de cobertura`
10. `chore: remover XML/ViewBinding/libs mortas`

---

### Próximo passo
Validar este planejamento e, em especial, **duas decisões em aberto**:
1. **Mapa:** Opção A (`maps-compose`, idiomático) vs **Opção B** (`AndroidView` reusando `CityBusCustomMap`, menos risco — recomendada).
2. **Navegação:** manter Activities/Fragments ou migrar para Navigation-Compose (single-Activity).

Definidas essas, posso começar a implementação pela Fase 0.
