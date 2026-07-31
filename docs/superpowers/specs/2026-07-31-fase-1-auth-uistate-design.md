# Design — fechamento da Fase 1: auth centralizada + UiState/StateFlow

Data: 2026-07-31
Referência: `PLANEJAMENTO_COMPOSE_E_TESTES.md`, seção 5 (Fase 1), itens 2 a 6.

## Contexto

A Fase 1 do planejamento tem 7 itens. O item 1 (extrair TOKEN/COOKIE do estado
estático) já está implementado: existe `session/SessionManager.kt` injetável, o token
vem de `BuildConfig.SPTRANS_TOKEN` e é anexado por `service/TokenInterceptor.kt`.

Este spec cobre o que falta — itens 2 a 6 — e descarta o item 7.

Estado do código no momento em que este spec foi escrito:

- A autenticação (`toAuthenticate` + `handleSuccessfulAuthentication` + ramo `401`)
  está **triplicada e idêntica** nos 3 ViewModels.
- Os 3 ViewModels expõem `LiveData` sem representação de loading/erro/vazio.
- Os 3 consumidores de UI usam `observeForever` sem nunca remover o observer —
  leak de observer em `SearchLinesFragment.kt:70`, `BusesLineFragment.kt:56` e
  `BusesPositionMapActivity.kt:36`.
- Quem decide loading e vazio hoje é a UI: o Fragment esconde a lista e mostra o
  `loadingView` antes de chamar o ViewModel, e infere "vazio" de `isNullOrEmpty()`.
- Há 9 `println` em `app/src/main`.

## Objetivo

Deixar os 3 ViewModels sem nenhum conhecimento de autenticação e com estado
observável que represente loading, sucesso, vazio e erro — a pré-condição para a
suíte de testes da Fase 2 e para a migração Compose da Fase 4.

## Decisões de design

| # | Decisão | Alternativas descartadas |
|---|---|---|
| 1 | Auth centralizada **antes** do UiState | Fazer os dois no mesmo PR; fazer UiState primeiro (escreveria o ramo 401 três vezes para apagar depois) |
| 2 | Um único `AuthInterceptor` do OkHttp | `CookieJar` + `Authenticator` (mais peças, exige login bloqueante); `AuthRepository` na camada de dados (nada no tipo obriga os repos a passarem por ele) |
| 3 | `UiState<out T>` genérico | Um selado por tela (3x a mesma hierarquia); `data class` com flags (admite `isLoading` + `error` juntos) |
| 4 | Regra dos 4 chars **fica no Fragment** | Mover para o ViewModel; criar camada de UseCase (item 7 do planejamento) |
| 5 | `Error` carrega `ErrorType`, não texto | `@StringRes Int` (acopla o VM ao `R`); `String` (copy no VM, testes asseverando literais) |

A decisão 4 tem uma consequência registrada: o item da Fase 2 que cobriria essa
regra sai do escopo de cobertura. O planejamento já condiciona esse item a "*se*
movida para o VM/UseCase" (linha 136), então não há contradição com a meta de 100%.

## Divisão em PRs

| PR | Título | Itens da Fase 1 |
|---|---|---|
| A | `refactor: centralize auth in an OkHttp interceptor` | 2 |
| B | `refactor: expose screen state as StateFlow of sealed UiState` | 3, 4, 5, 6 |

Cada PR é mergeável de forma independente e o app continua funcionando ao fim de
cada um, como pede a estratégia geral do planejamento.

---

## PR A — auth centralizada na camada de rede

### Componente novo: `service/AuthInterceptor.kt`

Uma classe, síncrona, sem coroutines. Responsabilidades:

1. Anexar `Cookie: <session.cookie>` em toda request, quando o cookie não é vazio.
2. Ao receber `401`, fazer o login, gravar o cookie novo no `SessionManager` e
   repetir a request original uma vez.

```kotlin
class AuthInterceptor(private val session: SessionManager) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val cookieAtRequest = session.cookie
        val response = chain.proceed(chain.request().withCookie(cookieAtRequest))
        if (response.code != HTTP_UNAUTHORIZED) return response

        response.close()
        if (!renewSession(cookieAtRequest, chain)) return response
        return chain.proceed(chain.request().withCookie(session.cookie))
    }
}
```

Três invariantes que o desenho fixa:

**Ordem de registro no client.** `AuthInterceptor` é registrado **antes** do
`TokenInterceptor`, ficando o mais externo da cadeia. A request de login que ele
cria desce pelo `TokenInterceptor` e herda o `?token=` automaticamente. Inverter a
ordem quebraria o login (iria sem token).

**Single-flight no login.** O cookie é capturado **antes** do `chain.proceed`, para
que `cookieAtRequest` seja de fato o cookie que produziu o `401`. `renewSession` é
`synchronized` e compara esse valor com `session.cookie` atual: se outra thread já
renovou a sessão nesse meio-tempo, ele não faz um segundo login — retorna `true`
para que o retry aconteça com o cookie novo. Sem isso, N requests em paralelo
tomando `401` disparam N logins.

**Uma tentativa por request.** O retry é um `chain.proceed` direto, não uma chamada
recursiva ao próprio `intercept`. Se o retry também devolver `401`, esse `401` sobe
para o ViewModel. Não existe caminho para loop infinito.

A request de login é montada à mão, sem passar pelo Retrofit:

```kotlin
Request.Builder()
    .url(chain.request().url.newBuilder().encodedPath(LOGIN_PATH).build())
    .post("".toRequestBody())
    .build()
```

O cookie é lido do header `set-cookie` da resposta de login, e a resposta é
considerada bem-sucedida quando o código é 2xx e o corpo é `true` — o mesmo
critério que os ViewModels usam hoje.

### Remoções

`AuthInterceptor` assumindo o cookie e o login torna órfão tudo isto:

- **`service/BusesService.kt`** — remove `@Header("Cookie")` dos 3 endpoints e
  remove `toAuthenticate()` (a interface Retrofit não é mais usada para login).
- **`repository/SearchLinesRepository.kt`** — assinaturas passam a
  `getLines(searchTerm)` e `getBusesPositionToLine(line)`; `toAuthenticate()` sai
  da interface e da implementação.
- **`repository/BusesPositionRepository.kt`** — assinatura passa a
  `getBusesPosition()`; `toAuthenticate()` sai.
- **Os 3 ViewModels** — removem `toAuthenticate()`,
  `handleSuccessfulAuthentication()`, o ramo `401` e **o parâmetro
  `sessionManager` do construtor**. Nenhum ViewModel precisa mais do
  `SessionManager`.
- **Os 3 módulos Koin de ViewModel** — voltam a receber só o repository.

O `SessionManager` continua existindo e registrado no Koin; só troca de consumidor
(ViewModels → `AuthInterceptor`). A injeção dele nos ViewModels é, portanto,
transitória: ela foi o passo que eliminou o estado estático, e este PR a recolhe.

Ao fim dos dois PRs, os repositories são delegação pura — o que ajuda a meta de
100% da Fase 2.

### Wiring

`di/servicesmodule/BusesServiceModule.kt` passa a injetar o `SessionManager` no
`provideService`, e a ordem dos interceptors é explícita:

```kotlin
val busesServiceModule = module {
    single { provideService(get()) }
}

fun provideService(session: SessionManager): BusesService {
    val client = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor(session))       // externo
        .addInterceptor(TokenInterceptor(BuildConfig.SPTRANS_TOKEN))  // interno
        .build()
    // ...
}
```

### Dependência nova

`mockwebserver` (`com.squareup.okhttp3:mockwebserver`, versão 4.10.0 — a mesma do
`logging-interceptor` já no catálogo) em `libs.versions.toml` e como
`testImplementation`. Sem ele não há como exercitar o interceptor de verdade.

### Testes do PR A

Com `MockWebServer`, contra o `AuthInterceptor` real:

1. Resposta `200` → passa direto e a request carrega o header `Cookie`.
2. `401` → login `200` com `Set-Cookie` → retry → `200`; o cookie novo está no
   `SessionManager`.
3. `401` → login falha → devolve o `401` original, sem segunda tentativa.
4. Dois requests concorrentes tomando `401` → exatamente um hit em
   `/v2.1/Login/Autenticar`.

---

## PR B — `UiState` + `StateFlow`

### Componente novo: `ui/UiState.kt`

```kotlin
sealed interface UiState<out T> {
    data object Idle : UiState<Nothing>
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data object Empty : UiState<Nothing>
    data class Error(val type: ErrorType) : UiState<Nothing>
}

enum class ErrorType { Network, Server, Unauthorized, Unknown }
```

`Idle` é o estado inicial das 3 telas e, na busca, o estado de volta após limpar o
campo.

### Máquina de estados

Idêntica nos 3 ViewModels — e pequena, porque o `401` já foi resolvido na rede pelo
PR A:

```
Idle ──chamada──▶ Loading ──┬─ 200 + payload cheio ──▶ Success(data)
                            ├─ 200 + payload vazio ──▶ Empty
                            ├─ 401 ─────────────────▶ Error(Unauthorized)
                            ├─ outro código ────────▶ Error(Server)
                            ├─ IOException ─────────▶ Error(Network)
                            └─ outra exceção ───────▶ Error(Unknown)
```

`Error(Unauthorized)` só acontece quando o próprio retry do `AuthInterceptor`
falhou — ou seja, a sessão não pôde ser renovada.

Semântica de vazio por tela, preservando o comportamento atual:

| Tela | Vazio quando | Decidido hoje em |
|---|---|---|
| Search | `body().isNullOrEmpty()` | `SearchLinesFragment.kt:72` |
| BusesLine | `body().buses.isEmpty()` | `BusesLineFragment.kt:58` |
| BusesPosition | `body().lines.isEmpty()` | **não existe hoje** |

`BusesPositionViewModel` ganha um ramo `Empty` que não existia. É a única mudança
de comportamento visível deste PR: sem ele, uma resposta sem linhas deixa o mapa
em branco sem explicação.

### API dos ViewModels

```kotlin
// SearchLinesViewModel
val state: StateFlow<UiState<List<BusesLines>>>
fun searchLines(term: String)
fun onClearSearch()

// BusesPositionViewModel
val state: StateFlow<UiState<BusesPosition>>
fun getBusesPosition()

// BusesLineViewModel
val state: StateFlow<UiState<BusesPositionToLine>>
fun getBusesPositionToLine(line: String)
```

Padrão interno: `private val _state = MutableStateFlow<UiState<X>>(UiState.Idle)`
exposto por `asStateFlow()`.

`onClearSearch()` é novo e existe porque o `StateFlow` passa a ser fonte única de
verdade: o Fragment não pode mais zerar o adapter por fora e deixar o estado
dizendo `Success`.

### Lado da UI

Os 3 consumidores trocam `observeForever` por coleta atrelada ao ciclo de vida, o
que **corrige o leak de observer** existente nos três:

```kotlin
viewLifecycleOwner.lifecycleScope.launch {
    viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
        viewModel.state.collect(::render)
    }
}
```

Em `BusesPositionMapActivity`, o mesmo com `lifecycleScope` e `this` como owner.

Cada tela ganha um único `render(state: UiState<X>)` com `when` exaustivo,
concentrando os `visible()/gone()` que hoje estão espalhados entre o `TextWatcher`,
o observer e os `setOnClickListener`. A UI deixa de mostrar loading na mão antes de
chamar o ViewModel — quem emite `Loading` é o ViewModel.

`lifecycle-runtime-ktx` já está no build; nenhuma dependência nova.

Na direção oposta, `androidx.arch.core:core-testing` foi adicionado na Fase 0
explicitamente "enquanto ainda houver `LiveData`". Sem `LiveData` no `main` ao fim
deste PR, o `InstantTaskExecutorRule` perde a razão de existir e a dependência pode
sair — junto com as outras libs mortas, na Fase 6.

**Strings novas:** `error_network`, `error_server`, `error_unauthorized`,
`error_unknown`. O mapeamento `ErrorType` → texto vive na UI, não no ViewModel.

### Testes do PR B

Este PR não escreve a suíte completa — isso é a Fase 2. Escreve o mínimo para não
entregar código sem rede: por ViewModel, um teste de `Loading → Success` e um de
`Loading → Error(Network)`, com `MainDispatcherRule` + Turbine, provando que o
`StateFlow` emite na ordem correta.

Os 3 testes atuais de `assertNotNull` são substituídos por esses.

A Fase 2 então preenche a matriz completa (vazio, códigos de erro, exceções) sobre
uma máquina de estados que já não tem ramo de auth para cobrir.

---

## Fora de escopo

Explicitamente **não** entra nestes dois PRs:

- **Debounce na busca.** Hoje cada tecla a partir do 4º char dispara uma request.
  Corrigir é mudança de comportamento; mover a regra para o ViewModel (decisão 4,
  descartada) é o que tornaria o debounce trivial depois.
- **Regra dos 4 chars.** Fica no `TextWatcher` do Fragment.
- **`_binding` não anulado em `onDestroyView`** nos 2 Fragments. Leak adjacente,
  independente deste trabalho.
- **`CookieJar` do OkHttp.** O `AuthInterceptor` já resolve o cookie; trocar por
  `CookieJar` seria ganho estético.
- **Qualquer coisa de Compose.** Fase 3 em diante.

## Critérios de conclusão

- `grep -r "toAuthenticate" app/src/main` não retorna nada nos ViewModels nem nos
  repositories.
- `grep -rc "println" app/src/main` retorna 0.
- Nenhum `observeForever` nem `LiveData` em `app/src/main`.
- Os 3 ViewModels não importam `SessionManager`.
- `./gradlew testDebugUnitTest` verde, incluindo os 4 testes do `AuthInterceptor` e
  os 6 testes de estado dos ViewModels.
- O app roda: mapa carrega, busca por linha lista resultados, tela de linha mostra
  os ônibus.
