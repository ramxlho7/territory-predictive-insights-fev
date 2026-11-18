# Territory Predictive Insights — CitizenTech (Monitoramento de Alagamentos)

**Disciplina:** Programação Orientada a Objetos (2º Semestre / 2026 — Turma de Fevereiro)  
**Tema:** CitizenTech — Tecnologias para as necessidades reais das pessoas nas cidades e comunidades  
**Recorte Escolhido:** Clima local: chuva intensa e risco de alagamento em pontos vulneráveis  

---

## 1. Público Beneficiado e Problema Resolvido

* **Quem sofre o problema:** Moradores, comerciantes locais, equipes da Defesa Civil municipal e lideranças comunitárias de microbacias urbanas sujeitas a enxurradas e alagamentos rápidos (exemplificado no sistema pela *Microbacia Córrego Pirajuçara - Zona Sul*, com população estimada de 18.500 habitantes em área vulnerável, sem uso de dados pessoais de indivíduos).
* **Qual decisão o sistema apoia:** Responde à pergunta *"qual problema vai atingir o nosso território nos próximos dias/horas, e o que podemos fazer hoje para evitar ou reduzir o impacto?"*, apoiando a decisão preventiva de desobstrução de bocas de lobo, sinalização de vias alagáveis, mobilização de voluntários comunitários e acionamento preventivo de abrigos antes do transbordamento.

---

## 2. Como Compilar, Executar e Rodar os Testes

### Pré-requisitos
* **Java 17** ou superior
* **Apache Maven 3.8+**

### Configuração do Banco de Dados
* O projeto utiliza **H2 Database embarcado em modo arquivo** (`jdbc:h2:file:./data/territorydb;AUTO_SERVER=TRUE`).
* **Zero configuração externa:** Ao iniciar a aplicação, a classe `ConnectionFactory` cria automaticamente o banco local na pasta `./data/` e executa os comandos `CREATE TABLE IF NOT EXISTS` para `medicoes` e `alertas`. Os dados permanecem salvos mesmo após encerrar e reiniciar o programa.

### Comandos via Terminal
```bash
# 1. Executar todos os testes unitários (JUnit 5 + Mockito)
mvn test

# 2. Compilar e iniciar a aplicação de console
mvn compile exec:java
```
*(Também pode ser executado diretamente pela IDE — IntelliJ IDEA, Eclipse ou VS Code — abrindo o arquivo `pom.xml` como projeto Maven e executando a classe `br.com.fiap.citizentech.SistemaMonitoramento`.)*

---

## 3. Arquitetura e Decisões Técnicas da Equipe

### 3.1. Domínio Orientado a Objetos e Polimorfismo
* **`ComponenteTerritorial` (Classe Abstrata) e `EstacaoHidrologica` (Concreta):** Encapsula atributos comuns (`id`, `nome`, `regiao`, `status`) e define o contrato abstrato `verificarStatus()`, implementado concretamente pela estação hidrológica conforme a disponibilidade dos sensores conectados.
* **`Sensor` (Interface) e 3 Implementações:**
  1. `SensorPluviometrico`: mede intensidade de chuva (`mm/h`, faixa física `0..300`).
  2. `SensorNivelRio`: mede cota do córrego (`m`, faixa física `0..20`).
  3. `SensorUmidadeSolo`: mede saturação hídrica do solo (`%`, faixa física `0..100`).
* **`DadosTerritorio`:** Encapsula dados demográficos e geográficos agregados da região e valida a hierarquia dos limiares (`Atenção < Alerta < Crítico`).
* **`EstrategiaPrevisao` (Abstrata) + `PrevisaoMediaMovel` e `PrevisaoComparacao`:** Sobrescrevem `prever()` e `explicarPrevisao()` reutilizando `super.validarEExtrairValores(...)` para garantir integridade antes do cálculo.

### 3.2. Exceções Customizadas e Validações
* **`DadosInvalidosException`:** Lançada quando uma leitura viola faixas físicas (ex.: chuva negativa ou umidade > 100%), quando limiares são configurados fora de ordem (`Atenção >= Alerta`), ou quando não há histórico mínimo suficiente para calcular uma previsão confiável.
* **`FonteIndisponivelException`:** Lançada quando um sensor está offline/inoperante durante a coleta.
* Todas as exceções são capturadas no menu principal (`SistemaMonitoramento`) exibindo mensagens claras e orientativas ao operador (nenhum `catch` vazio).

### 3.3. Persistência JDBC Segura
* Implementada via `MedicaoDAO` e `AlertaDAO` utilizando exclusivamente **`PreparedStatement`** com parâmetros `?` (eliminando qualquer risco de SQL Injection ou concatenação de strings) e gerenciamento automático de conexões e cursores com **`try-with-resources`**.

### 3.4. Justificativa dos Padrões Criacionais Aplicados
1. **`ConfiguracaoSistema` (Singleton):** Os limiares de `ATENÇÃO`, `ALERTA` e `CRÍTICO` precisam ser únicos e consistentes entre a tela de configuração do território (`DadosTerritorio`), o motor de emissão de alertas (`ServicoMonitoramento`) e o relatório consolidado (`RelatorioBuilder`). O construtor privado e o acesso via `getInstance()` impedem divergências de parâmetros em memória.
2. **`FabricaPrevisao` (Factory):** Centraliza a criação das estratégias polimórficas (`PrevisaoMediaMovel` e `PrevisaoComparacao`) através de um `EnumMap<TipoPrevisao, Supplier<EstrategiaPrevisao>>`, eliminando cadeias de `if/else` ou `instanceof` espalhadas pelo serviço e pelo menu.
3. **`RelatorioBuilder` (Builder):** O relatório consolidado de status territorial agrega cinco fontes distintas (estação, dados do território, sensores ativos, últimas medições e últimos alertas). O Builder permite montar esse objeto complexo passo a passo com legibilidade e sem construtores telescópicos.

### 3.5. Funcionalidades Bônus Implementadas
* **Simulação de 24 horas (Opção 6 do menu):** Simula a evolução horária de uma tempestade intensa sobre a microbacia, demonstrando na prática o período de silêncio preventivo nas primeiras horas seguido pela escalada automática entre `NORMAL`, `ATENCAO`, `ALERTA` e `CRITICO`.
* **Exportação em CSV (Opção 7 do menu):** Exporta o histórico persistido de medições (`historico_medicoes.csv`) e alertas (`historico_alertas.csv`) em formato compatível com planilhas.

---

## 4. Reflexão Crítica

### Quando o sistema deve se calar?
Um sistema preditivo deve recusar-se a emitir previsões ou alertas quando não dispõe de massa mínima de dados históricos ou quando os sensores apresentam falha de telemetria. Na nossa arquitetura, exigimos um mínimo de **5 leituras** para a Média Móvel e **6 leituras** para a Comparação de Períodos (`validarEExtrairValores`), além de bloquear sensores offline via `FonteIndisponivelException`. Com apenas uma ou duas medições isoladas, qualquer projeção seria um "chute" estatístico capaz de gerar falsos alarmes (descredibilizando o sistema junto à comunidade) ou falsa sensação de segurança.

### O que os dados não representam?
Os dados telemétricos de uma estação pontual capturam apenas variáveis físicas locais (milímetros de chuva, cota do rio e umidade do solo em um ponto específico). Eles **não representam** a microtopografia de ruas secundárias, o grau de obstrução momentânea de bueiros por resíduos sólidos, a impermeabilização irregular do solo em encostas, nem a vulnerabilidade socioeconômica das famílias residentes (como idosos, pessoas com mobilidade reduzida ou moradias precárias).

### Por que o alerta apoia a decisão da pessoa, em vez de decidir por ela?
O algoritmo identifica padrões quantitativos e rompe limiares pré-configurados, mas **não possui responsabilidade institucional nem contexto situacional**. A decisão de interditar uma via arterial, acionar sirenes ou evacuar uma comunidade envolve custos logísticos, risco de pânico e avaliação presencial das condições locais. Portanto, o alerta funciona como um **sistema de apoio à decisão**: entrega evidência rastreável (sensor, valor previsto, limite estourado e tendência) para que gestores da Defesa Civil e lideranças comunitárias tomem decisões humanas informadas e proporcionais.

---

## 5. Uso de Inteligência Artificial

* **Onde foi utilizada:** Apoio na estruturação inicial do arquivo Maven (`pom.xml`), revisão da sintaxe de testes parametrizados/mocks com JUnit 5 e Mockito, e refinamento da formatação textual do `RelatorioBuilder` e do `ExportadorCSV`.
* **Como a equipe revisou e validou:** Todos os trechos sugeridos foram analisados linha a linha pelos integrantes, adaptados às regras de negócio do recorte de alagamentos urbanos, testados manualmente via menu de console (incluindo injeção de erros propositais) e validados através da suíte automatizada de 9 testes unitários.

---

## 6. Equipe

Gabriel Simioni  | RM563475
Davi Xavier         | RM563572
Felipe Ramalho  | RM565073
