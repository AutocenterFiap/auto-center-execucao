# language: pt
Funcionalidade: Execucao da oficina
  Como a oficina
  Quero conduzir o diagnostico e o reparo de uma OS
  Para concluir o servico e avisar o servico de Ordem de Servico

  Cenario: Fluxo completo de diagnostico e reparo
    Dado que a OS 4242 foi criada
    Entao a execucao da OS 4242 fica com status "EM_DIAGNOSTICO"
    E o evento "diagnostico.iniciado" e publicado para a OS 4242
    Quando a oficina conclui o diagnostico da OS 4242
    Entao o evento "diagnostico.concluido" e publicado para a OS 4242
    Quando a OS 4242 manda iniciar o reparo
    Entao a execucao da OS 4242 fica com status "EM_EXECUCAO"
    Quando a oficina finaliza a execucao da OS 4242
    Entao a execucao da OS 4242 fica com status "FINALIZADA"
    E o evento "execucao.finalizada" e publicado para a OS 4242
