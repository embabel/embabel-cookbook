
# [Embabel Agent Framework](https://hub.embabel.com)

<a href="https://hub.embabel.com"><img align="left" src="https://github.com/embabel/embabel-agent/blob/main/embabel-agent-api/images/315px-Meister_der_Weltenchronik_001.jpg?raw=true" width="180"></a>

[![Docs](https://img.shields.io/badge/docs-live-brightgreen)](https://docs.embabel.com/embabel-agent/guide/0.5.0/)
[![MvnRepository](https://badges.mvnrepository.com/badge/com.embabel.agent/embabel-agent-api/badge.svg?label=MvnRepository)](https://mvnrepository.com/artifact/com.embabel.agent/embabel-agent-api)
![Build](https://github.com/embabel/embabel-agent/actions/workflows/maven.yml/badge.svg)
[![Discord](https://img.shields.io/discord/1277751399261798401?logo=discord)](https://discord.gg/t6bjkyj93q)

[//]: # ([![Quality Gate Status]&#40;https://sonarcloud.io/api/project_badges/measure?project=embabel_embabel-agent&metric=alert_status&token=d275d89d09961c114b8317a4796f84faf509691c&#41;]&#40;https://sonarcloud.io/summary/new_code?id=embabel_embabel-agent&#41;)

[//]: # ([![Bugs]&#40;https://sonarcloud.io/api/project_badges/measure?project=embabel_embabel-agent&metric=bugs&#41;]&#40;https://sonarcloud.io/summary/new_code?id=embabel_embabel-agent&#41;)
![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)
![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring](https://img.shields.io/badge/spring-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-6DB33F.svg?style=for-the-badge&logo=Spring-Boot&logoColor=white)
![Apache Tomcat](https://img.shields.io/badge/apache%20tomcat-%23F8DC75.svg?style=for-the-badge&logo=apache-tomcat&logoColor=black)
![Apache Maven](https://img.shields.io/badge/Apache%20Maven-C71A36?style=for-the-badge&logo=Apache%20Maven&logoColor=white)
![JUnit](https://img.shields.io/badge/JUnit5-25A162.svg?style=for-the-badge&logo=JUnit5&logoColor=white)
![ChatGPT](https://img.shields.io/badge/chatGPT-74aa9c?style=for-the-badge&logo=openai&logoColor=white)
![Jinja](https://img.shields.io/badge/jinja-white.svg?style=for-the-badge&logo=jinja&logoColor=black)
![JSON](https://img.shields.io/badge/JSON-000?logo=json&logoColor=fff)
![GitHub Actions](https://img.shields.io/badge/github%20actions-%232671E5.svg?style=for-the-badge&logo=githubactions&logoColor=white)
![SonarQube](https://img.shields.io/badge/SonarQube-black?style=for-the-badge&logo=sonarqube&logoColor=4E9BCD)
![Docker](https://img.shields.io/badge/docker-%230db7ed.svg?style=for-the-badge&logo=docker&logoColor=white)
![IntelliJ IDEA](https://img.shields.io/badge/IntelliJIDEA-000000.svg?style=for-the-badge&logo=intellij-idea&logoColor=white)
[![License](https://img.shields.io/github/license/embabel/embabel-agent?style=for-the-badge&logo=apache&color=brightgreen)](https://www.apache.org/licenses/LICENSE-2.0)
[![Commits](https://img.shields.io/github/commit-activity/m/embabel/embabel-agent.svg?label=commits&style=for-the-badge&logo=git&logoColor=white)](https://github.com/embabel/embabel-agent/pulse)

&nbsp;&nbsp;&nbsp;&nbsp;

Embabel (Em-BAY-bel) is a framework for authoring agentic flows on the JVM that seamlessly mix LLM-prompted interactions
with code and domain models. Supports
intelligent path finding towards goals. Written in Kotlin
but offers a natural usage
model from Java.
From the creator of Spring.

# Cookbook
Embabel Agentic AI Framework

Recepies on Building Embabel-powered Agentic Applications

Embabel Cookbook is a hands-on guide to building agentic AI applications on the JVM with the Embabel Agent Framework.
It covers agent annotations, conditions, heuristics, repeat-until loops, stuck-state debugging, startup wiring, and EPUB/HTML cookbook generation for Java and Spring Boot developers.
Keywords: Embabel, Embabel Agent Framework, agentic AI, Java, Kotlin, Spring Boot, JVM, LLM, prompt runner, conditions, heuristics, repeat-until, stuck state, MCP, A2A, RAG.

## Local build

Build the HTML cookbook locally with:

```bash
mvn -DskipTests generate-resources
```

The generated HTML is written under `target/generated-docs/`.

Build the EPUB cookbook locally with:

```bash
mvn -DskipTests -Pcookbook-epub generate-resources
```

The generated EPUB is written under `target/generated-docs/book.epub`.

For reference, the generated HTML files are also under `target/generated-docs/`, with the main entry point at `target/generated-docs/book.html`.

## Tests

Build and run tests locally with:

```bash
mvn clean install -DskipTests=false
```

By default this repo skips test execution during `mvn clean install`, but still compiles test sources.

## CI

In CI, test execution is currently skipped temporarily.
That is a stopgap until the Ollama-based test container setup is ready.
Once that container path is in place, CI should run the test suite again.

## Contributing

Contributions are welcome.
Please keep changes small, focused, and aligned with the existing cookbook structure.

## How It Differs From The User Guide

This cookbook is organized as executable documentation:

- every chapter has a corresponding test with one-to-one naming mapping
- documentation is generated from tagged code in tests
- each chapter follows the same structure: Introduction, Key Concepts, How It Works, Conclusion
- tests are self-contained and runnable on their own
- the book is built from the repo sources, so the code and prose stay aligned

## Roadmap

Tentative areas to cover next:

- create object, including `PromptRunner` options and parameters
- `create-object-if-possible`
- creating user prompts and system prompts with `PromptRunner`
- property filtering
- thinking mode
- streaming mode
- termination
- tooling, including `AgenticTool` , ```UnfoldingTools``` and subagents
- parallel tool loop, for more advanced flows
- native provider support, for more advanced flows
- interceptors and guardrails
- HITL
- observability
- MCP and A2A
- RAG
