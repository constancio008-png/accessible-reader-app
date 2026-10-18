# Caçador Reader — versão experimental 0.4.0

Leitor de telas Android em Java, em português brasileiro, com configurações acessíveis, navegação por gestos, voz do mecanismo TTS instalado no aparelho, efeitos acústicos sintetizados localmente e retorno tátil opcional.

## Recursos incluídos nesta versão
- Navegação por gesto horizontal: deslizar para a direita avança para o próximo elemento; deslizar para a esquerda volta ao anterior.
- Gestos verticais para rolar a área rolável; quando não há área rolável, tenta navegar pelos elementos.
- Duplo toque para ativar o elemento com foco e duplo toque prolongado para ouvir a ajuda de gestos, quando o Android encaminhar esses gestos ao serviço.
- Voz em português do Brasil com velocidade configurável.
- Três níveis de narração: essencial, padrão e detalhado.
- Efeitos acústicos próprios, sintetizados em tempo real no aparelho, com volume configurável; não são sons de videogame e não exigem arquivos de áudio externos.
- Vibração discreta opcional.
- Preferências persistidas, demonstração de som, teste de voz, ajuda de gestos e restauração de padrões.
- Botão para abrir as configurações de acessibilidade do Android.

## Compilação automática
Cada atualização na branch `master` inicia o GitHub Actions. Abra **Actions**, selecione **Compilar Caçador Reader (Android)** e baixe o artefato `cacador-reader-debug-apk` quando a execução terminar com sucesso.

## Instalação e ativação
1. Baixe o artefato APK da execução bem-sucedida e instale-o no aparelho.
2. Abra Caçador Reader.
3. Toque em **Ativar ou gerenciar o leitor**.
4. Nas configurações de acessibilidade do Android, habilite Caçador Reader e confirme a permissão.
5. Volte ao aplicativo para ajustar voz, detalhamento, sons e vibração.

## Limitações importantes
Esta é uma implementação experimental, não uma substituição validada do TalkBack. A acessibilidade de cada aplicativo depende das informações que ele expõe ao Android; certos gestos e a exploração por toque variam por fabricante e versão do sistema. Mantenha outro leitor de telas confiável disponível até concluir os testes no dispositivo. Uma compilação bem-sucedida confirma que o APK foi construído, mas não prova que todos os fluxos funcionam em todos os aparelhos.
