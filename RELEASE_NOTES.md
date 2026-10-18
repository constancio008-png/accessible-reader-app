# Lume 1.0.0-beta.1

**Status:** Beta pública em validação. A compilação automatizada passou, mas a navegação ainda precisa ser testada em aparelhos reais. Não substitua nem desative seu leitor principal.

## Destaques
- Interface redesenhada com tema escuro, alto contraste, controles maiores e botões destacados.
- Configuração do serviço ampliada para receber eventos de exploração por toque.
- Tratamento de eventos de entrada e saída da exploração para tentar anunciar o elemento tocado.
- Toque duplo encaminhado ao serviço nas versões compatíveis do Android.
- `versionCode` 6 e `versionName` `1.0.0-beta.1`; pacote mantido como `br.com.cacadorreader`.

## Pendências importantes
- Validar os gestos em aparelhos reais: tocar para ouvir, deslizar à direita/esquerda para próximo/anterior, deslizar para cima/baixo para rolar e toque duplo para ativar.
- Os efeitos sonoros ainda são gerados pelo app; a biblioteca de sons reais e sua licença continuam pendentes.
- O repositório ainda não tem licença própria definida.
- **Atualização sobre uma versão instalada:** o mesmo nome de pacote não basta; o Android também exige a mesma chave de assinatura. Como o CI pode gerar assinaturas de depuração diferentes entre execuções, não é possível garantir a atualização por cima da versão anterior até configurar uma chave persistente e segura. Não desinstale a versão anterior sem verificar isso.

## APK de teste
Acesse [GitHub Actions](https://github.com/constancio008-png/accessible-reader-app/actions), abra a execução mais recente concluída com sucesso e baixe `lume-debug-apk` em Artifacts.
