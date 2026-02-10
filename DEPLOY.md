# Guia de Deploy (Linux/Ubuntu)

Este guia descreve o processo de deployment da aplicação **Park & Ride Simulator** num servidor Linux (VPS) utilizando Docker e Docker Compose.

## Pré-requisitos
*   Um servidor Ubuntu 22.04 LTS (ou superior).
*   **Mínimo:** 4 GB de RAM (Recomendado: 8 GB) devido aos múltiplos serviços Java.
*   Acesso SSH ao servidor.

---

## 1. Preparação do Servidor
Aceda ao servidor via SSH e instale o Docker e o Docker Compose.

```bash
# Atualizar lista de pacotes
sudo apt update
sudo apt upgrade -y

# Instalar Docker e Docker Compose Plugin
sudo apt install -y ca-certificates curl gnupg
sudo install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
sudo chmod a+r /etc/apt/keyrings/docker.gpg

echo \
  "deb [arch="$(dpkg --print-architecture)" signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
  "$(. /etc/os-release && echo "$VERSION_CODENAME")" stable" | \
  sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

sudo apt update
sudo apt install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

# Verificar instalação
docker compose version
```

---

## 2. Instalação da Aplicação

### 2.1 Clonar o Repositório
```bash
cd /opt
sudo git clone https://github.com/SEU_UTILIZADOR/park-ride-simulator.git
cd park-ride-simulator
```

### 2.2 Configurar Variáveis de Ambiente (Segurança)
Crie o ficheiro de segredos `.env` que não está no version control.

```bash
sudo nano .env
```

Cole o seguinte conteúdo (ajuste as passwords para valores seguros!):

```properties
POSTGRES_USER=prr_admin
POSTGRES_PASSWORD=UmaPasswordMuitoForteDeProducao_2024!
DB_USER=prr_app_user
DB_PASS=OutraPasswordForteParaApp!
JWT_SECRET=ChaveSecretaGeradaAleatoriamenteComMaisDe32Caracteres!
```

*Para sair do nano: Ctrl+O (Guardar), Enter, Ctrl+X (Sair).*

---

## 3. Arrancar a Aplicação
Compile e inicie os contentores em background (modo detached).

```bash
sudo docker compose up -d --build
```

Isto pode demorar alguns minutos na primeira vez, pois o Maven vai descarregar as dependências e compilar os .jar de cada microsserviço.

### Verificar Estado
```bash
sudo docker compose ps
```
Todos os serviços devem estar com o estado `Up` ou `Healthy`.

### Ver Logs (Debug)
Se algo falhar, verifique os logs de um serviço específico:
```bash
sudo docker compose logs -f service-sessoes
```

---

## 4. (Opcional) Configurar Acesso Externo
Por segurança, o ficheiro `docker-compose.yml` expõe apenas:
*   Frontend: Porta 3001
*   API Gateway: Porta 8080

Para aceder via browser:
*   **Aplicação:** `http://SEU_IP_SERVIDOR:3001`
*   **API:** `http://SEU_IP_SERVIDOR:8080`

**Recomendação:** Para produção, configure um **Nginx Reverse Proxy** na porta 80/443 com SSL (Let's Encrypt) para não expor as portas 3001/8080 diretamente.

---

## 5. Atualizar a Aplicação
Quando fizer alterações no código e enviar para o GitHub:

```bash
# 1. Puxar alterações
git pull origin main

# 2. Reconstruir e reiniciar (apenas o que mudou)
sudo docker compose up -d --build --remove-orphans

# 3. Limpar imagens antigas (poupar espaço)
sudo docker image prune -f
```
