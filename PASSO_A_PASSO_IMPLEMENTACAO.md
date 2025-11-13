# 📖 Passo a Passo - Implementação de Localização

## 🎯 Objetivo
Implementar sistema onde usuário vê sua localização no mapa automaticamente.

---

## 📋 Pré-requisitos

- ✅ Android Studio 2024+
- ✅ Gradle 8.0+
- ✅ Kotlin 1.9+
- ✅ Google Play Services instalado
- ✅ Emulador Android 11+ ou dispositivo

---

## 🚀 Passo 1: Preparar Dependências

### 1.1 Verificar build.gradle.kts

```gradle
dependencies {
    // Google Play Services Location
    implementation("com.google.android.gms:play-services-location:21.0.1")
    
    // Google Maps Compose (deve estar aqui)
    implementation("com.google.maps.android:maps-compose:4.3.0")
    
    // Androidx Core (deve estar aqui)
    implementation("androidx.core:core:1.9.0")
}
```

**Como fazer:**
1. Abra `app/build.gradle.kts`
2. Procure na seção `dependencies`
3. Se não tiver a primeira linha, adicione
4. Clique "Sync Now"

---

## 🚀 Passo 2: Adicionar Permissões

### 2.1 Editar AndroidManifest.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest ...>

    <!-- Adicionar estas linhas -->
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    
    <application>
        <!-- resto aqui -->
    </application>
</manifest>
```

**Como fazer:**
1. Abra `app/src/main/AndroidManifest.xml`
2. Após `<manifest>`, adicione as permissões
3. Antes de `<application>`

---

## 🚀 Passo 3: Criar LocationManager

### 3.1 Novo arquivo

**Criar:** `app/src/main/java/com/oportunyfam_mobile/Service/LocationManager.kt`

```kotlin
package com.oportunyfam_mobile.Service

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class LocationManager(context: Context) {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
    private val context = context

    fun getCurrentLocation(onLocationReceived: (Location?) -> Unit) {
        try {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                    onLocationReceived(location)
                }.addOnFailureListener {
                    onLocationReceived(null)
                }
            } else {
                onLocationReceived(null)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            onLocationReceived(null)
        }
    }
}
```

---

## 🚀 Passo 4: Modificar LocationPermissionDialog

### 4.1 Adicionar parâmetro

**Arquivo:** `Screens/LocationPermissionDialog.kt`

```kotlin
@Composable
fun LocationPermissionDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    context: Context,
    onLocationPermissionGranted: (() -> Unit)? = null  // ← NOVO
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        // ... resto igual
        confirmButton = {
            Button(
                onClick = {
                    openLocationSettings(context)
                    onLocationPermissionGranted?.invoke()  // ← NOVO
                    onConfirm()
                }
            ) {
                // ...
            }
        }
    )
}
```

---

## 🚀 Passo 5: Atualizar HomeScreen

### 5.1 Adicionar imports

```kotlin
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.material.icons.filled.MyLocation
import androidx.core.content.ContextCompat
import com.google.maps.android.compose.CameraUpdateOptions
import com.oportunyfam_mobile.Service.LocationManager
import androidx.compose.ui.platform.LocalContext
```

### 5.2 Adicionar estados

```kotlin
@Composable
fun HomeScreen(navController: NavHostController?) {
    val context = LocalContext.current

    // ← Adicionar estas linhas
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var showLocationDialog by remember { mutableStateOf(false) }
    var locationManager by remember { mutableStateOf<LocationManager?>(null) }
    
    // ... resto do código
}
```

### 5.3 Adicionar LaunchedEffect

```kotlin
    // Depois dos estados, adicione:
    LaunchedEffect(Unit) {
        locationManager = LocationManager(context)
        
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        
        if (hasPermission) {
            locationManager?.getCurrentLocation { location ->
                if (location != null) {
                    userLocation = LatLng(location.latitude, location.longitude)
                }
            }
        } else {
            showLocationDialog = true
        }
    }
```

### 5.4 Modificar inicialização do mapa

```kotlin
    // Mude isso:
    val initialLatLng = LatLng(-23.5505, -46.6333)
    
    // Para isso:
    val initialLatLng = userLocation ?: LatLng(-23.5505, -46.6333)
    
    // Adicione este LaunchedEffect:
    LaunchedEffect(userLocation) {
        if (userLocation != null) {
            cameraPositionState.animate(
                update = CameraUpdateOptions(zoom = 15f),
                durationMs = 1000
            )
        }
    }
```

### 5.5 Adicionar marcador no mapa

```kotlin
        // Dentro do GoogleMap(), mude de:
        GoogleMap(
            modifier = Modifier.matchParentSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = false),
            uiSettings = MapUiSettings(zoomControlsEnabled = false)
        )

        // Para:
        GoogleMap(
            modifier = Modifier.matchParentSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = false),
            uiSettings = MapUiSettings(zoomControlsEnabled = false)
        ) {
            // ← ADICIONE ISTO
            if (userLocation != null) {
                Marker(
                    state = rememberMarkerState(position = userLocation!!),
                    title = "Sua Localização",
                    snippet = "Você está aqui",
                    infoWindowContent = {
                        Text(
                            text = "Sua Localização",
                            fontSize = 14.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                )
            }
        }
```

### 5.6 Adicionar botão de atualização

```kotlin
        // Antes do botão flutuante existente, adicione:
        FloatingActionButton(
            onClick = {
                locationManager?.getCurrentLocation { location ->
                    if (location != null) {
                        userLocation = LatLng(location.latitude, location.longitude)
                    }
                }
            },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = 90.dp, start = 16.dp),
            containerColor = Color(0xFFF69508)
        ) {
            Icon(Icons.Filled.MyLocation, contentDescription = "Minha Localização", tint = Color.White)
        }
```

### 5.7 Adicionar diálogo

```kotlin
        // No final da composição, antes da chave de fechamento:
        if (showLocationDialog) {
            LocationPermissionDialog(
                onDismiss = { showLocationDialog = false },
                onConfirm = { showLocationDialog = false },
                context = context,
                onLocationPermissionGranted = {
                    locationManager?.getCurrentLocation { location ->
                        if (location != null) {
                            userLocation = LatLng(location.latitude, location.longitude)
                        }
                    }
                }
            )
        }
}
```

---

## 🧪 Passo 6: Testar

### 6.1 Compilar
```bash
cd C:\Users\24122781\StudioProjects\OportunyFam-Mobile
gradlew.bat clean build
```

### 6.2 Executar
1. Clique "Run" no Android Studio
2. Selecione emulador ou dispositivo
3. Instale app

### 6.3 Testar Fluxo
1. Abra app
2. Veja diálogo (primeira vez)
3. Clique "Ativar Localização"
4. Vá para Configurações do Android
5. Autorize localização
6. Volte para app
7. Veja seu marcador no mapa ✅

---

## ✅ Checklist Pós-Implementação

- [ ] Dependências adicionadas
- [ ] Permissões no AndroidManifest
- [ ] LocationManager criado
- [ ] LocationPermissionDialog atualizado
- [ ] HomeScreen modificado
- [ ] Código compila sem erros
- [ ] App executa sem crash
- [ ] Diálogo aparece
- [ ] Marcador aparece após autorizar
- [ ] Botão atualiza localização

---

## 🐛 Se Não Funcionar

### Erro 1: "Cannot find symbol LocationManager"
**Solução:** Adicione import
```kotlin
import com.oportunyfam_mobile.Service.LocationManager
```

### Erro 2: "Unresolved reference CameraUpdateOptions"
**Solução:** Adicione import
```kotlin
import com.google.maps.android.compose.CameraUpdateOptions
```

### Erro 3: "Compilação falha"
**Solução:**
1. Sync Gradle (File → Sync Now)
2. Clean project (Build → Clean Project)
3. Build project (Build → Build Project)

### Erro 4: "Diálogo não aparece"
**Solução:** 
1. Desinstale app
2. Limpe cache (Configurações → Apps → OportunyFam → Storage → Clear Cache)
3. Reinstale

### Erro 5: "Marcador não aparece"
**Solução:**
1. Espere alguns segundos após autorizar
2. Clique botão 📍 para forçar atualização
3. Verifique GPS está ativado

---

## 📚 Próximas Melhorias

1. **Rastreamento em Tempo Real**
   - Usar LocationRequest para atualizações contínuas

2. **Mostrar ONGs Próximas**
   - Integrar com API de ONGs
   - Mostrar distância

3. **Salvar Localizações**
   - Room Database para histórico

---

## 🎊 Pronto!

Se você seguiu todos os passos, agora tem:
- ✅ Sistema de localização funcional
- ✅ Permissões bem tratadas
- ✅ Localização no mapa
- ✅ Botão de atualização
- ✅ Diálogo elegante

**Parabéns! 🎉**


