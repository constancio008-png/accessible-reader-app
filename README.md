# Lume — leitor de telas para Android

O **Lume 1.0.0-beta.2** é a primeira Beta pública planejada do leitor de telas para Android, com interface visual de alto contraste e navegação falada em português brasileiro.

> **Estado do projeto:** Beta pública em validação. A compilação automatizada foi executada; os gestos ainda precisam ser validados em aparelhos reais. O Lume não é um substituto confiável do TalkBack.

## Objetivos

- Narração em português do Brasil usando o mecanismo de síntese de voz instalado no Android.
- Navegação experimental por gestos e foco de acessibilidade.
- Configurações acessíveis para velocidade da fala, nível de detalhe, sons e vibração.
- Investigar e integrar efeitos sonoros de acessibilidade de bibliotecas externas, respeitando suas licenças.
- Melhorar a arquitetura de navegação com estudo de projetos maduros de código aberto, incluindo o TalkBack.

## O que a versão experimental contém

- Interface renovada com tema escuro, alto contraste, controles maiores e botões destacados.
- Diálogo de boas-vindas na primeira abertura.
- Quatro esquemas de efeitos sonoros externos: Suave, Navegação, Clássico e Vivo. Os arquivos são baixados no primeiro uso e armazenados em cache no aparelho.
- A fala e os efeitos sonoros usam atributos de áudio de acessibilidade; o serviço solicita o canal de volume de acessibilidade do Android.
- Narração contextual do tipo de controle e dos estados disponíveis, como botão, caixa de seleção, interruptor, marcado, desmarcado e desativado.
- Tela de configurações para velocidade da fala, detalhamento da narração, sons e vibração.
- Atalho para as configurações de acessibilidade do Android.
- Serviço de acessibilidade com ajustes para eventos de exploração por toque e toque duplo.
- Gestos experimentais para mover o foco, rolar e ativar o item selecionado.

## Limitações conhecidas

- Gestos, exploração por toque e gerenciamento de foco ainda podem falhar ou se comportar de forma diferente conforme o dispositivo e o aplicativo.
- Alguns aplicativos não expõem informações suficientes à API de acessibilidade do Android.
- A fala usa o mecanismo TTS configurado no aparelho; a qualidade e a voz dependem desse mecanismo e dos idiomas instalados.
- Os efeitos sonoros desta compilação ainda são gerados pelo próprio aplicativo. A substituição por uma biblioteca de sons apropriada será investigada antes de uma versão mais madura.
- **Mantenha o TalkBack ou outro leitor confiável disponível durante os testes.** Não desative seu leitor principal até confirmar que consegue recuperar o controle do aparelho.

## Instalar a versão de teste

1. Abra a página [GitHub Actions](https://github.com/constancio008-png/accessible-reader-app/actions).
2. Entre na execução mais recente que concluiu a compilação.
3. Na seção **Artifacts**, baixe `lume-debug-apk`.
4. Extraia o ZIP e instale o APK no Android, permitindo a instalação para essa origem caso o sistema solicite.
5. Abra o Lume e use o botão para acessar as configurações de acessibilidade e ativar o serviço.

O APK de depuração é para testes, não uma versão estável de distribuição. O pacote `br.com.cacadorreader` foi mantido e o `versionCode` passou para 7. Atenção: atualizar por cima também exige a mesma chave de assinatura. Como as compilações debug do CI podem usar chaves diferentes, a instalação sobre uma versão anterior ainda não está garantida; não desinstale a versão atual antes de verificar isso.

## Compilar

A compilação automatizada é feita pelo GitHub Actions usando Java 17, Gradle 8.9 e Android SDK 35. Um push que altere `cacador-reader/**` ou o workflow inicia uma nova compilação.

## Código-fonte e referências

O aplicativo usa a API pública `AccessibilityService` do Android. O projeto pretende estudar a arquitetura e os padrões de interação de leitores de telas maduros, incluindo o [Google TalkBack](https://github.com/google/talkback). Isso não significa que o código atual seja derivado do TalkBack. Qualquer código ou recurso de terceiros só deve ser incorporado depois de verificar a licença, as atribuições exigidas e os avisos de direitos autorais.

## Relatar problemas

Ao relatar uma falha, informe o modelo do aparelho, a versão do Android, o aplicativo em que ocorreu e os passos para reproduzir. Não envie informações pessoais capturadas na tela.

## Licença

A licença do código do Lume ainda precisa ser definida. Até que isso seja feito, a disponibilidade pública do repositório não deve ser interpretada como autorização automática para reutilizar, modificar ou redistribuir seu conteúdo.


### Créditos dos efeitos sonoros
Os esquemas de som usam arquivos do [THISUX Sound Kit](https://github.com/thisuxhq/soundkit), disponibilizado sob licença MIT. Os arquivos são baixados do repositório do projeto quando necessários e ficam em cache local. O aplicativo necessita de conexão à internet na primeira reprodução de cada efeito; se o download falhar, a voz continua sendo a forma principal de feedback.
