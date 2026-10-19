# Lume 2.0 Beta 1

**Status:** alterações enviadas ao repositório; compilação automática em andamento. Testes em aparelho real ainda são necessários.

## Grande reformulação
- Central de configurações organizada por categorias, com telas separadas e botão Voltar: Voz e leitura; Navegação e gestos; Sons e vibração; Acessibilidade e privacidade; Sobre e diagnóstico.
- Controles nativos do Android para melhorar a identificação de rótulos, funções e estados por leitores de telas.
- Melhor organização das opções e explicações mais claras sobre os limites da exploração por toque.
- Removido o caminho manual duplicado de exploração por coordenadas para reduzir eventos e anúncios repetidos; a navegação depende dos eventos e gestos fornecidos pelo Android.
- Configuração de eventos e gestos alinhada entre o manifesto XML do serviço e o código.
- Workflow de compilação cancela builds antigos quando uma revisão mais nova chega, reduzindo o risco de publicar um APK obsoleto.
- Pacote mantido como `br.com.cacadorreader`; `versionCode` 9 e `versionName` `2.0-beta.1`.

## Atualização por cima da versão anterior: atenção à assinatura
Manter o mesmo identificador do pacote é necessário, mas não suficiente. O Android também exige que o APK novo seja assinado pela mesma chave do APK instalado. Os APKs de depuração produzidos em runners temporários do GitHub Actions podem receber chaves diferentes entre execuções; nesse caso, o Android recusará a atualização. Para garantir atualizações futuras sem desinstalar, é necessário configurar uma chave de assinatura persistente e protegida como GitHub Actions Secrets. Não desinstale a versão atual até verificar a assinatura do APK novo.

## Limites e segurança
O Android normalmente permite que apenas um serviço controle a exploração por toque por vez. Portanto, não é tecnicamente possível prometer que o Lume e o TalkBack controlarão os mesmos gestos de exploração por toque simultaneamente em todos os aparelhos. Mantenha um leitor confiável disponível enquanto testa o Lume.

## APK de teste
Acesse [GitHub Actions](https://github.com/constancio008-png/accessible-reader-app/actions), abra a execução mais recente concluída com sucesso e baixe o artefato `lume-debug-apk`.
