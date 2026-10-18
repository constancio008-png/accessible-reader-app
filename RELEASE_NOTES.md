# Lume 1.0.0-beta.2

**Status:** Beta pública em validação. A compilação precisa ser confirmada no CI e os recursos de áudio devem ser testados em aparelhos reais. Não substitua nem desative seu leitor principal.

## Novidades
- Quatro esquemas de efeitos sonoros externos: Suave, Navegação, Clássico e Vivo.
- Os efeitos são obtidos do THISUX Sound Kit (licença MIT) e guardados em cache local após o primeiro download.
- Diálogo de boas-vindas exibido na primeira abertura do aplicativo.
- Fala configurada com `USAGE_ASSISTANCE_ACCESSIBILITY` e serviço solicitando o canal de volume de acessibilidade do Android.
- Dicas faladas de tipo de controle e estado: botão, caixa de seleção, interruptor, campo de edição, marcado/desmarcado e desativado, conforme as informações expostas pelo aplicativo acessado.
- `versionCode` 7 e `versionName` `1.0.0-beta.2`; pacote mantido como `br.com.cacadorreader`.

## Como funcionam os sons
Os sons são baixados de [THISUX Sound Kit](https://github.com/thisuxhq/soundkit) no primeiro uso e armazenados no cache do app. É necessária conexão com a internet para baixar cada efeito que ainda não esteja em cache. A narração falada continua sendo o principal retorno acessível e não deve depender dos efeitos sonoros.

## Validação pendente
- Confirmar a compilação no GitHub Actions.
- Testar se os quatro esquemas reproduzem corretamente os arquivos e se o canal de volume aparece como volume de acessibilidade no dispositivo.
- Validar a pronúncia de tipos e estados em botões, caixas de seleção, interruptores e campos de edição de diferentes aplicativos.
- A atualização sobre uma versão instalada exige o mesmo identificador de pacote e a mesma chave de assinatura. O CI pode assinar APKs de depuração com chaves diferentes; não desinstale a versão anterior sem verificar isso.

## APK de teste
Acesse [GitHub Actions](https://github.com/constancio008-png/accessible-reader-app/actions), abra a execução mais recente concluída com sucesso e baixe `lume-debug-apk` em Artifacts.
