# 🅿️ Park & Ride Simulator

> A cloud-native microservices ecosystem designed to simulate and manage intelligent Park & Ride facilities for urban sustainability.

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![React](https://img.shields.io/badge/React-18-61DAFB?style=for-the-badge&logo=react&logoColor=black)
![Docker](https://img.shields.io/badge/Docker-20.10-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![Nginx](https://img.shields.io/badge/Nginx-Proxy_Manager-009639?style=for-the-badge&logo=nginx&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-yellow.svg?style=for-the-badge)

## 📌 Project Overview

**Park & Ride Simulator** is a technical portfolio project implementing a complete **Microservices Architecture** for smart city parking management. It simulates a distributed system where drivers park at peripheral hubs to use public transport, featuring real-time tracking, dynamic billing, and role-based analytics.

This project demonstrates a production-ready infrastructure using **Docker Compose**, **Nginx Reverse Proxy**, and **Local DNS (BIND9)** to simulate a real cloud environment on a local VM.

## 🏗️ Architecture & Infrastructure

The ecosystem is composed of the following services, orchestrated via Docker:

| Service | Port (Internal) | Description |
| :--- | :--- | :--- |
| **Nginx Proxy Manager** | `80` / `81` | Reverse Proxy & SSL Termination |
| **BIND9 DNS** | `53` | Local DNS Resolution (`*.parkride.local`) |
| **API Gateway** | `8080` | Entry point, Authentication, Rate Limiting |
| **Eureka Server** | `8761` | Service Registry & Discovery |
| **Service Parques** | `8081` | Inventory & Capacity Management |
| **Service Utilizadores** | `8082` | Identity, Auth & Vehicle Registry |
| **Service Sessoes** | `8083` | Session State & Time Tracking |
| **Service Tarifas** | `8084` | Billing Engine & Invoice History |
| **Service Analytics** | `8085` | KPI Aggregation & Reporting |
| **PostgreSQL** | `5432` | Centralized Database (for dev simplicity) |
| **Frontend** | `3001` | React Single Page Application (Vite) |

---

## 🚀 Getting Started (The Professional Way)

This project is designed to run on a Linux VM (e.g., Ubuntu Server) with **Docker** & **Docker Compose** installed.

### 1. Prerequisites
*   Checking out the repository to your VM/Server.
*   System with at least **4GB RAM** (6GB+ recommended).
*   Correctly configured `.env` file (see `.env.example`).

### 2. DNS Setup (Crucial Step) 🌐
To access the application via the domain `app.parkride.local`, you must configure your client machine (Windows/Mac) to use the VM's DNS Server.

*   **Windows:** Network Settings -> IPv4 -> Set Primary DNS to your **VM's IP Address** (e.g., `192.168.1.216`).
*   **Alternative:** Add to hosts file: `192.168.1.216 app.parkride.local npm.parkride.local`

### 3. Deploy
```bash
# Clone the repository
git clone https://github.com/Andalexskywalker/park-ride-simulator.git
cd park-ride-simulator

# Create .env file (if not exists)
cp .env.example .env
nano .env # (Edit with your secrets)

# Disable local system resolver to free Port 53 for BIND9 (Ubuntu only)
sudo systemctl stop systemd-resolved
sudo systemctl disable systemd-resolved

# Launch the Ecosystem
sudo docker compose up -d --build
```

### 4. Configuration (First Run Only) ⚙️
1.  **Access Nginx Proxy Manager**: `http://npm.parkride.local:81` (or `http://<VM-IP>:81`)
    *   Default Login: `admin@example.com` / `changeme`
2.  **Create Proxy Host**:
    *   Domain: `app.parkride.local`
    *   Forward Host: `frontend`
    *   Forward Port: `80`
    *   Block Common Exploits: Enable
    *   Save.

### 5. Access the Application 🎉
> **Frontend Dashboard:** [http://app.parkride.local](http://app.parkride.local)

**Default Credentials:**
*   **Administrator**: `admin@prr.pt` / `admin123`
*   **Operator**: `op@prr.pt` / `Op123!45`

---

## 🧪 Testing & Validation

To check the health of the microservices ecosystem:

1.  **Eureka Dashboard**: [http://<VM-IP>:8761](http://<VM-IP>:8761) - Verify all services are REGISTERED and UP.
2.  **API Gateway Health**: [http://api.parkride.local/actuator/health](http://api.parkride.local/actuator/health) (requires Nginx Proxy Host for API).
3.  **Logs**:
    ```bash
    sudo docker compose logs -f service-sessoes
    ```

## 📄 License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---
*Built with ❤️ in Portugal* 🇵🇹
