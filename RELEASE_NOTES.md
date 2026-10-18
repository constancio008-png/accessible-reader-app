# Lume 0.5.0-experimental

**Status:** pré-lançamento experimental — testes em aparelhos reais ainda necessários.

## Destaques
- Nome público do aplicativo alterado para **Lume**.
- Tela inicial, mensagens faladas, descrições de acessibilidade e mensagens de serviço atualizadas com a nova identidade.
- Documentação reescrita para descrever o estado real do projeto e suas limitações.
- Artefato de compilação do GitHub Actions renomeado para `lume-debug-apk`.
- Número de versão atualizado para `0.5.0-experimental`.

## O que ainda não está resolvido
- A navegação por gestos, exploração por toque e gerenciamento de foco ainda precisam de correções e testes em aparelhos reais.
- Os sons atuais continuam sendo gerados pelo aplicativo. A próxima etapa é avaliar bibliotecas de efeitos sonoros de acessibilidade e verificar cuidadosamente suas licenças.
- O estudo do código do TalkBack e de outros leitores de código aberto ainda não foi integrado nesta versão.
- O repositório ainda não tem uma licença de código definida.

## Instalação para testes
Acesse [GitHub Actions](https://github.com/constancio008-png/accessible-reader-app/actions), abra a execução mais recente que terminou com sucesso e baixe o artefato `lume-debug-apk`.

**Aviso:** esta versão não é um substituto confiável do TalkBack. Mantenha seu leitor de telas principal disponível durante os testes.
