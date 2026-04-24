# Vitreos

Liquid Glass Messaging App - Android Native Chat Application

## Tech Stack

- **Backend:** NestJS + Socket.io + MongoDB + Redis
- **Frontend:** Android Native (Kotlin + Jetpack Compose)
- **Architecture:** MVVM + Clean Architecture
- **UI:** Liquid Glass Design (Glassmorphism)

## Features

- One-on-one real-time chat
- Typing indicator with throttling
- Online status tracking
- Message acknowledgment (✓✓)
- Liquid Glass bubbles with squishy animation
- Dark mode glassmorphism theme

## Getting Started

### Backend
```bash
cd vitreos-server
docker-compose up -d
npm run start:dev
```

### Android
```bash
cd vitreos
./gradlew assembleDebug
```

## API

- Port: 3001
- Socket namespace: /chat
- REST API: /auth/*