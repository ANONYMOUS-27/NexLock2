# NexLock

Bloqueador de aplicativos Android em Kotlin + Jetpack Compose.

## Gerar o APK pelo celular usando GitHub Actions

1. Crie um repositório no GitHub chamado `NexLock`.
2. Envie todo o conteúdo desta pasta para a raiz do repositório, incluindo a pasta `.github`.
3. Abra a aba **Actions** do repositório.
4. Abra **Build NexLock APK**.
5. Toque em **Run workflow**.
6. Quando a execução terminar, abra a execução e baixe o artefato **NexLock-APK**.
7. Extraia o ZIP do artefato e instale `app-debug.apk` no Android.

## Permissões no Android

O app precisa de acesso às estatísticas de uso para identificar qual aplicativo está em primeiro plano. Conceda essa permissão nas configurações quando o NexLock solicitar.

A autenticação biométrica utiliza a API BiometricPrompt do Android. Os métodos disponíveis dependem da biometria configurada e suportada pelo aparelho.
