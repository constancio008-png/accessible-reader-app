# Lume 1.0.0-beta.3

**Status:** APK de teste em compilação automática; validação em aparelho real ainda necessária.

## Atualização de acessibilidade
- Ampliada a captura de eventos para seleção de itens, alterações em campos de texto, seleção de texto, rolagem e notificações.
- Anúncio de alterações de conteúdo com pequeno atraso para reduzir fala repetida quando a interface envia vários eventos seguidos.
- O leitor não ecoa automaticamente o texto digitado em campos, para reduzir o risco de expor senhas ou mensagens privadas.
- Melhor identificação de campos editáveis, áreas roláveis e opções de seleção.
- Mantidos o pacote `br.com.cacadorreader`, os controles de voz e os esquemas de efeitos sonoros.
- `versionCode` 8 e `versionName` `1.0.0-beta.3`.

## Limites e validação
O Lume utiliza APIs públicas do Android AccessibilityService. Nem todo aplicativo expõe seus elementos corretamente, e esta versão ainda precisa de testes em aparelho real para validar foco, gestos, eventos e convivência com outros leitores de tela. Não desative o TalkBack principal antes de concluir os testes.

## APK
Acesse [GitHub Actions](https://github.com/constancio008-png/accessible-reader-app/actions), abra a execução mais recente concluída com sucesso e baixe o artefato `lume-debug-apk`.
