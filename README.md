# Sample JavaFX application using Proxy API

## To run the game

`./mvnw clean javafx:run`

## To setup the API to access Chat Completions and TTS

- add in the root of the project (i.e., the same level where `pom.xml` is located) a file named `apiproxy.config`
- the details below should come from your specific GPT-model user and apiKey.

  ```
  email: "email"
  apiKey: "YOUR_KEY"
  ```

  These are your credentials to invoke the APIs.

  The token credits are charged as follows:

  - 1 token credit per 1 character for Googlel "Standard" Text-to-Speech.
  - 4 token credit per 1 character for Google "WaveNet" and "Neural2" Text-to-Speech.
  - 1 token credit per 1 character for OpenAI Text-to-Text.
  - 1 token credit per 1 token for OpenAI Chat Completions (as determined by OpenAI, charging both input and output tokens).

## To debug the game

`./mvnw clean javafx:run@debug` then in VS Code "Run & Debug", then run "Debug JavaFX"
