# language: pt
Funcionalidade: Execucao da oficina
  Como a oficina
  Quero processar a execucao de uma OS apos o pagamento confirmado
  Para concluir o reparo e avisar o servico de Ordem de Servico

  Cenario: Fluxo completo de execucao apos pagamento confirmado
    Dado que o pagamento da OS "os-bdd-1" foi confirmado
    Quando a oficina inicia o diagnostico da execucao
    E a oficina inicia o reparo
    E a oficina finaliza a execucao
    Entao a execucao da OS "os-bdd-1" fica com status "FINALIZADA"
    E o evento "execucao.finalizada" e publicado para a OS "os-bdd-1"
