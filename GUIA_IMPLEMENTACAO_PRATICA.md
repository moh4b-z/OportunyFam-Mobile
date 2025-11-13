# 📚 Guia Passo a Passo - Implementação Prática

## ✅ Checklist de Implementação

### Fase 1: Setup Básico ✓
- [x] CategoryFilterChip.kt criado
- [x] CategoryFilterRow.kt criado
- [x] OngMapMarker.kt criado
- [x] OngMapMarkersHelper.kt criado
- [x] HomeScreen.kt atualizado com imports
- [x] HomeScreen.kt atualizado com estados

### Fase 2: Visualização (Seu Próximo Passo)
- [ ] Testar se os chips aparecem na tela
- [ ] Verificar cores das categorias
- [ ] Testar clique nos chips

### Fase 3: Lógica de Filtro
- [ ] Conectar com sua API de ONGs
- [ ] Implementar busca de ONGs
- [ ] Mostrar marcadores no mapa

### Fase 4: Detalhes
- [ ] Adicionar tela de detalhes da ONG
- [ ] Implementar navegação
- [ ] Adicionar animações

---

## 🚀 Implementação Passo a Passo

### PASSO 1: Verificar Compilação

```bash
# Abra o terminal no Android Studio
# Execute:
./gradlew build

# Se tudo estiver ok, não haverá erros de compilação
```

### PASSO 2: Adicionar os Chips à HomeScreen

Se ainda não estão visíveis, certifique-se de que no `HomeScreen.kt` tem:

```kotlin
// Dentro da Box principal, após GoogleMap:
CategoryFilterRow(
    categories = categories,
    selectedCategories = selectedCategories,
    onCategorySelected = { categoriaId ->
        filtrarOngsPorCategoria(categoriaId)
    },
    modifier = Modifier
        .align(Alignment.TopCenter)
        .padding(top = 90.dp)
)
```

### PASSO 3: Testar Clique nos Chips

Adicione logs para verificar:

```kotlin
fun filtrarOngsPorCategoria(categoriaId: Int) {
    if (selectedCategories.contains(categoriaId)) {
        selectedCategories = selectedCategories.filter { it != categoriaId }
        android.util.Log.d("HomeScreen", "Deseleccionada categoria: $categoriaId")
    } else {
        selectedCategories = selectedCategories + categoriaId
        android.util.Log.d("HomeScreen", "Selecionada categoria: $categoriaId")
    }
    
    android.util.Log.d("HomeScreen", "Categorias selecionadas: $selectedCategories")
}
```

### PASSO 4: Conectar com API

Crie um novo serviço no `RetrofitFactory.kt`:

```kotlin
fun getOngService(): OngService {
    return retrofit.create(OngService::class.java)
}
```

Crie a interface:

```kotlin
// OngService.kt
interface OngService {
    @GET("v1/ongs/filtrar")
    fun filtrarPorCategorias(
        @Query("categorias") categorias: String
    ): Call<List<OngMapMarker>>
    
    @GET("v1/ongs/all")
    fun buscarTodas(): Call<List<OngMapMarker>>
}
```

### PASSO 5: Atualizar Função de Filtro

```kotlin
fun filtrarOngsPorCategoria(categoriaId: Int) {
    if (selectedCategories.contains(categoriaId)) {
        selectedCategories = selectedCategories.filter { it != categoriaId }
    } else {
        selectedCategories = selectedCategories + categoriaId
    }
    
    if (selectedCategories.isNotEmpty()) {
        // Chamar API
        isLoading = true
        RetrofitFactory().getOngService()
            .filtrarPorCategorias(selectedCategories.joinToString(","))
            .enqueue(object : Callback<List<OngMapMarker>> {
                override fun onResponse(
                    call: Call<List<OngMapMarker>>,
                    response: Response<List<OngMapMarker>>
                ) {
                    isLoading = false
                    if (response.isSuccessful) {
                        filteredOngs = response.body() ?: emptyList()
                        android.util.Log.d("HomeScreen", "ONGs encontradas: ${filteredOngs.size}")
                    }
                }
                
                override fun onFailure(call: Call<List<OngMapMarker>>, t: Throwable) {
                    isLoading = false
                    t.printStackTrace()
                }
            })
    } else {
        filteredOngs = emptyList()
    }
}
```

### PASSO 6: Adicionar Marcadores ao Mapa

No GoogleMap, adicione:

```kotlin
GoogleMap(
    modifier = Modifier.matchParentSize(),
    cameraPositionState = cameraPositionState,
    // ... outras propriedades ...
) {
    // Adicionar marcadores das ONGs filtradas
    OngMapMarkers(
        ongs = filteredOngs,
        onMarkerClick = { ong ->
            // Navegação futura
            navController?.navigate("ong_details/${ong.id}")
        }
    )
}
```

### PASSO 7: Testar com Dados Locais

Para testar sem API, use os dados de exemplo:

```kotlin
// Adicione isso após o Estado de categorias selecionadas
val allOngs = remember { getExampleOngs() }
var filteredOngs by remember { mutableStateOf<List<OngMapMarker>>(emptyList()) }

// Atualize a função de filtro
fun filtrarOngsPorCategoria(categoriaId: Int) {
    if (selectedCategories.contains(categoriaId)) {
        selectedCategories = selectedCategories.filter { it != categoriaId }
    } else {
        selectedCategories = selectedCategories + categoriaId
    }
    
    filteredOngs = filterOngsByCategories(allOngs, selectedCategories)
}
```

---

## 🧪 Testes de Validação

### Teste 1: Verificar Rendering dos Chips
```
✓ 4 chips aparecem na tela
✓ Cores estão corretas
✓ Nenhum chip está pré-selecionado
```

### Teste 2: Verificar Interação
```
✓ Clicar no chip muda de cor
✓ Ícone de check aparece
✓ Pode selecionar múltiplos chips
✓ Pode deselecionar chips
```

### Teste 3: Verificar Filtro de ONGs
```
✓ Selecionando "Jiu Jitsu" mostra ONGs com Jiu Jitsu
✓ Selecionando "T.I" mostra ONGs com T.I
✓ Selecionando ambas mostra 5 ONGs (1+2+5)
✓ Desselecionar todas limpa a lista
```

### Teste 4: Verificar Mapa
```
✓ Marcadores aparecem na posição correta
✓ Clicar em marcador mostra info
✓ Marcadores desaparecem quando desseleciona categoria
```

---

## 🐛 Possíveis Problemas e Soluções

### Problema: Chips não aparecem
**Solução:**
```kotlin
// Verifique se CategoryFilterRow está no Box principal
// e se tem o modifier correto
modifier = Modifier
    .align(Alignment.TopCenter)
    .padding(top = 90.dp)
```

### Problema: Cores não aparecem
**Solução:**
```kotlin
// Verifique a ordem de composição
// Chips precisam estar APÓS SearchBar
// padding top deve ser 90.dp (height da SearchBar)
```

### Problema: Cliques não funcionam
**Solução:**
```kotlin
// Verifique se a função é chamada
// Adicione logs
android.util.Log.d("HomeScreen", "Categoria clicada: $categoriaId")
```

### Problema: Marcadores não aparecem
**Solução:**
```kotlin
// Verifique se OngMapMarkers está dentro do GoogleMap
// Verifique se filteredOngs não está vazio
// Confira as coordenadas latitude/longitude
```

---

## 📊 Estrutura Final do HomeScreen

```kotlin
HomeScreen
├── Box(fillMaxSize)
│   ├── GoogleMap
│   │   └── OngMapMarkers(filteredOngs)
│   ├── SearchBar (top 16.dp)
│   ├── CategoryFilterRow (top 90.dp) ← SEUS NOVOS FILTROS
│   │   ├── Jiu Jitsu chip
│   │   ├── T.I chip
│   │   ├── Centro Cultural chip
│   │   └── Biblioteca chip
│   ├── Resultados de busca (se houver query)
│   ├── FAB (bottom-right)
│   └── BarraTarefas (bottom)
```

---

## ✨ Dicas Finais

1. **Comece simples:** Teste com dados locais antes de conectar API
2. **Use Logs:** Adicione logs para entender o fluxo
3. **Teste incrementalmente:** Uma funcionalidade por vez
4. **Verifique coordenadas:** Latitude e longitude devem estar em São Paulo
5. **Cores:** Customize conforme sua marca

---

## 🎓 Próximos Desafios

Depois de implementar tudo, você pode:

1. ✅ Adicionar animação ao zoom do mapa
2. ✅ Implementar busca + filtro juntos
3. ✅ Criar tela de detalhes da ONG
4. ✅ Adicionar avaliações de ONGs
5. ✅ Implementar favoritos

---

**Sucesso na implementação! 🚀**


