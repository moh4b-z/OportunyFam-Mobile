# 🚀 OportunyFam Mobile

Este é o projeto **mobile nativo** para a plataforma **Android** do **OportunyFam**.  
Ele utiliza as bibliotecas modernas do **Jetpack** para construir uma aplicação eficiente, escalável e com **interface de usuário reativa**.

---

## 🛠️ Tecnologias e Versões Utilizadas

A tabela abaixo resume as principais ferramentas e versões usadas no projeto:

| **Componente**        | **Versão / Valor**     | **Propósito** |
|------------------------|------------------------|----------------|
| **Linguagem**          | Kotlin 2.0+            | Linguagem de programação principal. |
| **AGP** (Android Gradle Plugin) | 8.4.1+             | Plugin base para build do Android. |
| **KSP**                | 2.0.21-1.0.27          | Kotlin Symbol Processing (para geração de código do Room). |
| **UI**                 | Jetpack Compose        | Framework moderno para construção de interfaces declarativas. |
| **Persistência Local** | Room Database          | ORM baseado em SQLite para armazenamento local. |
| **Rede**               | Retrofit               | Cliente HTTP para integração com a API. |
| **Min SDK**            | 30                     | Versão mínima do Android suportada. |
| **Java/JVM Target**    | 11                     | Compatibilidade com a JVM. |

---

## ⚙️ Configuração do Gradle

A configuração do **Gradle** foi ajustada para um projeto Android **Single Platform**,  
removendo configurações conflitantes do Kotlin Multiplatform (KMP).

### 📁 Arquivo: `app/build.gradle.kts`

#### **Plugins**

Os plugins essenciais para Android, Compose, Room e KSP são aplicados:

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp") // KSP versão 2.0.21-1.0.27
    alias(libs.plugins.androidx.room)
}
