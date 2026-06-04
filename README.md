# 🧠 RetainIQ - AI Customer Churn & Re-engagement Agent

![RetainIQ Dashboard](https://images.unsplash.com/photo-1551288049-bebda4e38f71?auto=format&fit=crop&q=80&w=1200&h=400)

> An agentic system that analyzes customer purchase history, predicts churn risk using Machine Learning, and autonomously generates and sends personalized re-engagement campaigns via Email & WhatsApp.

## 🌟 Overview

RetainIQ solves the e-commerce retention problem. By leveraging predictive ML models and generative AI agents, it identifies customers who are likely to churn and crafts the perfect strategy to win them back—whether that's a 20% discount, a loyalty points boost, or a simple check-in.

### 🏗️ 3-Tier Architecture

This project is built using a modern, scalable 3-tier architecture:

1. **Dashboard (Frontend):** React + Vite. A premium dark-mode interface with glassmorphism to interact with the system.
2. **Core API (Backend):** Java + Spring Boot. Acts as the core data layer (using H2 In-Memory DB for zero-dependency setup) and proxies ML requests. Manages Customer and Campaign logs.
3. **ML & AI Engine (Microservice):** Python + FastAPI. Evaluates churn risk using a `scikit-learn` RandomForest model, and orchestrates LangChain agents (powered by Groq/Llama3 or OpenAI) for crafting personalized campaigns.

---

## 🚀 Getting Started (Local Development)

### Prerequisites
- Node.js (v18+)
- Python (3.10+)
- Java (17+)

### One-Click Run (VS Code)
We have provided a VS Code `tasks.json` to make running all 3 services trivial.
1. Open this repository in VS Code.
2. Press `Ctrl + Shift + P` (or `Cmd + Shift + P` on Mac).
3. Type **"Tasks: Run Task"** and press Enter.
4. Select **"Run RetainIQ (All Services)"**.
5. VS Code will open split terminals and start the Python, Java, and React services simultaneously.
6. Open **[http://localhost:5173](http://localhost:5173)** in your browser.

### Manual Setup & Run

If you prefer to run services individually in your terminal:

**1. ML & AI Service (Port 8000)**
```bash
cd ml-ai-service
pip install -r requirements.txt
# Optional: Add GROQ_API_KEY to .env for AI generation (falls back to templates if not provided)
python -m uvicorn app.main:app --reload --port 8000
```

**2. Java Backend (Port 8080)**
```bash
cd backend
# Windows
.\maven\apache-maven-3.9.6\bin\mvn.cmd spring-boot:run
# Mac/Linux (if Maven is installed)
mvn spring-boot:run
```

**3. React Dashboard (Port 5173)**
```bash
cd dashboard
npm install
npm run dev
```

---


---

## 🛡️ Resilience & Fallbacks

RetainIQ is designed to never fail silently. 
* **LLM Failure:** If the Groq/OpenAI API is down or keys are missing, the Python service falls back to a heuristic rule-based template generation engine.
* **ML Service Failure:** If the Python service is entirely down, the Java backend catches the timeout and uses RFM (Recency, Frequency, Monetary) heuristics to calculate a churn score locally.

---
*Built as a conceptual customer retention agent for modern retail.*
