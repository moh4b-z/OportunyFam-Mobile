# 🧪 Checklist Final - Validação do Mapa

## ✅ Pré-requisitos Verificados

### Google Maps API
- [ ] API Key está no AndroidManifest.xml
- [ ] API Key está habilitada no Google Cloud Console
- [ ] Aplicativo está registrado no Google Cloud com o SHA-1 correto

### Permissões do App
- [ ] INTERNET permissão existe em AndroidManifest.xml
- [ ] ACCESS_FINE_LOCATION permissão existe
- [ ] ACCESS_COARSE_LOCATION permissão existe
- [ ] Usuário concedeu permissão de localização ao app

### Dependências
- [ ] `com.google.android.gms:play-services-maps:18.2.0` está em build.gradle
- [ ] `com.google.maps.android:maps-compose:4.3.3` está em build.gradle
- [ ] Google Play Services está instalado no device/emulador

## 🔄 Passos para Testar

### 1. Limpar e Reconstruir
```bash
cd C:\Users\Isabella\StudioProjects\OportunyFam-Mobile
./gradlew clean
./gradlew build
```

### 2. Instalar no Device/Emulador
```bash
./gradlew installDebug
```

### 3. Abrir App e Navegar para Home
- Executar o app
- Passar pela tela de Splash
- Fazer login/registro
- Navegação deve ir para Home (onde está o mapa)

### 4. Validar Comportamento Esperado

#### Durante o carregamento (primeiros segundos):
- [ ] Aparecer tela com "Carregando mapa..."
- [ ] CircularProgressIndicator está rodando

#### Após mapa carregar:
- [ ] Mapa Google visível com zoom inicial de 12x
- [ ] Fundo do mapa é o satélite/mapa padrão
- [ ] Marcadores aparecem em cores diferentes:
  - [ ] Azul: Sua localização
  - [ ] Verde: Instituições cadastradas
  - [ ] Laranja: Instituições não cadastradas (Google Places)

#### Interatividade:
- [ ] Pode fazer zoom in/out com pinch
- [ ] Pode arrastar o mapa
- [ ] Clicar em marcador mostra title/snippet
- [ ] Botão de atualizar localização (canto inferior esquerdo)

### 5. Verificar Logs
```bash
adb logcat | grep -i "HomeScreen\|Maps\|MapsInitializer"
```

**Esperado ver:**
```
✅ Google Maps inicializado com sucesso
📍 Localização obtida: [latitude], [longitude]
✅ [X] instituições cadastradas carregadas
✅ [X] instituições não cadastradas encontradas
```

**NÃO deve ver erros como:**
```
❌ Erro ao inicializar Google Maps
❌ Erro ao buscar instituições
```

## 🐛 Solução de Problemas

### Problema: "Carregando mapa..." fica congelado
**Solução:**
1. Verificar conexão com internet
2. Verificar se Google Play Services está instalado
3. Verificar logs para exceções

### Problema: Mapa branco/cinza
**Solução:**
1. Verificar API Key no AndroidManifest.xml
2. Testar API Key no Google Cloud Console
3. Verificar restrições da API Key

### Problema: Sem permissão de localização
**Solução:**
1. Ir para Configurações do App > Permissões
2. Ativar "Localização"
3. Escolher "Apenas enquanto usa o app"

### Problema: Erro de desserialização Gson
**Solução:**
1. Verificar se @SerializedName está adicionado em todos os campos
2. Limpar cache: `./gradlew clean`
3. Reconstruir projeto: `./gradlew build`

## 📱 Device/Emulador Mínimo
- Android 10+ (API 30)
- Google Play Services instalado
- Tela com resolução mínima 320x480

## 🎉 Sucesso!
Se todos os checkmarks acima estão marcados e o mapa aparece, tudo foi fixado com sucesso!

---

**Última atualização:** 2025-11-15
**Arquivos modificados:** 2 (HomeScreen.kt, Instituicao.kt)

